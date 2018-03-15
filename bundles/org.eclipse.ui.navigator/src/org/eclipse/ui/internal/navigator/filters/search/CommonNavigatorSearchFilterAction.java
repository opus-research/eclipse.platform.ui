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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.navigator.CommonNavigatorActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * The find button in the {@link CommonNavigator}'s tool bar. This is realized
 * as an {@link Action} instead of a command/toolbar contribution, because a
 * developer shall have the possibility to hide this button from a
 * {@link CommonNavigator}. This can be done by setting the descriptor property
 * "org.eclipse.ui.navigator.hideSearchFilterAction" to <code>true</code>.
 *
 * @see CommonNavigatorActionGroup
 * @since 3.7
 */
public class CommonNavigatorSearchFilterAction extends Action {

	/**
	 * The {@link CommonViewer} this action is responsible for
	 */
	private CommonViewer commonViewer;

	/**
	 * Create the action for the given {@link CommonViewer}
	 *
	 * @param commonViewer
	 *            The {@link CommonViewer} this action shall be responsible for
	 */
	public CommonNavigatorSearchFilterAction(CommonViewer commonViewer) {
		this.commonViewer = commonViewer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		CommonNavigatorSearchFilterHelper.getInstance().activateFilter(commonViewer);
	}
}
