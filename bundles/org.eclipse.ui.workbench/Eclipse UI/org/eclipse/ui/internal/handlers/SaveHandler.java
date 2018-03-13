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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.util.Util;

public class SaveHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) {
		IWorkbenchPart activePart = getActivePart();
		ISaveablePart saveable = getSaveableView();

		if (saveable != null) {
			WorkbenchPage workbenchPage;
			if (activePart != null) {
				workbenchPage = (WorkbenchPage) activePart.getSite().getPage();
			} else {
				workbenchPage = (WorkbenchPage) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
			}
			workbenchPage.saveSaveable(saveable, false, false);
		} else if (activePart instanceof IEditorPart) {
			IWorkbenchPage page = activePart.getSite().getPage();
			page.saveEditor((IEditorPart) activePart, false);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		ISaveablePart saveable = getSaveableView();
		if (saveable instanceof ISaveablesSource) {
			return SaveableHelper.needsSave((ISaveablesSource) saveable);
		}
		if (saveable != null) {
			return saveable.isDirty();
		}
		return false;
	}

	private ISaveablePart getSaveableView() {
		IWorkbenchPart part = getActivePart();
		ISaveablePart saveable = null;

		if (part instanceof IViewPart) {
			saveable = (ISaveablePart) Util.getAdapter(part, ISaveablePart.class);
		}
		if (saveable == null && part instanceof IEditorPart) {
			saveable = (IEditorPart) part;
		}
		return saveable;
	}

	protected IWorkbenchPart getActivePart() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
				.getActivePart();
	}
}
