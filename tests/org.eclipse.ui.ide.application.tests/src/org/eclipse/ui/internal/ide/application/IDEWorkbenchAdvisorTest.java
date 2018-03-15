package org.eclipse.ui.internal.ide.application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IDEWorkbenchAdvisorTest {
	private Display display = null;

	@Before
	public void setUp() {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
	}

	@After
	public void tearDown() throws Exception {
		while (display.readAndDispatch()) {
			;
		}
		display.dispose();
		assertTrue(display.isDisposed());
		display = null;

	}

	@Test
	/**
	 * Workbench shutdown should not deadlock when invoked from workspace
	 * operation
	 *
	 * Regression test for bug 501404 Timeout annotation parameter can't be
	 * used, as it makes test to be executed in non-UI thread.
	 *
	 */
	public void testShutdownWithLockedWorkspace() throws TimeoutException {
		IDEWorkbenchAdvisor advisor = new IDEWorkbenchAdvisor() {
			@Override
			public void postStartup() {
				super.postStartup();
				ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
				display.asyncExec(() -> {
					Job.getJobManager().beginRule(rule, null);
					try {
						PlatformUI.getWorkbench().close();
					} finally {
						Job.getJobManager().endRule(rule);
					}
				});
			}
		};

		int returnCode = PlatformUI.createAndRunWorkbench(display, advisor);
		Assert.assertEquals(PlatformUI.RETURN_OK, returnCode);
	}

}
