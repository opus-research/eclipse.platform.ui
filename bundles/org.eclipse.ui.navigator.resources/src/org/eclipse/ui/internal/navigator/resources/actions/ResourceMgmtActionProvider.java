/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lucas Bullen (Red Hat Inc.) - Bug 522096 - "Close Projects" on working set
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.CloseUnrelatedProjectsAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * @since 3.2
 *
 */
public class ResourceMgmtActionProvider extends CommonActionProvider {

	private BuildAction buildAction;

	private OpenResourceAction openProjectAction;

	private CloseResourceAction closeProjectAction;

	private CloseUnrelatedProjectsAction closeUnrelatedProjectsAction;

	private RefreshAction refreshAction;

	private Shell shell;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		shell = aSite.getViewSite().getShell();
		makeActions();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), buildAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.OPEN_PROJECT.getId(), openProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT.getId(), closeProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_UNRELATED_PROJECTS.getId(), closeUnrelatedProjectsAction);
		updateActionBars();
	}

	/**
	 * Adds the build, open project, close project and refresh resource actions
	 * to the context menu.
	 * <p>
	 * The following conditions apply: build-only projects selected, auto build
	 * disabled, at least one builder present open project-only projects
	 * selected, at least one closed project close project-only projects
	 * selected, at least one open project refresh-no closed project selected
	 * </p>
	 * <p>
	 * Both the open project and close project action may be on the menu at the
	 * same time.
	 * </p>
	 * <p>
	 * No disabled action should be on the context menu.
	 * </p>
	 *
	 * @param menu
	 *            context menu to add actions to
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		List<IProject> openProjects = new ArrayList<>();
		List<IProject> closedProjects = new ArrayList<>();
		List<IProject> buildableProjects = new ArrayList<>();
		boolean containsNonproject = false;

		List<IProject> projects = selectionToResources(selection);
		Iterator<IProject> projectsIterator = projects.iterator();

		while (projectsIterator.hasNext()) {
			Object next = projectsIterator.next();
			IProject project = Adapters.adapt(next, IProject.class);

			if (project == null) {
				containsNonproject = true;
				continue;
			}
			if (project.isOpen()) {
				openProjects.add(project);
				if (hasBuilder(project)) {
					buildableProjects.add(project);
				}
			} else {
				closedProjects.add(project);
			}
		}
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding() && !buildableProjects.isEmpty()) {
			buildAction.selectionChanged(selection);
			if (buildableProjects.size() > 1) {
				buildAction.setText(IDEWorkbenchMessages.BuildAction_text_plural);
			} else {
				buildAction.setText(IDEWorkbenchMessages.BuildAction_text);
			}
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, buildAction);
		}
		if (closedProjects.isEmpty()) {
			refreshAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, refreshAction);
		}
		if (!containsNonproject && !closedProjects.isEmpty()) {
			if (closedProjects.size() > 1) {
				openProjectAction.setText(IDEWorkbenchMessages.OpenResourceAction_text_plural);
				openProjectAction.setToolTipText(IDEWorkbenchMessages.OpenResourceAction_toolTip_plural);
			} else {
				openProjectAction.setText(IDEWorkbenchMessages.OpenResourceAction_text);
				openProjectAction.setToolTipText(IDEWorkbenchMessages.OpenResourceAction_toolTip);
			}
			openProjectAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, openProjectAction);
		}
		if (!containsNonproject && !openProjects.isEmpty()) {
			if (openProjects.size() > 1) {
				closeProjectAction.setText(IDEWorkbenchMessages.CloseResourceAction_text_plural);
				closeProjectAction.setToolTipText(IDEWorkbenchMessages.CloseResourceAction_toolTip_plural);
			} else {
				closeProjectAction.setText(IDEWorkbenchMessages.CloseResourceAction_text);
				closeProjectAction.setToolTipText(IDEWorkbenchMessages.CloseResourceAction_toolTip);
			}
			closeProjectAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, closeProjectAction);
			closeUnrelatedProjectsAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, closeUnrelatedProjectsAction);
		}
	}

	public static List<IProject> selectionToResources(IStructuredSelection selection) {
		if (selection == null) {
			return Collections.emptyList();
		}
		List<IProject> resources = new ArrayList<>();
		Iterator<?> iter = selection.iterator();
		while (iter.hasNext()) {
			Object curr = iter.next();
			if (curr instanceof IWorkingSet) {
				IWorkingSet workingSet = (IWorkingSet) curr;
				if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty()) {
					continue;
				}
				IAdaptable[] elements = workingSet.getElements();
				for (IAdaptable element : elements) {
					IProject project = element.getAdapter(IProject.class);
					if (project != null) {
						resources.add(project);
					}
				}
			} else if (curr instanceof IAdaptable) {
				IProject resource = ((IAdaptable) curr).getAdapter(IProject.class);
				if (resource != null) {
					resources.add(resource);
				}
			}
		}
		return resources;
	}

	/**
	 * Returns whether there are builders configured on the given project.
	 *
	 * @return <code>true</code> if it has builders, <code>false</code> if not,
	 *         or if this could not be determined
	 */
	boolean hasBuilder(IProject project) {
		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0) {
				return true;
			}
		} catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed
			// or does not exist. Fall through to return false.
		}
		return false;
	}

	protected void makeActions() {
		IShellProvider sp = new IShellProvider() {
			@Override
			public Shell getShell() {
				return shell;
			}
		};

		openProjectAction = new OpenResourceAction(sp);

		closeProjectAction = new CloseResourceAction(sp);

		closeUnrelatedProjectsAction = new CloseUnrelatedProjectsAction(sp);

		refreshAction = new RefreshAction(sp) {
			@Override
			public void run() {
				final IStatus[] errorStatus = new IStatus[1];
				errorStatus[0] = Status.OK_STATUS;
				final WorkspaceModifyOperation op = (WorkspaceModifyOperation) createOperation(errorStatus);
				WorkspaceJob job = new WorkspaceJob("refresh") { //$NON-NLS-1$

					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						try {
							op.run(monitor);
							if (shell != null && !shell.isDisposed()) {
								shell.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										StructuredViewer viewer = getActionSite().getStructuredViewer();
										if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
											viewer.refresh();
										}
									}
								});
							}
						} catch (InvocationTargetException e) {
							String msg = NLS.bind(WorkbenchNavigatorMessages.ResourceMgmtActionProvider_logTitle, getClass().getName(), e.getTargetException());
							throw new CoreException(new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, IStatus.ERROR, msg, e.getTargetException()));
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						return errorStatus[0];
					}

				};
				ISchedulingRule rule = op.getRule();
				if (rule != null) {
					job.setRule(rule);
				}
				job.setUser(true);
				job.schedule();
			}
		};
		refreshAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/refresh_nav.png"));//$NON-NLS-1$
		refreshAction.setImageDescriptor(getImageDescriptor("elcl16/refresh_nav.png"));//$NON-NLS-1$
		refreshAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);

		buildAction = new BuildAction(sp, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildAction.setActionDefinitionId(IWorkbenchCommandConstants.PROJECT_BUILD_PROJECT);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		return IDEWorkbenchPlugin.getIDEImageDescriptor(relativePath);

	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeUnrelatedProjectsAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
	}

}
