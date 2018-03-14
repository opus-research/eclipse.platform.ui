/*******************************************************************************
 * Copyright (c) 2014-2015 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.workingsets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.dnd.DND;
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
		if (operation != DND.DROP_MOVE) {
			return Status.CANCEL_STATUS;
		}

		IWorkingSet targetWorkingSet = null;
		if (target instanceof IAdaptable) {
			targetWorkingSet = (IWorkingSet) ((IAdaptable)target).getAdapter(IWorkingSet.class);
		}
		if (targetWorkingSet == null) {
			return Status.CANCEL_STATUS;
		}

		Set<IProject> toMove = new HashSet<IProject>();
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
			if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
				for (Object item : ((IStructuredSelection)sel).toArray()) {
					if (item instanceof IAdaptable) {
						IProject project = null;
						if (item instanceof IAdaptable) {
							project = (IProject) ((IAdaptable)item).getAdapter(IProject.class);
						}
						if (project != null && !workingSetContains(targetWorkingSet, project)) {
							toMove.add(project);
						}
					}
				}
			}
		}
		if (toMove.isEmpty()) {
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet targetWorkingSet = (IWorkingSet) ((IAdaptable)aTarget).getAdapter(IWorkingSet.class);
		ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
		if (sel instanceof ITreeSelection) {
			for (TreePath path : ((ITreeSelection)sel).getPaths()) {
				IProject project = (IProject) ((IAdaptable) path.getLastSegment()).getAdapter(IProject.class);
				if (path.getFirstSegment() instanceof IAdaptable) {
					IWorkingSet sourceWorkingSet = (IWorkingSet) ((IAdaptable)path.getFirstSegment()).getAdapter(IWorkingSet.class);
					if (! targetWorkingSet.equals(sourceWorkingSet)) {
						if (! workingSetContains(targetWorkingSet, project)) {
							workingSetManager.addToWorkingSets(project, new IWorkingSet[] { targetWorkingSet });
						}
						if (sourceWorkingSet != null) {
							removeFromWorkingSet(project, sourceWorkingSet);
						}
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
	 * @param project
	 * @param sourceWorkingSet
	 */
	private void removeFromWorkingSet(IProject project, IWorkingSet sourceWorkingSet) {
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
