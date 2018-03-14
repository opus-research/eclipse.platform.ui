package org.eclipse.ui.internal.ide.addons;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;

public class SaveAllDirtyEditorsAddon {
	/**
 * 
 */
	public SaveAllDirtyEditorsAddon() {
		System.out.println("SaveAllDirtyEditorsAddon"); //$NON-NLS-1$
	}

	@Inject
	@Optional
	public void subscribeDirtyChanged(@EventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event event) {
		System.out.println("Dirty editor"); ////$NON-NLS-1$
	}

}
