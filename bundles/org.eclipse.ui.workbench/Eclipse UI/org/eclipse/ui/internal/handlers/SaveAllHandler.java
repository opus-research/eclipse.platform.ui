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

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;

public class SaveAllHandler extends SaveHandler {
	public Object execute(ExecutionEvent event) {
		IWorkbenchPart activePart = getActivePart();
		WorkbenchPage workbenchPage;
		
		if (activePart != null) {
			workbenchPage = (WorkbenchPage) activePart.getSite().getPage();
		} else {
			workbenchPage = (WorkbenchPage) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
		}
		workbenchPage.saveAllEditors(false);
		return null;
	}
}
