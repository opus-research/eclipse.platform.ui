/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.AbstractControlSelectionBackgroundCustomizationElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

/**
 * A handler which will set the selection/hot background and border colors.
 */
public class CSSPropertySelectionBackgroundHandler implements ICSSPropertyHandler {

	private static final String SWT_SELECTION_BACKGROUND_COLOR = "swt-selection-background-color"; //$NON-NLS-1$
	private static final String SWT_SELECTION_BORDER_COLOR = "swt-selection-border-color"; //$NON-NLS-1$

	private static final String SWT_HOT_BACKGROUND_COLOR = "swt-hot-background-color"; //$NON-NLS-1$
	private static final String SWT_HOT_BORDER_COLOR = "swt-hot-border-color"; //$NON-NLS-1$

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (element instanceof AbstractControlSelectionBackgroundCustomizationElement) {
			AbstractControlSelectionBackgroundCustomizationElement treeElement = (AbstractControlSelectionBackgroundCustomizationElement) element;
			Control control = treeElement.getControl();

			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				if (SWT_SELECTION_BACKGROUND_COLOR.equals(property)) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					treeElement.setSelectionBackgroundColor(newColor);
				} else if (SWT_SELECTION_BORDER_COLOR.equals(property)) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					treeElement.setSelectionBorderColor(newColor);
				} else if (SWT_HOT_BACKGROUND_COLOR.equals(property)) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					treeElement.setHotBackgroundColor(newColor);
				} else if (SWT_HOT_BORDER_COLOR.equals(property)) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					treeElement.setHotBorderColor(newColor);
				}
			}
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
