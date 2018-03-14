/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * Internal class, do not use.
 * 
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ResourceTreeAndListGroup extends org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup {

	/**
	 * @param parent
	 * @param rootObject
	 * @param treeContentProvider
	 * @param treeLabelProvider
	 * @param listContentProvider
	 * @param listLabelProvider
	 * @param style
	 * @param useHeightHint
	 */
	public ResourceTreeAndListGroup(Composite parent, Object rootObject,
			ITreeContentProvider treeContentProvider,
			ILabelProvider treeLabelProvider,
			IStructuredContentProvider listContentProvider,
			ILabelProvider listLabelProvider, int style, boolean useHeightHint) {
		super(parent, rootObject, treeContentProvider, treeLabelProvider,
				listContentProvider, listLabelProvider, style, useHeightHint);
	}

	/**
	 * @param filter
	 * @param monitor
	 * @throws InterruptedException
	 */
	public void getAllCheckedListItems(org.eclipse.ui.internal.ide.dialogs.IElementFilter filter, IProgressMonitor monitor) throws InterruptedException {
		getAllCheckedListItems(filter, monitor);
	}
}
