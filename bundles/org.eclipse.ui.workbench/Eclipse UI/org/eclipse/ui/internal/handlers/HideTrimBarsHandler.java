/*******************************************************************************
 * Copyright (c) 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * CodeFocus handler - see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=427999
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class HideTrimBarsHandler extends AbstractHandler {

	/**
	 *
	 */
	private static final String INITIAL_TRIM_VISIBILIY = "initialTrimVisibility"; //$NON-NLS-1$
	private static final String WINDOWS_WITH_MINIMIZED_TRIMBARS = "windowsWithMinimizedTrimbars"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MTrimmedWindow winModel = window.getService(MTrimmedWindow.class);
		EModelService modelService = window.getService(EModelService.class);

		// ensure we have everything we need
		if ((winModel == null || modelService == null)) {
			return null;

		}
		if (winModel.getTags().contains(WINDOWS_WITH_MINIMIZED_TRIMBARS)) {
			winModel.getTags().remove(WINDOWS_WITH_MINIMIZED_TRIMBARS);
			disableCodeFocus(winModel, modelService);
		} else {
			enableCodeFocus(winModel, modelService);
			winModel.getTags().add(WINDOWS_WITH_MINIMIZED_TRIMBARS);
		}

		return null;
	}

	private void disableCodeFocus(MTrimmedWindow window, EModelService modelService) {

		List<MTrimBar> tcList = modelService.findElements(window, null, MTrimBar.class, null);
		for (MTrimBar tc : tcList) {
			boolean visible = true;
			String string = tc.getPersistedState().get(INITIAL_TRIM_VISIBILIY);
			if (string != null && string.length() > 0) {
				visible = Boolean.valueOf(tc.getPersistedState().get(INITIAL_TRIM_VISIBILIY));
				tc.getPersistedState().remove(INITIAL_TRIM_VISIBILIY);
			}
			tc.setVisible(visible);
		}
	}

	private void enableCodeFocus(MTrimmedWindow window, EModelService modelService) {
		List<MTrimBar> tcList = modelService.findElements(window, null, MTrimBar.class, null);
		for (MTrimBar tc : tcList) {
			// remember the visibility state in case some trmbars are already
			// not visible
			tc.getPersistedState().put(INITIAL_TRIM_VISIBILIY, String.valueOf(tc.isVisible()));
			tc.setVisible(false);
		}
	}

}
