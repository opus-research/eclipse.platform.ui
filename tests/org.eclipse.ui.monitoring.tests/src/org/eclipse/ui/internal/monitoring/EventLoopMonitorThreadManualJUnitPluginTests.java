/*******************************************************************************
 * Copyright (C) 2014, Google Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import junit.framework.TestCase;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.monitoring.PreferenceConstants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A test that measures performance overhead of {@link EventLoopMonitorThread}.
 * This test is not included into {@link AllMonitoringTests} due to its
 * low reliability and the amount of time it takes.
 */
public class EventLoopMonitorThreadManualJUnitPluginTests extends TestCase {
	// Test Parameters
	/** Time each measurement should run for. This will affect the number of samples collected. */
	protected static final double TARGET_RUNNING_TIME_PER_MEASUREMENT = 5.0; // seconds

	/** Maximum allowable relative increase due to taking traces on the UI thread. */
	protected static final double MAX_RELATIVE_INCREASE_ONE_STACK = 2; // %

	/** Maximum allowable relative increase due to taking traces on all threads. */
	protected static final double MAX_RELATIVE_INCREASE_ALL_STACKS = 7; // %

	/** Number of times to repeat the control measurement. This should always be at least 2. */
	protected static final int NUM_CONTROL_MEASUREMENTS = 5;

	/** Number of times to repeat the UI stack sampling measurement. */
	protected static final int NUM_UI_STACK_MEASUREMENTS = 5;

	/** Number of times to repeat the all stacks sampling measurement. */
	protected static final int NUM_ALL_STACKS_MEASUREMENTS = 5;

	// Calibration Tuning Parameters
	/**
	 * Number of work units to integrate over while calibrating. This parameter is used to adjust how
	 * much work is done to capture some jitter but too large of value will oscillate with large
	 * events (e.g. GC) instead of converging.
	 */
	protected static final long WORK_INTEGRATION_ITERATIONS = 70000; // iterations

	/**
	 * Number of consecutive iterations below the error threshold (defined by
	 * {@link #CAL_MAX_RELATIVE_ERROR}) before accepting a calibration result. Due to occasional
	 * jitter (e.g. OS scheduling, GC, etc), large values may not ever converge.
	 */
	protected static final int MIN_CONVERGING_ITERATIONS = 256; // iterations

	/**
	 * Number of outliers, relative to samples, to tolerate while calculating convergence during
	 * calibration. Tolerated points are ignored (e.g. not factored into the rolling average).
	 */
	protected static final double MAX_OUTLIER_RATIO = 0.03;

	/**
	 * Weight of new elements added to the exponential averaging of the mean during calibration. The
	 * optimal value is [2/(N-1)] where N is the desired number of samples to average.
	 */
	protected static final double CAL_EMA_ALPHA = 2.0 / (MIN_CONVERGING_ITERATIONS - 1.0);

	/**
	 * Max relative error between each new estimated mean and the previous estimate. Once the relative
	 * error stabilizes below this threshold for long enough (defined by
	 * {@link #MIN_CONVERGING_ITERATIONS} iterations) the calibration is complete.
	 */
	protected static final double CAL_MAX_RELATIVE_ERROR = 0.003;

	/**
	 * Pseudo-random Noise LFSR generator polynomial, degree = 64. Polynomial for maximum sequence
	 * length by Wayne Stahnke, Primitive Polynomials Modulo Two,
	 * http://www.ams.org/journals/mcom/1973-27-124/S0025-5718-1973-0327722-7/S0025-5718-1973-0327722-7.pdf
	 *
	 * <p>
	 * At 80ns/call, the sequence will have a period of >23382 years (== (2^63-1) * 80ns)
	 */
	protected static final long PN63_GENERATOR_POLY = (3L << 62) | 1;

	@Override
	public void setUp() {
		getPreferences().setValue(PreferenceConstants.MONITORING_ENABLED, false);
	}

	@Override
	public void tearDown() throws Exception {
		getPreferences().setToDefault(PreferenceConstants.MONITORING_ENABLED);
	}

	protected static long pn63(long pnSequence) {
		int nextBit = 0;
		for (int i = 0; i < 64; ++i) {
			if (((PN63_GENERATOR_POLY >> i) & 1) == 1) {
				nextBit ^= (pnSequence >> i) & 1;
			}
		}

		return (pnSequence << 1) | nextBit;
	}

	protected static long doWork(long pnSeqeuence, long iterations) {
		LinkedList<String> messages = new LinkedList<String>();

		for (long i = 0; i < iterations; ++i) {
			pnSeqeuence = pn63(pnSeqeuence);

			if (i % 10000 == 0) {
				messages.add("10000!");
			} else if (i % 1000 == 0) {
				for (int c = messages.size() / 2; c >= 0; --c) {
					messages.removeFirst();
				}
			} else if (i % 100 == 0) {
				messages.add(String.format("Iteration %d", i));
			}
		}

		for (String s : messages) {
			pnSeqeuence ^= s.hashCode();
		}

		return pnSeqeuence;
	}

	/**
	 * Performance test for {@link EventLoopMonitorThread}. This test verifies that the monitoring
	 * doesn't interfere too much with the real work being done.
	 */
	public void testFixedWork() throws Exception {
		final Display display = Display.getDefault();
		assertNotNull("No SWT Display available.", display);

		final MockUiFreezeEventLogger logger = new MockUiFreezeEventLogger();
		final CountDownLatch backgroundJobsDone = new CountDownLatch(1);
		Queue<Thread> threads = startBackgroundThreads(backgroundJobsDone);

		double nsPerWork = calibrate(display);
		final long numIterations = Math.round(1e9 * TARGET_RUNNING_TIME_PER_MEASUREMENT / nsPerWork);

		final double[] tWork = {0};
		final long[] workOutput = {0};
		final Runnable doFixedAmountOfWork = new Runnable() {
			@Override
			public void run() {
				long start = System.nanoTime();
				long result = doWork(start, numIterations);
				tWork[0] = System.nanoTime() - start;
				workOutput[0] ^= result;
			}
		};

		List<Double> controlResults = new ArrayList<Double>(NUM_CONTROL_MEASUREMENTS);
		List<Double> uiStackResults = new ArrayList<Double>(NUM_UI_STACK_MEASUREMENTS);
		List<Double> allStacksResults = new ArrayList<Double>(NUM_ALL_STACKS_MEASUREMENTS);
		double expectedRunningTime = 0.0;
		double maxRunningTime = 0.0;

		/*
		 * This could be made more precise by measuring the control loop many times, calculating the
		 * mean and standard deviation, measuring each of the test case a few times, calculating the
		 * probability of the result, and comparing that probability to some acceptable bound.
		 */
		System.out.println(String.format("Starting %d control measurements without monitoring.",
				NUM_CONTROL_MEASUREMENTS));
		for (int i = 1; i <= NUM_CONTROL_MEASUREMENTS; ++i) {
			display.syncExec(doFixedAmountOfWork);
			System.out.println(String.format("Control measurement %d/%d finished. tWork = %fs",
					i, NUM_CONTROL_MEASUREMENTS, tWork[0] / 1e9));
			controlResults.add(tWork[0]);
			expectedRunningTime += tWork[0] / NUM_CONTROL_MEASUREMENTS;
			if (tWork[0] > maxRunningTime) {
				maxRunningTime = tWork[0];
			}
		}

		// Calculate error bound between mean & max control runs
		double controlDiff = Math.abs((maxRunningTime - expectedRunningTime) / expectedRunningTime);
		double maxRelativeIncreaseOneStackAllowed =
				(MAX_RELATIVE_INCREASE_ONE_STACK / 100) * (1 + controlDiff);
		double maxRelativeIncreaseAllStacksAllowed =
				(MAX_RELATIVE_INCREASE_ALL_STACKS / 100) * (1 + controlDiff);

		Thread monitor1 = createAndStartMonitoringThread(display, false);
		double worstRelativeDiffOneThread = Double.MIN_NORMAL;
		System.out.println(String.format("Starting %d measurements while collecting UI thread stacks.",
				NUM_UI_STACK_MEASUREMENTS));
		for (int i = 1; i <= NUM_UI_STACK_MEASUREMENTS; ++i) {
			display.syncExec(doFixedAmountOfWork);
			uiStackResults.add(tWork[0]);
			double relativeDiffOneThread = (tWork[0] - expectedRunningTime) / expectedRunningTime;
			if (relativeDiffOneThread > worstRelativeDiffOneThread) {
				worstRelativeDiffOneThread = relativeDiffOneThread;
			}
			System.out.println(String.format(
					"Measurement %d/%d finished. tWork = %fs, Relative increase = %f%%  (allowed < %f%%)",
					i, NUM_UI_STACK_MEASUREMENTS, tWork[0] / 1e9, relativeDiffOneThread * 100,
					maxRelativeIncreaseOneStackAllowed * 100));
		}
		killMonitorThread(monitor1, display);

		Thread monitor2 = createAndStartMonitoringThread(display, true);
		double worstRelativeDiffAllThreads = Double.MIN_NORMAL;
		System.out.println(String.format("Starting %d measurements while collecting all thread stacks.",
				NUM_ALL_STACKS_MEASUREMENTS));
		for (int i = 1; i <= NUM_ALL_STACKS_MEASUREMENTS; ++i) {
			display.syncExec(doFixedAmountOfWork);
			allStacksResults.add(tWork[0]);
			double relativeDiffAllThreads = (tWork[0] - expectedRunningTime) / expectedRunningTime;
			if (relativeDiffAllThreads > worstRelativeDiffAllThreads) {
				worstRelativeDiffAllThreads = relativeDiffAllThreads;
			}
			System.out.println(String.format(
					"Measurement %d/%d finished. tWork = %fs, Relative increase = %f%%  (allowed < %f%%)",
					i, NUM_ALL_STACKS_MEASUREMENTS, tWork[0] / 1e9, relativeDiffAllThreads * 100,
					maxRelativeIncreaseAllStacksAllowed * 100));
		}
		killMonitorThread(monitor2, display);

		backgroundJobsDone.countDown();

		// Tabulate final results while waiting for monitor to finish
		double controlMean = 0;
		double controlMin = Double.MAX_VALUE;
		double controlMax = Double.MIN_NORMAL;
		double controlM2 = 0;
		for (int n = 0; n < controlResults.size(); ) {
			double v = controlResults.get(n) / 1e9;
			controlResults.set(n, v);
			++n;
			double delta = v - controlMean;
			controlMean += delta / n;
			controlM2 += delta * (v - controlMean);
			controlMin = Math.min(controlMin, v);
			controlMax = Math.max(controlMax, v);
		}
		double controlStD = Math.sqrt(controlM2 / controlResults.size());

		double uiStackMean = 0;
		double uiStackMin = Double.MAX_VALUE;
		double uiStackMax = Double.MIN_NORMAL;
		double uiStackM2 = 0;
		for (int n = 0; n < uiStackResults.size(); ) {
			double v = uiStackResults.get(n) / 1e9;
			uiStackResults.set(n, v);
			++n;
			double delta = v - uiStackMean;
			uiStackMean += delta / n;
			uiStackM2 += delta * (v - uiStackMean);
			uiStackMin = Math.min(uiStackMin, v);
			uiStackMax = Math.max(uiStackMax, v);
		}

		double allStacksMean = 0;
		double allStacksMin = Double.MAX_VALUE;
		double allStacksMax = Double.MIN_NORMAL;
		double allStacksM2 = 0;
		for (int n = 0; n < allStacksResults.size(); ) {
			double v = allStacksResults.get(n) / 1e9;
			allStacksResults.set(n, v);
			++n;
			double delta = v - allStacksMean;
			allStacksMean += delta / n;
			allStacksM2 += delta * (v - allStacksMean);
			allStacksMin = Math.min(allStacksMin, v);
			allStacksMax = Math.max(allStacksMax, v);
		}

		// Publish results to output
		boolean gotFreezeEvents =
		logger.getLoggedEvents().size() == NUM_UI_STACK_MEASUREMENTS + NUM_ALL_STACKS_MEASUREMENTS;

		boolean oneThreadOk = worstRelativeDiffOneThread < maxRelativeIncreaseOneStackAllowed;
		boolean allThreadsOk = worstRelativeDiffAllThreads < maxRelativeIncreaseAllStacksAllowed;

		// Ensure the work cannot be optimized away completely
		System.out.println(String.format("Work result = %016X", workOutput[0]));

		System.out.println(oneThreadOk && allThreadsOk && gotFreezeEvents
				&& !logger.getLoggedEvents().isEmpty() ? "Test passed"
						: "Test failed!");

		if (!oneThreadOk) {
			System.out.println(" * Monitoring UI thread slowed down work too much");
		}
		if (!allThreadsOk) {
			System.out.println(" * Monitoring all threads slowed down work too much");
		}
		if (!gotFreezeEvents) {
			System.out.println(String.format(" * Didn't get expected freeze events (got %d/%d)",
					logger.getLoggedEvents().size(),
					NUM_UI_STACK_MEASUREMENTS + NUM_ALL_STACKS_MEASUREMENTS));
		}

		System.out.println();
		System.out.println("Raw results (in seconds): ");
		System.out.println(String.format(
				" * Control    (min = %f max = %f avg = %f stD = %f): %s",
				controlMin, controlMax, controlMean, controlStD, joinItems(controlResults)));
		System.out.println(String.format(
				" * UI stack   (min = %f max = %f avg = %f stD = %f, vsC = %f): %s",
				uiStackMin, uiStackMax, uiStackMean, Math.sqrt(uiStackM2 / uiStackResults.size()),
				Math.abs(uiStackMean - controlMean) / controlStD, joinItems(uiStackResults)));
		System.out.println(String.format(
				" * All stacks (min = %f max = %f avg = %f stD = %f, vsC = %f): %s",
				allStacksMin, allStacksMax, allStacksMean, Math.sqrt(allStacksM2 / allStacksResults.size()),
				Math.abs(allStacksMean - controlMean) / controlStD,
				joinItems(allStacksResults)));

		// Join all threads
		while (!threads.isEmpty()) {
			Thread t = threads.poll();
			try {
				t.join();
			} catch (InterruptedException e) {
				threads.offer(t); // Retry
			}
		}

		// Report test results
		assertTrue("Monitoring slowed down cases too much", oneThreadOk || allThreadsOk);
		assertTrue("Monitoring UI thread slowed down work too much", oneThreadOk);
		assertTrue("Did not log expected freeze events", gotFreezeEvents);
		assertTrue("Monitoring all threads slowed down work too much", allThreadsOk);
	}

	private Thread createAndStartMonitoringThread(Display display, boolean dumpAll) throws Exception {
		CountDownLatch monitorStarted = new CountDownLatch(1);
		Thread monitor = createTestMonitor(dumpAll, monitorStarted);
		startMontoring(display, monitor, monitorStarted);
		return monitor;
	}

	private void killMonitorThread(final Thread thread, Display display) throws Exception {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				shutdownThread(thread);
			}
		});

		thread.join();
	}

	protected static EventLoopMonitorThread.Parameters createDefaultParameters() {
		IPreferenceStore preferences = MonitoringPlugin.getDefault().getPreferenceStore();
		EventLoopMonitorThread.Parameters params = new EventLoopMonitorThread.Parameters();

		params.loggingThreshold = preferences.getInt(PreferenceConstants.MAX_EVENT_LOG_TIME_MILLIS);
		params.samplingThreshold = preferences.getInt(PreferenceConstants.MAX_EVENT_SAMPLE_TIME_MILLIS);
		params.dumpAllThreads = preferences.getBoolean(PreferenceConstants.DUMP_ALL_THREADS);
		params.minimumPollingDelay = preferences.getInt(
				PreferenceConstants.SAMPLE_INTERVAL_TIME_MILLIS);
		params.loggedTraceCount = preferences.getInt(PreferenceConstants.MAX_LOG_TRACE_COUNT);
		params.deadlockDelta = preferences.getInt(PreferenceConstants.FORCE_DEADLOCK_LOG_TIME_MILLIS);
		params.filterTraces = preferences.getString(PreferenceConstants.FILTER_TRACES);

		params.checkParameters();
		return params;
	}

	protected Thread createTestMonitor(boolean dumpAllThreads, final CountDownLatch monitorStarted)
			throws Exception {
		EventLoopMonitorThread.Parameters args = createDefaultParameters();
		args.samplingThreshold = 100;
		args.minimumPollingDelay = 100;
		args.dumpAllThreads = dumpAllThreads;

		return new EventLoopMonitorThread(args) {
			@Override
			protected Display getDisplay() {
				return Display.getDefault();
			}

			/**
			 * Replaces the super-class implementation with a no-op. This breaks the implicit contract
			 * that some amount of time should have passed when sleepForMillis is called with a non-zero
			 * argument, but in this testing environment giving the unit tests complete control over the
			 * elapsed time allows the tests to be more deterministic.
			 */
			@Override
			protected void sleepForMillis(long nanoseconds) {
				monitorStarted.countDown();
				super.sleepForMillis(nanoseconds);
			}
		};
	}

	protected Queue<Thread> startBackgroundThreads(final CountDownLatch backgroundJobsDone) {
		final Runnable backgroundTaskRunnable = new Runnable() {
			@Override
			public void run() {
				final double dutyCycle = 0.10;

				final double min = 100; // ns
				final double max = 1e9; // ns
				final double skew = 0.1; // the degree to which the values cluster around the mode
				final double bias = -1e5; // bias the mode to approach the min (< 0) vs max (> 0)

				double range = max - min;
				double mid = min + range / 2.0;
				double biasFactor = Math.exp(bias);
				Random rng = new Random();

				while (true) {
					double rv = rng.nextGaussian();
					double runFor = mid + (range * (biasFactor / (biasFactor + Math.exp(-rv / skew)) - 0.5));

					long endTime = System.nanoTime() + (long) runFor;
					do {
						doWork(rng.nextInt(), (int) runFor);
					} while (endTime - System.nanoTime() > 0);

					double sleepScale = Math.abs(rng.nextGaussian() / dutyCycle);
					try {
						if (backgroundJobsDone.await((int) Math.round(runFor * sleepScale),
								TimeUnit.NANOSECONDS)) {
							return;
						}
					} catch (InterruptedException e) {
						// Wake up.
					}
				}
			}
		};

		final Runnable backgroundIdle = new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						backgroundJobsDone.await();
						return;
					} catch (InterruptedException e) {
						// Wake up.
					}
				}
			}
		};

		Random rng = new Random();
		int activeThreads = Thread.activeCount();
		int numBackgroundTasks = activeThreads <= 2 ? 5 + rng.nextInt(5) : 1;
		int numBackgroundIdlers = 8 + rng.nextInt(30);

		System.out.println(String.format("Starting %d background tasks and %d background idlers",
				numBackgroundTasks, numBackgroundIdlers));
		Queue<Thread> threads = new ArrayDeque<Thread>(numBackgroundIdlers + numBackgroundTasks);
		for (int i = 0; i < numBackgroundTasks; i++) {
			Thread t = new Thread(backgroundTaskRunnable);
			threads.add(t);
			t.start();
		}

		for (int i = 0; i < numBackgroundIdlers; i++) {
			Thread t = new Thread(backgroundIdle);
			threads.add(t);
			t.start();
		}
		return threads;
	}

	protected void startMontoring(final Display display, final Thread swtThread,
			CountDownLatch eventsRegistered) throws Exception {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				swtThread.start();

				// If we're still running when display gets disposed, shutdown the thread.
				display.disposeExec(new Runnable() {
					@Override
					public void run() {
						shutdownThread(swtThread);
					}
				});
			}
		});

		for (boolean eventsReady = false; !eventsReady;) {
			while (display.readAndDispatch()) { /* keep invoking events */
			}

			eventsReady |= eventsRegistered.await(1, TimeUnit.MILLISECONDS);
		}
	}

	protected void shutdownThread(Thread monitor) {
		((EventLoopMonitorThread) monitor).shutdown();
	}

	/**
	 * @returns nanoseconds/unitWork
	 */
	protected double calibrate(final Display display) {
		System.out.println("Starting calibration...");
		final double[] tWork = {0};
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				tWork[0] = measureAvgTimePerWorkItem();
			}
		});
		System.out.println(String.format("Calibration finished. tWorkUnit = %fns", tWork[0]));
		return tWork[0];
	}

	protected double measureAvgTimePerWorkItem() {
		int consecutiveGood = 0;
		int outliers = 0;
		int hash = 0;
		double avgAbsRelErr = 0;

		double numSamplesAveraged = 2.0 / CAL_EMA_ALPHA + 1.0;

		// Ensure everything is in memory, JITed, and ready to go.
		doWork(0, 1000);

		// Initialize a meaningful mean
		long start = System.nanoTime();
		long result = doWork(0, WORK_INTEGRATION_ITERATIONS);
		double mean = System.nanoTime() - start;
		double oldMean;

		long n = 0;
		long startWallTime = System.currentTimeMillis();
		long nextPrintAt = startWallTime + 5000; // 5s
		while (++n < Long.MAX_VALUE
				&& (consecutiveGood < MIN_CONVERGING_ITERATIONS || avgAbsRelErr > CAL_MAX_RELATIVE_ERROR)) {
			start = System.nanoTime();
			result = doWork(result, WORK_INTEGRATION_ITERATIONS);
			long duration = System.nanoTime() - start;
			hash ^= result;

			double deltaDuration = duration - mean;
			oldMean = mean;
			mean += CAL_EMA_ALPHA * (deltaDuration - mean); // EMA
			double absRelErr = Math.abs((oldMean - mean) / mean);
			if (absRelErr < CAL_MAX_RELATIVE_ERROR) {
				consecutiveGood++;
				avgAbsRelErr = avgAbsRelErr * ((consecutiveGood - 1) / consecutiveGood)
						+ (absRelErr / consecutiveGood);
			} else if (outliers < (int) (MAX_OUTLIER_RATIO * Math.min(n, numSamplesAveraged))) {
				++outliers;
			} else {
				consecutiveGood = 1;
				outliers = 0;
				avgAbsRelErr = absRelErr;
			}

			if (nextPrintAt - System.currentTimeMillis() < 0) {
				nextPrintAt += 2000; // 2s
				System.out.println(String.format(
						"Still calibrating... n = %3d\t\ttWork = %f ns\t\tRelErr = %f\t\tOutliers = %d/%d",
						n, 2.0 * mean / WORK_INTEGRATION_ITERATIONS, avgAbsRelErr, outliers,
						(int) (MAX_OUTLIER_RATIO * Math.min(n, numSamplesAveraged))));
			}
		}

		double tWork = 2.0 * mean / WORK_INTEGRATION_ITERATIONS;

		// Printing the hash ensures it cannot be optimized away completely
		System.out.println(String.format("Measurement converged in %d ms (%d loops) with result=%16x. "
				+ "tWork = %fns, relErr = %f, outliers = %d",
				System.currentTimeMillis() - startWallTime,
				n,
				hash,
				tWork,
				avgAbsRelErr,
				outliers));

		return tWork;
	}

	private String joinItems(List<Double> items) {
		StringBuilder mergedItems = new StringBuilder();

		for (double item : items) {
			if (mergedItems.length() != 0) {
				mergedItems.append(',');
			}
			mergedItems.append(item);
		}

		return mergedItems.toString();
	}

	private static IPreferenceStore getPreferences() {
		return MonitoringPlugin.getDefault().getPreferenceStore();
	}
}
