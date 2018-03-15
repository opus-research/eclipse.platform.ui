/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc-Andre Laperle (Ericsson) - Bug 413278
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 497618, 368977, 502123
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.AbstractTableInformationControl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Shows a list of open editor and parts in the current or last active workbook.
 *
 * @since 3.4
 *
 */
public class WorkbookEditorsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MUIElement uiElement = null;

		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart != null) {
			IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			WorkbenchPage page = (WorkbenchPage) workbenchWindow.getActivePage();
			if (page != null) {
				IWorkbenchPartReference reference = page.getReference(activePart);
				if (reference != null) {
					uiElement = page.getActiveElement(reference);
				}
			}
		}

		if (uiElement instanceof MPlaceholder) {
			uiElement = ((MPlaceholder) uiElement).getRef();
		}

		Shell activeShell = HandlerUtil.getActiveShell(event);
		MPartStack activeStack = getActiveStack(uiElement);
		if (activeStack != null) {
			// Items list is not visible, to be displayed
			if (activeStack.getRenderer() instanceof StackRenderer
					&& activeStack.getWidget() instanceof CTabFolder) {
				StackRenderer stackRenderer = (StackRenderer) activeStack.getRenderer();
				stackRenderer.showAvailableItems(activeStack, (CTabFolder) activeStack.getWidget(), true);
			}
		} else if (activeShell != null && activeShell.getData() instanceof AbstractTableInformationControl) {
			// Items list is already displayed, we cycle through the list
			AbstractTableInformationControl atc = (AbstractTableInformationControl) activeShell.getData();
			atc.next();
		}
		return null;
	}

	private MPartStack getActiveStack(Object element) {
		if (element instanceof MPartStack) {
			return (MPartStack) element;
		} else if (element instanceof MElementContainer<?>) {
			return getActiveStack(((MElementContainer<?>) element).getSelectedElement());
		}
		return null;
	}

}
