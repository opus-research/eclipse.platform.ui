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

import java.util.Date;

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
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
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
				if (!(newValue instanceof Date)) {
					return;
				}
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				// initial case
				if (activePage == null) {
					activePage = page;
				}
				int interval = WorkbenchPlugin.getDefault().getPreferenceStore()
						.getInt(IPreferenceConstants.SAVE_AUTOMATICALLY_INTERVAL) * 1000;
				if (page != null && page.equals(activePage)) {
					saveJob.cancel();
					saveJob.schedule(interval);
				} else {
					try {
						// activate page has switched, wait until existing save
						// job finished than schedule new one
						saveJob.join();
						activePage = page;
						saveJob.schedule(interval);
					} catch (InterruptedException e) {
						IDEWorkbenchPlugin.log(e.getMessage());
					}
				}
			}
		}
	};

	@Inject
	IEventBroker eventBroker;

	private boolean initial = true;

	@Inject
	@Optional
	private void setAutoSave(
			@SuppressWarnings("restriction") @Preference(value = IPreferenceConstants.SAVE_AUTOMATICALLY, nodePath = "org.eclipse.ui.workbench") boolean autoSave) {
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
			eventBroker.subscribe(UIEvents.Dirtyable.TOPIC_LAST_MODIFIED, dirtyHandler);
			initial = false;
		} else {
			eventBroker.unsubscribe(dirtyHandler);
		}
	}

	/**
	 * Creates the job for saving the dirty editor
	 */
	public SaveAllDirtyPartsAddon() {
		saveJob = doCreateSaveJob();
	}

	protected WorkbenchJob doCreateSaveJob() {
		return new WorkbenchJob("Auto save parts") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (WorkbenchPlugin.getDefault().getPreferenceStore()
						.getBoolean(IPreferenceConstants.SAVE_AUTOMATICALLY)) {
					if (activePage == null) {
						return Status.CANCEL_STATUS;
					}
					activePage.saveAllEditors(false);
				}
				return Status.OK_STATUS;
			}
		};
	}
}
