/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.css;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;
import org.w3c.dom.css.CSSValue;

public class TabbedPropertyTitleCssPropertyHandler extends AbstractCSSPropertySWTHandler
		implements ICSSPropertyHandler {

	private static final String H_GRADIENT_START = "h-gradient-start-color"; //$NON-NLS-1$
	private static final String H_GRADIENT_END = "h-gradient-end-color"; //$NON-NLS-1$
	private static final String H_BOTTOM_KEYLINE_1 = "h-bottom-keyline-1-color"; //$NON-NLS-1$
	private static final String H_BOTTOM_KEYLINE_2 = "h-bottom-keyline-2-color"; //$NON-NLS-1$

	private static final Map<String, String> cssPropertyToSWTProperty = new HashMap<>();

	static {
		cssPropertyToSWTProperty.put(H_BOTTOM_KEYLINE_1, IFormColors.H_BOTTOM_KEYLINE1);
		cssPropertyToSWTProperty.put(H_BOTTOM_KEYLINE_2, IFormColors.H_BOTTOM_KEYLINE2);
		cssPropertyToSWTProperty.put(H_GRADIENT_START, IFormColors.H_GRADIENT_START);
		cssPropertyToSWTProperty.put(H_GRADIENT_END, IFormColors.H_GRADIENT_END);
	}

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof TabbedPropertyTitle)) {
			return;
		}
		TabbedPropertyTitle title = (TabbedPropertyTitle) control;

		String swtProperty = cssPropertyToSWTProperty.get(property);
		if (swtProperty != null) {
			if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				RGBA rgba = CSSSWTColorHelper.getRGBA(value);
				title.setColor(swtProperty, rgba);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
