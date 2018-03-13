/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kevin Milburn - [Bug 423214] [PropertiesView] add support for IColorProvider and IFontProvider
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Extension to the standard <code>IPropertySheetEntry2</code> interface.
 * <p>
 * This interface provides extended API to <code>IPropertySheetEntry</code> to
 * allow additional styling of property sheet entries
 * </p>
 *
 * @since 4.4
 * @see org.eclipse.ui.views.properties.IPropertySheetEntry
 */
public interface IPropertySheetEntry2 extends IPropertySheetEntry {

	/**
	 * Provides a foreground color for the entry if the supplied
	 * Label Provider is an instance of {@link IColorProvider}.
	 *
	 * @return	the foreground color for the entry, or <code>null</code>
	 *   to use the default foreground color
	 */
	Color getForeground();

	/**
	 * Provides a background color for the entry if the supplied
	 * Label Provider is an instance of {@link IColorProvider}.
	 *
	 * @return	the background color for the rntry, or <code>null</code>
	 *   to use the default background color
	 */
	Color getBackground();

	/**
	 * Provides a font for the entry if the supplied
	 * Label Provider is an instance of {@link IFontProvider}.
	 *
	 * @return the font for the entry, or <code>null</code>
	 *   to use the default font
	 */
	Font getFont();

}
