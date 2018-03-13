/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.monitoring.IUiFreezeEventLogger;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

/**
 * Event loop monitoring thread. Detects events that take long time to process, collects stack
 * traces of the UI thread during processing of those events, and logs the long events to the error
 * log.
 */
public class EventLoopMonitorThread extends Thread implements Listener {
	private static final String EXTENSION_ID = "org.eclipse.ui.monitoring.logger"; //$NON-NLS-1$
	private static final String NEW_LINE_AND_BULLET = "\n* "; //$NON-NLS-1$

	/* NOTE: All time-related values in this class are in milliseconds. */

	/**
	 * Helper object for passing preference-based arguments by name to the constructor, making
	 * the code more readable compared to a large parameter list of integers and booleans.
	 */
	public static class Parameters {
		/** Milliseconds after which a long event should get logged */
		public int longEventThreshold;
		/**
		 * Milliseconds at which stack traces should be sampled. Down-sampling of a long event may
		 * extend the sampling interval for that event.
		 */
		public int sampleInterval;
		/** Milliseconds to wait before collecting the first sample. */
		public int initialSampleDelay;
		/** Maximum number of stack samples to log */
		public int maxStackSamples;
		/** Milliseconds after which an ongoing long event should logged as a deadlock */
		public long deadlockThreshold;
		/**
		 * If true, includes call stacks of all threads into the logged message. Otherwise, only
		 * the stack of the watched thread is included.
		 */
		public boolean dumpAllThreads;
		/**
		 * If true, log freeze events to the Eclipse error log on the local machine.
		 */
		public boolean logToErrorLog;
		/**
		 * Contains the list of fully qualified methods to filter out.
		 */
		public String filterTraces;

		/**
		 * Checks if parameters for plug-in are valid before startup.
		 *
		 * @throws IllegalArgumentException if the parameter values are invalid or inconsistent.
		 */
		public void checkParameters() throws IllegalArgumentException {
			StringBuilder problems = new StringBuilder();
			if (!(longEventThreshold > 0)) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_logging_threshold_error_1,
								longEventThreshold));
			}
			if (sampleInterval <= 0) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_sample_interval_error_1,
								sampleInterval));
			} else if (sampleInterval >= longEventThreshold) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_sample_interval_too_high_error_2,
								sampleInterval, longEventThreshold));
			}
			if (maxStackSamples <= 0) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_max_log_count_error_1,
								maxStackSamples));
			}
			if (initialSampleDelay <= 0) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_sample_interval_error_1,
								initialSampleDelay));
			} else if (initialSampleDelay >= longEventThreshold) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_initial_sample_delay_too_high_error_2,
								initialSampleDelay, longEventThreshold));
			}
			if (deadlockThreshold <= 0) {
				problems.append(NEW_LINE_AND_BULLET
						+ NLS.bind(Messages.EventLoopMonitorThread_deadlock_error_1,
								deadlockThreshold));
			} else if (deadlockThreshold <= longEventThreshold) {
				problems.append(NEW_LINE_AND_BULLET +
						NLS.bind(Messages.EventLoopMonitorThread_deadlock_threshold_too_low_error_2,
								deadlockThreshold, longEventThreshold));
			}

			if (problems.length() != 0) {
				throw new IllegalArgumentException(
						NLS.bind(Messages.EventLoopMonitorThread_invalid_argument_error_1,
								problems.toString()));
			}
		}
	}

	/*
	 * Tracks when the current event was started, or if the event has nested {@link Event#sendEvent}
	 * calls, then the time when the most recent nested call returns and the current event is
	 * resumed.
	 *
	 * Accessed by both the UI and monitoring threads. Updated by the UI thread and read by the
	 * polling thread. Changing this in the UI thread causes the polling thread to reset its stalled
	 * event state. The UI thread sets this value to zero to indicate a sleep state and to
	 * a positive value to represent a dispatched state. (Using zero as an invalid event start time
	 * will be wrong for a 1 millisecond window when the 32-bit system clock rolls over in 2038,
	 * but we can live with skipping any events that fall in that window).
	 */
	private volatile long eventStartOrResumeTime;

	// Accessed by both the UI and monitoring threads.
	private final int longEventThreshold;
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final AtomicReference<LongEventInfo> publishEvent =
			new AtomicReference<LongEventInfo>(null);

	// Accessed only on the polling thread.
	private final List<IUiFreezeEventLogger> externalLoggers =
			new ArrayList<IUiFreezeEventLogger>();
	private final DefaultUiFreezeEventLogger defaultLogger;
	private final Tracer tracer;
	private final Display display;
	private final FilterHandler filterHandler;
	private final long initialSampleDelay;
	private final long sampleInterval;
	private final int maxStackSamples;
	private final int maxLoggedStackSamples;
	private final long deadlockThreshold;
	private final long uiThreadId;
	private final Object sleepMonitor;
	private final boolean dumpAllThreads;
	private final boolean logToErrorLog;

	/**
	 * A helper class to track and report potential deadlocks.
	 */
	private class DeadlockTracker {
		private boolean haveAlreadyLoggedPossibleDeadlock;

		// The last time a state transition between events or sleep/wake was seen. May be set to
		// zero to indicate that deadlocks should not be tracked.
		private long lastActive;

		/**
		 * Logs a possible deadlock to the remote log. {@code lastActive} is zero if the interval is
		 * for a sleep, in which case we don't log a deadlock.
		 *
		 * @param currTime the current time
		 * @param stackSamples stack trace samples for the currently stalled event
		 * @param numStacks the number of valid stack trace samples in the stackSamples array
		 */
		public void logPossibleDeadlock(long currTime, StackSample[] stackSamples, int numStacks) {
			long totalDuration = currTime - lastActive;

			if (!haveAlreadyLoggedPossibleDeadlock && lastActive > 0 &&
					totalDuration > deadlockThreshold &&
					filterHandler.shouldLogEvent(stackSamples, numStacks, uiThreadId)) {
				stackSamples = Arrays.copyOf(stackSamples, numStacks);
				logEvent(new UiFreezeEvent(lastActive, totalDuration,
						Arrays.copyOf(stackSamples, numStacks), true));
				haveAlreadyLoggedPossibleDeadlock = true;
				Arrays.fill(stackSamples, null);
			}
		}

		/**
		 * Resets the deadlock tracker's state.
		 */
		public void reset(long lastActive) {
			this.lastActive = lastActive;
			haveAlreadyLoggedPossibleDeadlock = false;
		}
	}

	/**
	 * Initializes the static state of the monitoring thread.
	 *
	 * @param args parameters derived from preferences
	 * @throws IllegalArgumentException if monitoring thread cannot be initialized due to an error.
	 */
	public EventLoopMonitorThread(Parameters args) throws IllegalArgumentException {
		super("Event Loop Monitor"); //$NON-NLS-1$

		tracer = MonitoringPlugin.getTracer();

		Assert.isNotNull(args);

		args.checkParameters();

		setDaemon(true);
		setPriority(NORM_PRIORITY + 1);
		display = getDisplay();
		uiThreadId = this.display.getThread().getId();
		this.longEventThreshold = args.longEventThreshold;
		this.maxLoggedStackSamples = args.maxStackSamples;
		this.maxStackSamples = 2 * (args.maxStackSamples - 1);
		this.sampleInterval = args.sampleInterval;
		this.initialSampleDelay = args.initialSampleDelay;
		this.dumpAllThreads = args.dumpAllThreads;
		this.deadlockThreshold = args.deadlockThreshold;
		this.logToErrorLog = args.logToErrorLog;
		filterHandler = new FilterHandler(args.filterTraces);
		defaultLogger = new DefaultUiFreezeEventLogger();
		sleepMonitor = new Object();

		loadLoggerExtensions();

		if (!logToErrorLog && externalLoggers.isEmpty()) {
			MonitoringPlugin.logWarning(Messages.EventLoopMonitorThread_logging_disabled_error);
		}
	}

	/**
	 * Shuts down the monitoring thread. Must be called on the display thread.
	 */
	public void shutdown() throws SWTException {
		cancelled.set(true);
		if (!display.isDisposed()) {
			display.removeListener(SWT.PreEvent, this);
			display.removeListener(SWT.PostEvent, this);
			display.removeListener(SWT.Sleep, this);
			display.removeListener(SWT.Wakeup, this);
		}
		wakeUp();
	}

	@Override
	public void handleEvent(Event event) {
		/*
		 * Freeze monitoring involves seeing long intervals between BeginEvent/EndEvent messages,
		 * regardless of the level of event nesting. For example:
		 * 1) Log if a top-level or nested dispatch takes too long (interval is between BeginEvent
		 *    and EndEvent).
		 * 2) Log if preparation before popping up a dialog takes too long (interval is between two
		 *    BeginEvent messages).
		 * 3) Log if processing after dismissing a dialog takes too long (interval is between two
		 *    EndEvent messages).
		 * 4) Log if there is a long delay between nested calls (interval is between EndEvent and
		 *    BeginEvent). This could happen after a dialog is dismissed, does too much processing
		 *    on the UI thread, and then pops up a notification dialog.
		 * 5) Don't log for long delays between top-level events (interval is between EndEvent and
		 *    BeginEvent at the top level), which should involve sleeping.
		 *
		 * Calls to Display.sleep() make the UI responsive, whether or not events are actually
		 * dispatched, so items 1-4 above assume that there are no intervening calls to sleep()
		 * between the event transitions. Treating the BeginSleep event as an event transition lets
		 * us accurately capture true freeze intervals.
		 *
		 * Correct management of BeginSleep/EndSleep events allow us to handle items 4 and 5 above
		 * since we can tell if a long delay between an EndEvent and a BeginEvent are due to an idle
		 * state (in Display.sleep()) or a UI freeze.
		 *
		 * Since an idle system can potentially sleep for a long time, we need to avoid logging long
		 * delays that are due to sleeps. The eventStartOrResumeTime variable is set to zero
		 * when the thread is sleeping so that deadlock logging can be avoided for this case.
		 */
		switch (event.type) {
		case SWT.PreEvent:
			beginEvent();
			break;
		case SWT.PostEvent:
			endEvent();
			break;
		case SWT.Sleep:
			beginSleep();
			break;
		case SWT.Wakeup:
			endSleep();
			break;
		default:
		}
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void beginEvent() {
		if (tracer != null) {
			tracer.trace("Begin event");
		}
		handleEventTransition(true, false);  // Log a long interval, not entering sleep.
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void endEvent() {
		if (tracer != null) {
			tracer.trace("End event");
		}
		handleEventTransition(true, false);  // Log a long interval, not entering sleep.
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void beginSleep() {
		if (tracer != null) {
			tracer.trace("Begin sleep");
		}
		handleEventTransition(true, true);  // Log a long interval, entering sleep.
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void endSleep() {
		if (tracer != null) {
			tracer.trace("End sleep");
		}
		handleEventTransition(false, false);  // Don't log a long interval, not entering sleep.
	}

	// Called on the UI thread!
	private void handleEventTransition(boolean attemptToLogLongDelay, boolean isEnteringSleep) {
		/*
		 * On transition between events or sleeping/wake up, we need to reset the delay tracking
		 * state and possibly publish a long delay message. Updating eventStartOrResumeTime causes
		 * the polling thread to reset its stack traces, so it should always be changed *after*
		 * the event is published. The indeterminacy of threading may cause the polling thread to
		 * see both changes or only the (first) publishEvent change, but the only difference is
		 * a small window where if an additional stack trace was scheduled to be sampled, a bogus
		 * stack trace sample will be appended to the end of the samples. Analysis code needs to be
		 * aware that the last sample may not be relevant to the issue which caused the freeze.
		 */
		long currTime = getTimestamp();
		if (attemptToLogLongDelay) {
			long startTime = eventStartOrResumeTime;
			if (startTime != 0) {
				int duration = (int) (currTime - startTime);
				if (duration >= longEventThreshold) {
					LongEventInfo info = new LongEventInfo(startTime, duration);
					publishEvent.set(info);
					wakeUp();
				}
			}
		}
		// Using zero as an invalid event time will be wrong for a 1 millisecond window when
		// the system clock rolls over in 2038, but we can live with that.
		eventStartOrResumeTime = !isEnteringSleep ? currTime : 0;
	}

	@Override
	public void run() {
		/*
		 * If this event loop starts in the middle of a UI freeze, it will succeed in capturing
		 * the portion of that UI freeze that it sees.
		 *
		 * Our timer resolution is, at best, 1 millisecond so we can never try to catch events of
		 * a duration less than that.
		 */
		boolean resetStalledEventState = true;

		DeadlockTracker deadlockTracker = new DeadlockTracker();

		final long pollingNyquistDelay = sampleInterval / 2;
		long pollingDelay = 0; // Immediately updated by resetStalledEventState.
		long grabStackSampleAt = 0; // Immediately updated by resetStalledEventState.
		long lastEventStartOrResumeTime = 0; // Immediately updated by resetStalledEventState.

		StackSample[] stackSamples = new StackSample[maxStackSamples];
		int numSamples = 0;

		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		boolean dumpLockedMonitors = jvmThreadManager.isObjectMonitorUsageSupported();
		boolean dumpLockedSynchronizers = jvmThreadManager.isSynchronizerUsageSupported();
		if (dumpAllThreads && jvmThreadManager.isThreadContentionMonitoringSupported()) {
			jvmThreadManager.setThreadContentionMonitoringEnabled(true);
		}

		// Register for events
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				registerDisplayListeners();
			}
		});

		long currTime = getTimestamp();

		while (!cancelled.get()) {
			long sleepFor;
			if (resetStalledEventState) {
				long eventTime = eventStartOrResumeTime;
				deadlockTracker.reset(eventTime);
				if (eventTime == 0) {
					eventTime = currTime;
				}
				grabStackSampleAt = eventTime + initialSampleDelay;
				numSamples = 0;
				pollingDelay = sampleInterval;
				sleepFor = pollingNyquistDelay;
				resetStalledEventState = false;
			} else if (lastEventStartOrResumeTime == 0) {
				sleepFor = pollingNyquistDelay;
			} else {
				sleepFor = Math.min(pollingNyquistDelay, Math.max(1, grabStackSampleAt - currTime));
			}

			// This is the top of the polling loop.
			long sleepAt = getTimestamp();

			/*
			 * Check for starvation outside of sleeping. If we sleep or process much longer than
			 * expected (e.g. > threshold/2 longer), then the polling thread has been starved and
			 * it's very likely that the UI thread has been as well. Starvation freezes do not have
			 * useful information, so don't log them.
			 */
			long awakeDuration = currTime - sleepAt;
			boolean starvedAwake = awakeDuration > (sleepFor + longEventThreshold / 2);
			sleepForMillis(sleepFor);
			currTime = getTimestamp();
			long currEventStartOrResumeTime = eventStartOrResumeTime;
			long sleepDuration = currTime - sleepAt;
			boolean starvedSleep = sleepDuration > (sleepFor + longEventThreshold / 2);
			boolean starved = starvedSleep || starvedAwake;

			/*
			 * If after sleeping we see that a new event has been dispatched, mark that we should
			 * update the stalled event state. Otherwise, check if we have surpassed our threshold
			 * and collect a stack trace.
			 */
			if (lastEventStartOrResumeTime != currEventStartOrResumeTime || starved) {
				resetStalledEventState = true;
				if (tracer != null && starved) {
					if (starvedAwake) {
						tracer.trace(String.format(
								"Starvation detected! Polling loop took a significant amount of threshold: %dms", //$NON-NLS-1$
								awakeDuration));
					}

					if (starvedSleep) {
						tracer.trace(String.format(
								"Starvation detected! Expected a sleep of %dms but actually slept for %dms", //$NON-NLS-1$
								sleepFor, sleepDuration));
					}
				}
			} else if (lastEventStartOrResumeTime != 0) {
				deadlockTracker.logPossibleDeadlock(currTime, stackSamples, numSamples);

				// Collect additional stack traces if enough time has elapsed.
				if (maxStackSamples > 0 && currTime - grabStackSampleAt > 0) {
					if (numSamples == maxStackSamples) {
						numSamples = maxStackSamples / 2;
						decimate(stackSamples, maxStackSamples, numSamples, 0);
						pollingDelay *= 2;
					}

					try {
						ThreadInfo[] rawThreadStacks = dumpAllThreads
							? jvmThreadManager.dumpAllThreads(dumpLockedMonitors, dumpLockedSynchronizers)
									: new ThreadInfo[] {
									jvmThreadManager.getThreadInfo(uiThreadId, Integer.MAX_VALUE)
							};

						ThreadInfo[] threadStacks = rawThreadStacks;
						// If all threads were dumped, we remove the info for the monitoring thread.
						if (dumpAllThreads) {
							int index = 0;
							threadStacks = new ThreadInfo[rawThreadStacks.length - 1];

							for (int i = 0; i < rawThreadStacks.length; i++) {
								ThreadInfo currentThread = rawThreadStacks[i];

								// Skip the stack trace of the event loop monitoring thread.
								if (!isCurrentThread(currentThread.getThreadId())) {
									if (currentThread.getThreadId() == uiThreadId && i > 0) {
										// Swap main thread to first slot in array if it is not
										// there already.
										currentThread = threadStacks[0];
										threadStacks[0] = rawThreadStacks[i];
									}
									threadStacks[index++] = currentThread;
								}
							}
						}

						stackSamples[numSamples++] = new StackSample(getTimestamp(), threadStacks);
						grabStackSampleAt += pollingDelay;
					} catch (SWTException e) {
						// Display is disposed so start terminating.
						cancelled.set(true);
						resetStalledEventState = true;
					}
				}
			}

			// If a stalled event has finished, publish it and mark that the information should
			// be reset.
			LongEventInfo eventSnapshot = publishEvent.getAndSet(null);
			if (starved || eventSnapshot != null) {
				if (eventSnapshot != null) {
					// Trim last stack trace if it is too close to the end of the event.
					int trimLast = 0;
					if (numSamples - 1 > maxLoggedStackSamples) {
						long eventEnd = eventSnapshot.start + eventSnapshot.duration;
						if (eventEnd - stackSamples[numSamples - 1].getTimestamp() < sampleInterval) {
							trimLast = 1;
						}
					}

					if (numSamples > maxLoggedStackSamples) {
						decimate(stackSamples, numSamples, maxLoggedStackSamples, trimLast);
						numSamples = maxLoggedStackSamples;
					}

					if (filterHandler.shouldLogEvent(stackSamples, numSamples, uiThreadId)) {
						logEvent(new UiFreezeEvent(eventSnapshot.start, eventSnapshot.duration,
								Arrays.copyOf(stackSamples, numSamples), false));
					}
				}

				resetStalledEventState = true;
				Arrays.fill(stackSamples, null);  // Allow the stack traces to be garbage collected.
			}

			lastEventStartOrResumeTime = currEventStartOrResumeTime;
		}
	}

	private static Display getDisplay() throws IllegalArgumentException {
		IWorkbench workbench = MonitoringPlugin.getDefault().getWorkbench();
		if (workbench == null) {
			throw new IllegalArgumentException(Messages.EventLoopMonitorThread_workbench_was_null);
		}

		Display display = workbench.getDisplay();
		if (display == null) {
			throw new IllegalArgumentException(Messages.EventLoopMonitorThread_display_was_null);
		}

		return display;
	}

	// VisibleForTesting
	protected long getTimestamp() {
		return System.currentTimeMillis();
	}

	// VisibleForTesting
	protected void sleepForMillis(long milliseconds) {
		if (milliseconds > 0) {
			try {
				synchronized (sleepMonitor) {
					// Spurious wake ups are OK; they will just burn a few extra CPU cycles.
					sleepMonitor.wait(milliseconds);
				}
			} catch (InterruptedException e) {
				// Wake up.
			}
		}
	}

	private void loadLoggerExtensions() {
		IConfigurationElement[] configElements =
				Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);

		for (IConfigurationElement element : configElements) {
			try {
				Object object = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (object instanceof IUiFreezeEventLogger) {
					externalLoggers.add((IUiFreezeEventLogger) object);
				} else {
					MonitoringPlugin.logWarning(String.format(
							Messages.EventLoopMonitorThread_invalid_logger_type_error_4,
							object.getClass().getName(),
							IUiFreezeEventLogger.class.getClass().getSimpleName(),
							EXTENSION_ID, element.getContributor().getName()));
				}
			} catch (CoreException e) {
				MonitoringPlugin.logError(e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns {@code true} if given thread is the same as the current thread.
	 */
	private static boolean isCurrentThread(long threadId) {
		return threadId == Thread.currentThread().getId();
	}

	private void registerDisplayListeners() {
		display.addListener(SWT.PreEvent, EventLoopMonitorThread.this);
		display.addListener(SWT.PostEvent, EventLoopMonitorThread.this);
		display.addListener(SWT.Sleep, EventLoopMonitorThread.this);
		display.addListener(SWT.Wakeup, EventLoopMonitorThread.this);
	}

	private static void decimate(Object[] list, int fromSize, int toSize, int trimTail) {
		fromSize -= trimTail;
		for (int i = 1; i < toSize; ++i) {
			int j = (i * fromSize + toSize / 2) / toSize; // == floor(i*(from/to)+0.5) == round(i*from/to)
			list[i] = list[j];
		}
	}

	private void wakeUp() {
		synchronized (sleepMonitor) {
			sleepMonitor.notify();
		}
	}

	/**
	 * Writes the snapshot and stack captures to the workspace log.
	 */
	private void logEvent(UiFreezeEvent event) {
		if (tracer != null) {
			tracer.trace("Logging " + event);;
		}

		if (logToErrorLog) {
			defaultLogger.log(event);
		}

		for (int i = 0; i < externalLoggers.size(); i++) {
			IUiFreezeEventLogger currentLogger = externalLoggers.get(i);
			try {
				currentLogger.log(event);
			} catch (Throwable t) {
				externalLoggers.remove(i);
				i--;
				MonitoringPlugin.logError(NLS.bind(
						Messages.EventLoopMonitorThread_external_exception_error_1,
						currentLogger.getClass().getName()), t);
			}
		}
	}
}
