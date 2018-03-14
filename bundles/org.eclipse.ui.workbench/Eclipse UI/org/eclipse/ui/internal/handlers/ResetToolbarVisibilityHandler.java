/*******************************************************************************
 * Copyright (c) 2014 vogella GmbHand others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Handler that resets the visibility of the toolbars in a given window.
 * 
 * @since 4.4
 */
public class ResetToolbarVisibilityHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean toolbarVisible = false;
		// Remember the visibility of the toolbar
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		if (activeWorkbenchWindow instanceof WorkbenchWindow) {
			WorkbenchWindow workbenchWindow = (WorkbenchWindow) activeWorkbenchWindow;
			toolbarVisible = workbenchWindow.isToolbarVisible();
			// turn the toolbar on for this operation
			if (!toolbarVisible) {
				workbenchWindow.toggleToolbarVisibility();
			}

			MTrimmedWindow window = (MTrimmedWindow) activeWorkbenchWindow
					.getService(MTrimmedWindow.class);
			if (window != null) {

				for (MTrimBar tb : window.getTrimBars()) {
					for (MTrimElement te : tb.getChildren()) {
						if (!te.isToBeRendered()) {
							te.setToBeRendered(true);
						}
					}
				}
			}
			// if initially hidden, hide it again
			if (!toolbarVisible) {
				workbenchWindow.toggleToolbarVisibility();
			}
		}

		return null;
	}

}
