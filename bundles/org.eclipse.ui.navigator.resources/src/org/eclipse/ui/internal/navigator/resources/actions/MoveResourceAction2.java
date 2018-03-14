/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.actions.MoveProjectAction;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Move resource action that also handles projects.
 */
public class MoveResourceAction2 extends BaseSelectionListenerAction {

	/**
	 * Default move resource action
	 */
	protected final MoveResourceAction moveResourceAction;

	/**
	 * Move resource delegate if resource is a project.
	 */
	protected final MoveProjectAction moveProjectAction;

	public MoveResourceAction2(IShellProvider provider) {
		super(IDEWorkbenchMessages.MoveResourceAction_text);
		moveResourceAction = new MoveResourceAction(provider);
		moveProjectAction = new MoveProjectAction(provider);

		setToolTipText(IDEWorkbenchMessages.MoveResourceAction_toolTip);
		setId(MoveResourceAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.MOVE_RESOURCE_ACTION);
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		moveProjectAction.selectionChanged(getStructuredSelection());

		if (moveProjectAction.isEnabled()) {
			return true;
		}

		moveResourceAction.selectionChanged(getStructuredSelection());
		return moveResourceAction.isEnabled();
	}

	@Override
	public void run() {
		if (moveProjectAction.isEnabled()) {
			moveProjectAction.run();
			return;
		}
		moveResourceAction.run();
	}

}