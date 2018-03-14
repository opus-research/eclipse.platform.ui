/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wayne Beaton (The Eclipse Foundation) - Bug 162006
 *******************************************************************************/
package org.eclipse.ui.quickaccess;

import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * Implementors of this interface provide entries for the quick access (Ctrl+3)
 * dialog. Instances are generally created via the
 * "org.eclipse.ui.workbench.quickAccess" extension point; immediately after
 * creation, instances are provided the Eclipse context via the
 * {@link #setContext(IEclipseContext)} method. This method is called exactly
 * once.
 *
 * TODO This documentation is pitiful.
 *
 * @since 3.109
 *
 */
public interface IQuickAccessProvider {
	/**
	 * Returns the elements provided by this provider.
	 *
	 * @return this provider's elements
	 */
	IQuickAccessElement[] getElements();

	/**
	 * Returns the element for the given ID if available, or null if no matching
	 * element is available.
	 *
	 * @param id
	 *            the ID of an element
	 * @return the element with the given ID, or null if not found.
	 */
	IQuickAccessElement getElementForId(String id);

	void reset();

	/**
	 * @param context
	 */
	void setContext(IEclipseContext context);
}