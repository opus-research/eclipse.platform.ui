/*******************************************************************************
 * Copyright (c) 2017 Stefan Winkler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Winkler - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.filters.search;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * This handler checks if the active part is a {@link CommonNavigator}. If this
 * is the case, the tree search filter text field for that viewer is shown and
 * focused.
 *
 * @author Stefan Winkler <stefan@winklerweb.net>
 * @since 3.7
 */
public class ActivateCommonNavigatorSearchFilterHandler extends AbstractHandler implements IHandler {

	/**
	 * Activate the search filter text field for the currently active
	 * {@link CommonNavigator}. If the currently active part is not a
	 * {@link CommonNavigator}, this handler does nothing
	 *
	 * @param event
	 *            the execution event
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof CommonNavigator) {
			CommonNavigator navigator = (CommonNavigator) activePart;
			CommonNavigatorSearchFilterHelper.getInstance().activateFilter(navigator.getCommonViewer());
		}

		return null;
	}
}
