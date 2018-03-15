/*******************************************************************************
 * Copyright (c) 2016 Lars Vogel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *     Axel Richard <axel.richard@obeo.fr> - bug 486644
 *******************************************************************************/
package org.eclipse.ui.internal.ide.addons;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Model add-on for automatic save of dirty editors.
 *
 * @since 3.12
 */
public class SaveAllDirtyPartsAddon {

	private WorkbenchJob saveJob;

	private IWorkbenchPage activePage;

	EventHandler dirtyHandler = new EventHandler() {

		@Override
		public void handleEvent(Event event) {
			if (WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.SAVE_AUTOMATICALLY)) {
				Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (!(newValue instanceof Boolean)) {
					return;
				}
				boolean dirty = (Boolean) newValue;
				if (!dirty) {
					return;
				}
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				// initial case
				if (activePage == null) {
					activePage = page;
				}
				if (page != null && page.equals(activePage)) {
					if (display == null) {
						display = PlatformUI.getWorkbench().getDisplay();
						display.addFilter(SWT.KeyUp, idleListener);
						display.addFilter(SWT.MouseUp, idleListener);
					}
				} else {
					activePage = page;
				}
			}
		}
	};

	@Inject
	IEventBroker eventBroker;

	private boolean initial = true;

	private Listener idleListener;

	private Runnable runnable;

	private Display display;

	@Inject
	@Optional
	private void setAutoSave(
			@SuppressWarnings("restriction") @Preference(value = IPreferenceConstants.SAVE_AUTOMATICALLY, nodePath = "org.eclipse.ui.workbench") boolean autoSave) {
		if (autoSave) {
			System.out.println("ON"); //$NON-NLS-1$
			// do not save during initialization
			if (!initial) {
				// ensure every dirty part is saved
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (IWorkbenchWindow iWorkbenchWindow : windows) {
					IWorkbenchPage p = iWorkbenchWindow.getActivePage();
					if (p != null) {
						p.saveAllEditors(false);
					}
				}
			}
			// await new dirty events
			eventBroker.subscribe(UIEvents.Dirtyable.TOPIC_DIRTY, dirtyHandler);
			initial = false;
		} else {
			eventBroker.unsubscribe(dirtyHandler);
			System.out.println("OFF"); //$NON-NLS-1$
			shutdown();
		}
	}

	/**
	 * Creates the job for saving the dirty editor
	 */
	public SaveAllDirtyPartsAddon() {
		saveJob = doCreateSaveJob();
		runnable = new Runnable() {
			@Override
			public void run() {
				saveJob.schedule();
				display.timerExec(WorkbenchPlugin.getDefault().getPreferenceStore()
							.getInt(IPreferenceConstants.SAVE_AUTOMATICALLY_INTERVAL) * 1000, this);
			}
		};
		idleListener = new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if (display != null) {
					display.timerExec(WorkbenchPlugin.getDefault().getPreferenceStore()
							.getInt(IPreferenceConstants.SAVE_AUTOMATICALLY_INTERVAL) * 1000, runnable);
				}
			}
		};
	}

	protected WorkbenchJob doCreateSaveJob() {
		return new WorkbenchJob("Auto save parts") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (WorkbenchPlugin.getDefault().getPreferenceStore()
						.getBoolean(IPreferenceConstants.SAVE_AUTOMATICALLY)) {
					// We do not want to save dirty editors when a sub shell is
					// active (e.g. content assist, javadoc hover...)
					if (activePage != null && !hasSubShellActive()) {
						activePage.saveAllEditors(false);
					}
				}
				return Status.OK_STATUS;
			}
		};
	}

	@PreDestroy
	private void shutdown() {
		if (saveJob != null) {
			saveJob.cancel();
		}
		if (idleListener == null) {
			return;
		}
		if (display != null && !display.isDisposed()) {
			try {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						display.timerExec(-1, runnable);
						display.removeFilter(SWT.KeyUp, idleListener);
						display.removeFilter(SWT.MouseUp, idleListener);
					}
				});
			} catch (SWTException ex) {
				// ignore (display might be disposed)
			}
		}
	}

	/**
	 * Check that the active shell has a sub shell visible and enabled. This is
	 * especially the case when the content assist is enabled or the javadoc on
	 * mouse hover is enabled.
	 *
	 * @return true the active shell has a sub shell visible and enabled, false
	 *         otherwise.
	 */
	private boolean hasSubShellActive() {
		Shell shell = Display.getCurrent().getActiveShell();
		if (shell != null && shell.isVisible() && shell.isEnabled()) {
			for (Shell subShell : shell.getShells()) {
				if (subShell.isVisible() && subShell.isEnabled()) {
					return true;
				}
			}
		}
		return false;
	}
}
