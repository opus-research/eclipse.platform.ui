/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     René Brandstetter - Bug 404231 - resetPerspectiveModel() does not reset the perspective
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * A service which will be used by EModelService implementation to reload a perspective so it can be
 * restored to its original state.
 */
public interface EPerspectiveRestoreService {

	/**
	 * Reloads a perspective state from a persistence storage.
	 * 
	 * @param perspectiveID
	 *            the ID of the perspective to reload
	 * @param window
	 *            the window which requested the perspective reload
	 * @return a newly reloaded {@link MPerspective} object for the given perspectiveID, or
	 *         <code>null</code> if no {@link MPerspective} for the given perspectiveID can be found
	 */
	public MPerspective reloadPerspective(String perspectiveID, MWindow window);
}
