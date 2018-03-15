/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.views.markers;


import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;


/**
 * The Problems view is supplied by the IDE to show problems.
 *
 * @since 3.4
 */
public class ProblemsView extends MarkerSupportView {
	Action filterAction;
	Viewer viewer;
	MarkerCategory category;

	/**
	 * Create a new instance of the receiver.
	 */
	public ProblemsView() {
		super(MarkerSupportRegistry.PROBLEMS_GENERATOR);
	}


	@Override
	void updateTitleImage(Integer[] counts) {
		Image image = WorkbenchPlugin.getDefault().getSharedImages()
				.getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW);
		if (counts[0].intValue() > 0) {
			image = WorkbenchPlugin.getDefault().getSharedImages()
					.getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR);
		} else if (counts[1].intValue() > 0) {
			image = WorkbenchPlugin.getDefault().getSharedImages()
					.getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING);
		} else if (counts[2].intValue() > 0) {
			image = WorkbenchPlugin.getDefault().getSharedImages()
					.getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_INFO);
		}
		setTitleImage(image);
	}

	@Override
	protected IUndoContext getUndoContext() {
		return WorkspaceUndoUtil.getProblemsUndoContext();
	}

	@Override
	protected String getDeleteOperationName(IMarker[] markers) {
		Assert.isLegal(markers.length > 0);
		return markers.length == 1 ? MarkerMessages.deleteProblemMarker_operationName : MarkerMessages.deleteProblemMarkers_operationName;
	}

	/**
	 * Create the Filter action for the receiver.
	 */
	private void createFilterAction() {
		filterAction = new Action(MarkerMessages.configureFiltersCommand_title) { // $NON-NLS-1$
			@Override
			public void run() {
				openFiltersDialog();
			}
		};
		filterAction.setToolTipText(MarkerMessages.configureFiltersCommand_title);// $NON-NLS-1$
		// TODO: currently using a dummy image, replace with new one
		ImageDescriptor id = WorkbenchImages.getWorkbenchImageDescriptor("/elcl16/progress_remall.png"); //$NON-NLS-1$
		if (id != null) {
			filterAction.setImageDescriptor(id);
		}
		id = WorkbenchImages.getWorkbenchImageDescriptor("/dlcl16/progress_remall.png"); //$NON-NLS-1$
		if (id != null) {
			filterAction.setDisabledImageDescriptor(id);
		}
	}


	/**
	 * Add additional actions to the tool bar.
	 */
	private void initToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager tm = bars.getToolBarManager();
		tm.add(new Separator("filterGroup")); //$NON-NLS-1$
		tm.add(filterAction);

	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		viewer = super.getViewer();
		createFilterAction();
		initToolBar();
	}

}