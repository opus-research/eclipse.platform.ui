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
package org.eclipse.ui.internal.navigator.filters.incremental;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * This handler checks if the active part is a {@link CommonNavigator}. If this
 * is the case, the {@link IncrementalFilterContribution} for that viewer is
 * activated and focused.
 *
 * @author Stefan Winkler <stefan@winklerweb.net>
 * @since 3.3
 */
public class ActivateIncrementalResourceFilterHandler extends AbstractHandler implements IHandler {

	/**
	 * Activate the incremental filter text field in the status line for the
	 * currently active {@link CommonNavigator}. If the currently active part is
	 * not a {@link CommonNavigator}, this handler does nothing
	 *
	 * @param event
	 *            the execution event
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof CommonNavigator) {
			CommonNavigator commonNavigator = (CommonNavigator) activePart;
			IncrementalFilterContribution.addOrActivate(commonNavigator);
		}

		return null;
	}
}
