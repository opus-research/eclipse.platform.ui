/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 457364
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog;

/**
 * Opens the CustomizePerspectiveDialog to configure the available menu and
 * toolbar entries.
 * 
 */
public class EditActionSetsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		WorkbenchWindow activeWorkbenchWindow = (WorkbenchWindow) HandlerUtil.getActiveWorkbenchWindow(event);
		IEclipseContext ctx = (IEclipseContext) activeWorkbenchWindow.getService(IEclipseContext.class);
		MWindow mWindow = ctx.get(MWindow.class);
		if (activeWorkbenchWindow != null) {
			WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
			Perspective activePerspective = page.getActivePerspective();
			// Create and open CPD dialog.
			CustomizePerspectiveDialog dlg = activeWorkbenchWindow.createCustomizePerspectiveDialog(activePerspective,
					mWindow.getContext());
			if (dlg.open() == Window.OK) {
				activeWorkbenchWindow.updateActionSets();
				activeWorkbenchWindow.firePerspectiveChanged(page, page.getPerspective(), IWorkbenchPage.CHANGE_RESET);
				activeWorkbenchWindow.firePerspectiveChanged(page, page.getPerspective(), IWorkbenchPage.CHANGE_RESET);
			}
		}
		return null;
	}
}
