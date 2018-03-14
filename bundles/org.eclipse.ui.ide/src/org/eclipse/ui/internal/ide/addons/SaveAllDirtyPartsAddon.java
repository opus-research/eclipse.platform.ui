/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.addons;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Model add-on for automatic save of dirty editors.
 */
public class SaveAllDirtyPartsAddon {

	private WorkbenchJob doCreateSaveJob;

	private IWorkbenchPage activePage;

	private static final int SAVE_INTERVALL = 800;

	EventHandler dirtyHandler = new EventHandler() {

		@Override
		public void handleEvent(Event event) {
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
				doCreateSaveJob.cancel();
				doCreateSaveJob.schedule(SAVE_INTERVALL);
			} else {
				try {
					// activate page has switched, wait until existing save job
					// finished than schedule new one
					doCreateSaveJob.join();
					activePage = page;
					doCreateSaveJob.schedule(SAVE_INTERVALL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	};

	@Inject
	IEventBroker eventBroker;

	private boolean initial = true;

	@Inject
	@Optional
	private void getPreference(
			@SuppressWarnings("restriction") @Preference(value = IDEInternalPreferences.SAVE_AUTOMATICALLY) boolean autoSave) {
		if (autoSave) {
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
		}
	}

	/**
	 * Creates the job for saving the dirty editor
	 */
	public SaveAllDirtyPartsAddon() {
		doCreateSaveJob = doCreateSaveJob();
	}

	protected WorkbenchJob doCreateSaveJob() {
		return new WorkbenchJob("Auto save parts") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (activePage == null) {
					return Status.CANCEL_STATUS;
				}
				activePage.saveAllEditors(false);
				return Status.OK_STATUS;
			}
		};
	}
}
