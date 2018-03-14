/*******************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.workbench;

import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;
import junit.framework.TestCase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIInject;
import org.eclipse.swt.widgets.Display;

public class InjectionThreadDomainTest extends TestCase {

	// Target for injections
	static class InjectTarget {

		private Thread thread;
		private DomainType dt;

		@Inject
		@UIInject
		public Thread setDomainType(@Optional DomainType dt) {
			this.dt = dt;
			thread = Thread.currentThread();
			return thread;
		}

		/**
		 * @return the dt
		 */
		public DomainType getDt() {
			return dt;
		}

		public Thread getThread() {
			return thread;
		}
	}

	// Target for injections
	static class InjectTargetException extends InjectTarget {

		@Inject
		@UIInject
		public Thread setDomainType(@Optional DomainType dt) {
			super.setDomainType(dt);
			throw new RuntimeException("intended");
		}

	}

	// Simple domain object
	static class DomainType {

	}

	/* setup & teardown */

	private InjectTarget target;
	private IEclipseContext context;
	protected boolean seenException = false;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Make sure default display is created in main thread
		final Display defaultDisplay = Display.getDefault();
		// Check that this is indeed true (HACK)
		assertTrue(defaultDisplay.getThread().getName().contains("main"));

		context = EclipseContextFactory.create();
		context.set(DomainType.class, new DomainType());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		if (target != null) {
			ContextInjectionFactory.uninject(target, context);
		}
	}

	/* unit tests */

	public void testUIInjectInMain() {
		target = ContextInjectionFactory.make(InjectTarget.class, context);

		assertEquals(target.getThread(), Display.getDefault().getThread());
	}

	public void testUIInjectInBackend() {
		final CountDownLatch cdl = new CountDownLatch(1);

		new Job("testUIInjectInBackend") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				target = ContextInjectionFactory.make(InjectTarget.class,
						context);

				cdl.countDown();
				return Status.OK_STATUS;
			}
		}.schedule();

		final Display display = busyWaitForLatch(cdl);

		assertEquals(target.getThread(), display.getThread());
	}

	// Busy wait on the CountDownLatch but continue to process the UI event
	// loop. If it just awaits on the CDL, the injection on DomainType is
	// blocked because the jUnit tests blocks the main aka UI thread.
	private Display busyWaitForLatch(final CountDownLatch cdl) {
		final Display display = Display.getDefault();
		while (cdl.getCount() > 0) {
			display.readAndDispatch();
		}
		return display;
	}

	public void testUnUIInjectInBackEnd() {
		final CountDownLatch cdl = new CountDownLatch(1);

		new Job("testUnUIInjectInBackEnd#Inject") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				target = ContextInjectionFactory.make(InjectTarget.class,
						context);

				cdl.countDown();
				return Status.OK_STATUS;
			}
		}.schedule();

		final Display display = busyWaitForLatch(cdl);

		final CountDownLatch cdl2 = new CountDownLatch(1);
		new Job("testUnUIInjectInBackEnd#UNInject") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				context.remove(DomainType.class);

				cdl2.countDown();
				return Status.OK_STATUS;
			}
		}.schedule();

		busyWaitForLatch(cdl2);

		// After un-injection, domain type has to be null, still in UI thread
		// though
		assertNull(target.getDt());
		assertTrue(target.getThread().equals(display.getThread()));
	}

	public void testUIInjectInBackendWithException() {
		final CountDownLatch cdl = new CountDownLatch(1);

		new Job("testUIInjectInBackendWithException") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					target = ContextInjectionFactory.make(
							InjectTargetException.class, context);
				} catch (InjectionException e) {
					seenException = true;
				}

				cdl.countDown();
				return Status.OK_STATUS;
			}
		}.schedule();

		busyWaitForLatch(cdl);

		assertTrue(seenException);
	}
}
