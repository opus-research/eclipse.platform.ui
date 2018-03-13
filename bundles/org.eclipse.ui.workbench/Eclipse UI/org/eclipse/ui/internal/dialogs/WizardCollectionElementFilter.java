/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @since 3.5
 *
 */
public class WizardCollectionElementFilter {
	/**
	 * 
	 * @param viewer
	 * @param viewerFilter
	 * @param inputCollection
	 * @return If some of the wizards from the input collection is skipped by
	 *         the viewerFilter then the modified copy of the collection
	 *         (without skipped wizards) is returned. When all wizards are
	 *         skipped then null will be returned. If none of the wizards is
	 *         skipped during filtering then the original input collection is
	 *         returned
	 */
	public static WizardCollectionElement filter(Viewer viewer, ViewerFilter viewerFilter,
			WizardCollectionElement inputCollection) {
		WizardCollectionElement modifiedCollection = null;

		for (Object child : inputCollection.getWizardAdaptableList().getChildren()) {
			if (!viewerFilter.select(viewer, inputCollection, child)) {
				if (modifiedCollection == null) {
					modifiedCollection = (WizardCollectionElement) inputCollection.clone();
				}
				modifiedCollection.getWizardAdaptableList().remove((IAdaptable) child);
			}
		}

		if (modifiedCollection == null) {
			return inputCollection;
		}
		if (modifiedCollection.getWizardAdaptableList().size() == 0) {
			return null;
		}
		return modifiedCollection;
	}
}
