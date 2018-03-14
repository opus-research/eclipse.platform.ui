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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.quickaccess.QuickAccessEntry;

/**
 * @since 4.5
 *
 */
public interface IQuickAccessElement {

	/**
	 * Returns the label to be displayed to the user.
	 * 
	 * @return the label
	 */
	String getLabel();

	/**
	 * Returns the image descriptor for this element.
	 * 
	 * @return an image descriptor, or null if no image is available
	 */
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns the id for this element. The id has to be unique within the
	 * QuickAccessProvider that provided this element.
	 * 
	 * @return the id
	 */
	String getId();

	/**
	 * Executes the associated action for this element.
	 */
	void execute();

	/**
	 * Return the label to be used for sorting elements.
	 * 
	 * @return the sort label
	 */
	String getSortLabel();

	/**
	 * @return Returns the provider.
	 */
	IQuickAccessProvider getProvider();

	/**
	 * If this element is a match (partial, complete, camel case, etc) to the
	 * given filter, returns a {@link QuickAccessEntry}. Otherwise returns
	 * <code>null</code>;
	 * 
	 * @param filter
	 *            filter for matching
	 * @param providerForMatching
	 *            the provider that will own the entry
	 * @return a quick access entry or <code>null</code>
	 */
	QuickAccessMatch match(String filter, IQuickAccessProvider providerForMatching);

}