/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc. and other
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.navigator.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * @since 3.4.800
 *
 */
public class ProjectInWorkingSetDropAdapterAssistant extends CommonDropAdapterAssistant {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		if (target instanceof IAdaptable) {
			if (((IAdaptable)target).getAdapter(IWorkingSet.class) == null) {
				return Status.CANCEL_STATUS;
			}
		}
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
			if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
				for (Object item : ((IStructuredSelection)sel).toArray()) {
					if (! (item instanceof IAdaptable && ((IAdaptable)item).getAdapter(IProject.class) != null)){
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		}
		return Status.CANCEL_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet targetWorkingSet = (IWorkingSet) ((IAdaptable)aTarget).getAdapter(IWorkingSet.class);
		ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
		if (sel instanceof TreeSelection) {
			for (TreePath path : ((ITreeSelection)sel).getPaths()) {
				if (path.getFirstSegment() instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable)path.getLastSegment()).getAdapter(IProject.class);
					if (! workingSetContains(targetWorkingSet, project)) {
						workingSetManager.addToWorkingSets(project, new IWorkingSet[] { targetWorkingSet });
						// TODO Only perform this operation when Ctrl isn't pressed
						IWorkingSet sourceWorkingSet = (IWorkingSet) ((IAdaptable)path.getFirstSegment()).getAdapter(IWorkingSet.class);
						IAdaptable[] srcElements = sourceWorkingSet.getElements();
						List<IAdaptable> newSrcElements = new ArrayList<IAdaptable>();
						for (IAdaptable srcElement : srcElements) {
							if (!project.equals(srcElement.getAdapter(IProject.class))) {
								newSrcElements.add(srcElement);
							}
						}
						IAdaptable[] adaptedNewSrcElements = sourceWorkingSet.adaptElements(newSrcElements.toArray(new IAdaptable[newSrcElements.size()]));
						sourceWorkingSet.setElements(adaptedNewSrcElements);
					}
				}
			}
		} else if (sel instanceof IStructuredSelection) {
			for (Object item : ((IStructuredSelection)sel).toArray()) {
				IProject project = (IProject) ((IAdaptable)item).getAdapter(IProject.class);
				if (! workingSetContains(targetWorkingSet, project)) {
					workingSetManager.addToWorkingSets(project, new IWorkingSet[] { targetWorkingSet });
				}
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * @param targetWorkingSet
	 * @param project
	 * @return see obvious method name
	 */
	private boolean workingSetContains(IWorkingSet targetWorkingSet, IProject project) {
		for (IAdaptable element : targetWorkingSet.getElements()) {
			if (project.equals(element.getAdapter(IProject.class))) {
				return true;
			}
		}
		return false;
	}

}
