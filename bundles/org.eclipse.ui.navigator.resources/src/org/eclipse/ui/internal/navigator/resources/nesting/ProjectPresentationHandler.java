/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nesting;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.CommonNavigator;

public class ProjectPresentationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.matchesRadioState(event)) {
			return null;
		}

		boolean previousNest = NestedProjectManager.isNestingEnabled();
		String newNestParam = event.getParameter(NestedProjectManager.NEST_PARAMETER);
		boolean newNest = false;
		if (newNestParam != null) {
			newNest = Boolean.parseBoolean(newNestParam);
		}
		if (newNest != previousNest) {
			WorkbenchNavigatorPlugin.getDefault().getPreferenceStore().setValue(NestedProjectManager.NEST_PARAMETER, newNest);
			((CommonNavigator)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()).getCommonViewer().refresh();
		}

		HandlerUtil.updateRadioState(event.getCommand(), Boolean.toString(newNest));
		// TODO refresh selection
		return Boolean.valueOf(newNest);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
