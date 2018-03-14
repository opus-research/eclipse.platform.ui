package org.eclipse.ui.internal.ide.addons;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.service.event.Event;

/**
 * Model add-on for automatic save of dirty editors.
 */
public class SaveAllDirtyEditorsAddon {
	@Inject
	@Optional
	IWorkbenchWindow windows;
	private IWorkbenchPage activePage;

	@Inject
	@Optional
	MWindow window;
	private WorkbenchJob doCreateSaveJob;

	/**
	 * Creates the job for saving the dirty editor
	 */
	public SaveAllDirtyEditorsAddon() {
		doCreateSaveJob = doCreateSaveJob();
	}

	@Inject
	@Optional
	private void subscribeDirtyChanged(@EventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event event) {
		Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
		
		if (!(newValue instanceof Boolean)) {
			return;
		}
		boolean dirty = (Boolean) newValue;
		if (!dirty) {
			return;
		}
		activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			doCreateSaveJob.cancel();
			doCreateSaveJob.schedule(400);
		} 
	}

	protected WorkbenchJob doCreateSaveJob() {
		return new WorkbenchJob("Auto Save editors") { //$NON-NLS-1$

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
