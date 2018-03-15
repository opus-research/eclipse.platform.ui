/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;

public class NestedProjectsContentProvider implements ITreeContentProvider, IResourceChangeListener {

	public static final String EXTENSION_ID = "org.eclipse.ui.navigator.resources.nested.nestedProjectContentProvider"; //$NON-NLS-1$

	private Command projectPresetionCommand;
	private CommonViewer viewer;
	private Set<IContainer> parentsToRefresh = new HashSet<IContainer>();

	public NestedProjectsContentProvider() {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		this.projectPresetionCommand = commandService.getCommand(ProjectPresentationHandler.COMMAND_ID);
		try {
			HandlerUtil.updateRadioState(this.projectPresetionCommand, Boolean.TRUE.toString());
		} catch (ExecutionException ex) {
			WorkbenchNavigatorPlugin.log(ex.getMessage(), new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, ex.getMessage(), ex));
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
				IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void dispose() {
		try {
			HandlerUtil.updateRadioState(this.projectPresetionCommand, Boolean.FALSE.toString());
		} catch (ExecutionException ex) {
			WorkbenchNavigatorPlugin.log(ex.getMessage(), new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, ex.getMessage(), ex));
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (CommonViewer)viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return null;
	}

	@Override
	public IProject[] getChildren(Object parentElement) {
		if (! (parentElement instanceof IContainer)) {
			return null;
		}
		IContainer container = (IContainer)parentElement;
		return NestedProjectManager.getInstance().getDirectChildrenProjects(container);
	}

	@Override
	public IContainer getParent(Object element) {
		if (element instanceof IProject) {
			IProject project = (IProject)element;
			if (NestedProjectManager.getInstance().isShownAsNested(project)) {
				return NestedProjectManager.getInstance().getMostDirectOpenContainer(project);
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer) {
			return NestedProjectManager.getInstance().hasDirectChildrenProjects((IContainer) element);
		}
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResource resource = event.getResource();
		if (event.getType() == IResourceChangeEvent.PRE_DELETE && resource != null
				&& resource.getType() == IResource.PROJECT) {
			IProject aboutToBeDeleted = (IProject) resource;
			IContainer parent = getParent(aboutToBeDeleted);
			if (parent != null) {
				parentsToRefresh.add(parent);
			}
		} else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				resource = delta.getResource();
				if (delta.getKind() == IResourceDelta.CHANGED && resource != null
						&& resource.getType() == IResource.ROOT) {
					for (IResourceDelta childDelta : event.getDelta().getAffectedChildren()) {
						IResource childResource = childDelta.getResource();
						if (childResource != null && childResource.getType() == IResource.PROJECT
								&& childDelta.getKind() == IResourceDelta.ADDED) {
							IProject newProject = (IProject) childResource;
							IContainer parent = getParent(newProject);
							if (parent != null) {
								parentsToRefresh.add(parent);
							}
						}
					}
				}
			}
			if (!parentsToRefresh.isEmpty()) {
				this.viewer.getTree().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						for (IContainer parent : parentsToRefresh) {
							NestedProjectsContentProvider.this.viewer.refresh(parent);
						}
						parentsToRefresh.clear();
					}
				});
			}
		}
	}

}
