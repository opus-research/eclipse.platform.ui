/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 4.5
 *
 */
public interface IQuickAccessProvider {

	/**
	 * Returns the unique ID of this provider.
	 * 
	 * @return the unique ID
	 */
	String getId();

	/**
	 * Returns the name of this provider to be displayed to the user.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the image descriptor for this provider.
	 * 
	 * @return the image descriptor, or null if not defined
	 */
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns the elements provided by this provider.
	 * 
	 * @return this provider's elements
	 */
	IQuickAccessElement[] getElements();

	IQuickAccessElement[] getElementsSorted();

	/**
	 * Returns the element for the given ID if available, or null if no matching
	 * element is available.
	 * 
	 * @param id
	 *            the ID of an element
	 * @return the element with the given ID, or null if not found.
	 */
	IQuickAccessElement getElementForId(String id);

	boolean isAlwaysPresent();

	void reset();

	/**
	 * @param context
	 */
	void setContext(IEclipseContext context);
}