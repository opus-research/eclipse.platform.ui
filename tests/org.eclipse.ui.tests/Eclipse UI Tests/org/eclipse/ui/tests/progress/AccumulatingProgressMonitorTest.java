/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.progress;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.ProgressMonitorUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;

/**
 * Test the accumulating progress monitor wrapper api, which fires received
 * progress events to it's wrapped monitor in the UI thread, and makes efficient
 * use of a collector, to minimize the number of unnecessary events propagated
 * on the UI thread.
 *
 * @since 3.5
 *
 */
public class AccumulatingProgressMonitorTest extends UITestCase {
	/**
	 * @param testName
	 */
	public AccumulatingProgressMonitorTest(String testName) {
		super(testName);
	}

	/*
	 * A monitor to be wrapped. This monitor's methods assert that each method
	 * call is in the correct thread. Most will be on the UI thread but some
	 * methods are exempt.
	 */
	private class UIThreadAsserterMonitor implements IProgressMonitorWithBlocking {
		public boolean beginTaskCalled = false;
		public boolean setTaskNameCalled = false;
		public boolean subTaskCalled = false;
		public boolean setBlockedCalled = false;
		public boolean clearBlockedCalled = false;
		public boolean workedCalled = false;
		public boolean internalWorkedCalled = false;
		public boolean doneCalled = false;
		public boolean isCanceledCalled = false;
		public boolean setCanceledCalled = false;

		@Override
		public void beginTask(String name, int totalWork) {
			beginTaskCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void done() {
			doneCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void internalWorked(double work) {
			internalWorkedCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public boolean isCanceled() {
			isCanceledCalled = true;
			assertFalse(Display.getDefault().getThread() == Thread.currentThread());
			return false;
		}

		@Override
		public void setCanceled(boolean value) {
			setCanceledCalled = true;
			assertFalse(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void setTaskName(String name) {
			setTaskNameCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void subTask(String name) {
			subTaskCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void worked(int work) {
			workedCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void setBlocked(IStatus reason) {
			setBlockedCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}

		@Override
		public void clearBlocked() {
			clearBlockedCalled = true;
			assertTrue(Display.getDefault().getThread() == Thread.currentThread());
		}
	}

	/*
	 * A monitor to be wrapped by an AccumulatingProgressMonitor This monitor
	 * will keep a reference to every setTaskName call and be able to provide
	 * the list later for inspection.
	 */
	private class CollectorAsserterMonitor implements IProgressMonitorWithBlocking {
		ArrayList<String> receivedTaskNames = new ArrayList<String>();

		public ArrayList<String> getTaskNames() {
			return receivedTaskNames;
		}

		@Override
		public void beginTask(String name, int totalWork) {
		}

		@Override
		public void done() {
		}

		@Override
		public void internalWorked(double work) {
		}

		@Override
		public boolean isCanceled() {
			return false;
		}

		@Override
		public void setCanceled(boolean value) {
		}

		@Override
		public void setTaskName(String name) {
			receivedTaskNames.add(name);
		}

		@Override
		public void subTask(String name) {
		}

		@Override
		public void worked(int work) {
		}

		@Override
		public void setBlocked(IStatus reason) {
		}

		@Override
		public void clearBlocked() {
		}
	}

	/**
	 * Call all applicable methods in a separate thread. Since the test runs in
	 * the UI thread, we must fire a new thread to do the initial calls. While
	 * the thread is running, we will also run the event loop until either the
	 * thread dies or a maximum duration has passed. Then, assert that no
	 * assertion exceptions were thrown during the calls to
	 * UIThreadAsserterMonitor's methods, and assert that each method was in
	 * fact called.
	 */

	@Test
	public void testAccumulatingMonitorInUIThread() {
		final Throwable[] death = new Throwable[1];
		final UIThreadAsserterMonitor[] mon2 = new UIThreadAsserterMonitor[1];
		TestThread t = new TestThread("Test Accumulating Monitor") {
			@Override
			public void run() {
				try {
					UIThreadAsserterMonitor tm = new UIThreadAsserterMonitor();
					mon2[0] = tm;
					IProgressMonitorWithBlocking wrapper = ProgressMonitorUtil.createAccumulatingProgressMonitor(tm,
							Display.getDefault());
					wrapper.beginTask("Some Task", 100);
					wrapper.setTaskName("Task Name");
					wrapper.subTask("Subtask");
					// call work, but internalWorked will be called
					// on our monitor instead
					wrapper.worked(10);
					wrapper.isCanceled();
					wrapper.setCanceled(false);
					wrapper.setBlocked(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "Some Error"));
					// wait a short bit to ensure that setBlocked actually
					// propagates before clearing it
					try {
						Thread.sleep(200);
					} catch (InterruptedException ie) {
					}
					wrapper.clearBlocked();
					wrapper.done();
				} catch (Throwable t) {
					death[0] = t;
				}
			}
		};
		t.start();
		runEventLoop(3000, t);
		assertFalse(t.isCanceled());
		assertNull("Wrapped monitor not executed in UI thread", death[0]);
		assertNotNull(mon2[0]);
		assertTrue(mon2[0].beginTaskCalled);
		assertTrue(mon2[0].setTaskNameCalled);
		assertTrue(mon2[0].subTaskCalled);
		assertTrue(mon2[0].setBlockedCalled);
		assertTrue(mon2[0].clearBlockedCalled);
		assertTrue(mon2[0].doneCalled);
		assertTrue(mon2[0].isCanceledCalled);
		assertTrue(mon2[0].setCanceledCalled);

		// AccumulatingProgressMonitor calls internalWorked instead
		assertFalse(mon2[0].workedCalled);
		assertTrue(mon2[0].internalWorkedCalled);
	}

	/**
	 * Call setTaskName a large number of times and verify a sufficiently
	 * efficient lower number were actually propagated to our monitor. This
	 * verifies that the UI thread is not firing all events, and the
	 * AccumulatingProgressMonitor's collector is functioning properly.
	 */

	@Test
	public void testCollector() {
		final CollectorAsserterMonitor[] mon = new CollectorAsserterMonitor[1];
		final int[] numLoops = new int[] { 10000 };
		TestThread t = new TestThread("Test Accumulating Monitor") {
			@Override
			public void run() {
				mon[0] = new CollectorAsserterMonitor();
				IProgressMonitorWithBlocking wrapper = ProgressMonitorUtil.createAccumulatingProgressMonitor(mon[0],
						Display.getDefault());
				wrapper.beginTask("Some Task", 100);
				for (int i = 0; i < numLoops[0]; i++) {
					wrapper.setTaskName("Task Name " + i);
				}
				wrapper.done();
			}
		};
		t.start();
		runEventLoop(4000, t);
		assertNotNull(mon[0]);
		ArrayList<String> tasks = mon[0].getTaskNames();
		int size = tasks.size();
		assertFalse("No events were fired in the UI thread. No events reached our intended monitor.", size == 0);
		// Check less than 1/5th of events were actually fired in UI Thread,
		// with the rest of them being ignored by the efficient collector
		assertTrue("Events received should be less than 20% of events fired: expected=" + (numLoops[0] / 5)
				+ ", actual=" + size, size < (numLoops[0] / 5));
		// Assert we didn't have to cancel the thread at all due to timeout
		assertFalse(t.isCanceled());
	}

	/*
	 * Wait for the given thread to complete or a maximum of 'ms' milliseconds
	 */
	private void runEventLoop(int ms, TestThread t) {
		long time = System.currentTimeMillis();
		long end = time + ms;
		Display display = Display.getCurrent();
		while (t.isAlive() && System.currentTimeMillis() < end && !t.isCanceled()) {
			if (!display.readAndDispatch()) {
				// if there are currently no other OS event to process
				// sleep until the next OS event is available
				display.sleep();
			}
		}
		if (t.isAlive()) {
			t.setCanceled(true);
		}
	}

	/*
	 * A simple canceable thread
	 */
	private abstract class TestThread extends Thread {
		boolean canceled;

		public TestThread(String name) {
			super(name);
		}

		synchronized boolean isCanceled() {
			return canceled;
		}

		synchronized void setCanceled(boolean b) {
			canceled = b;
		}

		@Override
		abstract public void run();
	}

}
