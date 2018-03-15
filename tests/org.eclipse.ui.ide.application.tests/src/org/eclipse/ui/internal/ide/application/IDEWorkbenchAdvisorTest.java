/*******************************************************************************
 * Copyright (c) 2016 InterSystems Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vasili Gulevich                   - Bug 501404
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IDEWorkbenchAdvisorTest {
	private static final String PLUGIN_ID = "org.eclipse.ui.ide.application.tests";
	private Display display = null;

	private static final class SaveHook implements ISaveParticipant, Closeable {
		public ISaveContext saving = null;
		public ISaveContext rollback = null;
		public ISaveContext prepareToSave = null;
		public ISaveContext doneSaving = null;
		public final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		public SaveHook() throws CoreException {
			workspace.addSaveParticipant(PLUGIN_ID, this);
		}

		@Override
		public void saving(ISaveContext context) {
			saving = context;
		}

		@Override
		public void rollback(ISaveContext context) {
			rollback = context;
		}

		@Override
		public void prepareToSave(ISaveContext context) {
			prepareToSave = context;
		}

		@Override
		public void doneSaving(ISaveContext context) {
			doneSaving = context;
		}

		@Override
		public void close() {
			workspace.removeSaveParticipant(PLUGIN_ID);
		}
	}

	@Before
	public void setUp() {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
	}

	@After
	public void tearDown() throws Exception {
		dispatchDisplay();
		display.dispose();
		assertTrue(display.isDisposed());
		display = null;

	}

	/**
	 * Workbench shutdown should not deadlock when invoked from workspace
	 * operation
	 *
	 * Regression test for bug 501404 Timeout annotation parameter can't be
	 * used, as it makes test to be executed in non-UI thread.
	 *
	 */
	@Test
	public void testShutdownWithLockedWorkspace() throws CoreException {
		try (SaveHook saveHook = new SaveHook()) {
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
			dispatchDisplay();

			Assert.assertNotNull(saveHook.prepareToSave);
			Assert.assertNotNull(saveHook.saving);
			Assert.assertNotNull(saveHook.prepareToSave);
			Assert.assertNotNull(saveHook.doneSaving);
			Assert.assertNull(saveHook.rollback);
		}
	}

	/**
	 * Workbench shutdown should disconnect workspace if it is not locked
	 *
	 * Regression test for bug 501404 Timeout annotation parameter can't be
	 * used, as it makes test to be executed in non-UI thread.
	 */
	@Test
	public void testShutdownWithUnlockedWorkspace() throws CoreException {
		try (SaveHook saveHook = new SaveHook()) {
			IDEWorkbenchAdvisor advisor = new IDEWorkbenchAdvisor() {
				@Override
				public void postStartup() {
					super.postStartup();
					display.asyncExec(() -> {
						PlatformUI.getWorkbench().close();
					});
				}
			};
			int returnCode = PlatformUI.createAndRunWorkbench(display, advisor);
			Assert.assertEquals(PlatformUI.RETURN_OK, returnCode);
			dispatchDisplay();

			Assert.assertNotNull(saveHook.prepareToSave);
			Assert.assertNotNull(saveHook.saving);
			Assert.assertNotNull(saveHook.prepareToSave);
			Assert.assertNotNull(saveHook.doneSaving);
			Assert.assertNull(saveHook.rollback);
		}

	}

	/**
	 * Process display events until there are none left
	 */
	private void dispatchDisplay() {
		while (display.readAndDispatch()) {
			;
		}
	}

}
