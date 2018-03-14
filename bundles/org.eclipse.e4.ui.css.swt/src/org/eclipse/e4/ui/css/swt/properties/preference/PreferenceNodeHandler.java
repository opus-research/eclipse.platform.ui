/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.preference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.preference.PreferenceNodeElement;
import org.eclipse.e4.ui.internal.css.swt.preference.IPreferenceNodeOverridable;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class PreferenceNodeHandler implements ICSSPropertyHandler {
	private final static String PREFERENCES_PROP = "preferences";

	private final static Pattern PROPERTY_NAME_AND_VALUE_PATTERN = Pattern
			.compile("(.+)\\s*=\\s*(.*)");

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!property.equals(PREFERENCES_PROP)
				|| !(element instanceof PreferenceNodeElement)) {
			return false;
		}

		IPreferenceNodeOverridable preferenceNode = (IPreferenceNodeOverridable) ((PreferenceNodeElement) element)
				.getNativeWidget();
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList list = (CSSValueList) value;
			for (int i = 0; i < list.getLength(); i++) {
				overrideProperty(preferenceNode, list.item(i));
			}
		} else {
			overrideProperty(preferenceNode, value);
		}

		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	private void overrideProperty(IPreferenceNodeOverridable preferenceNode,
			CSSValue value) {
		Matcher matcher = PROPERTY_NAME_AND_VALUE_PATTERN.matcher(value
				.getCssText());
		if (matcher.find()) {
			preferenceNode.overridePreference(matcher.group(1).trim(), matcher
					.group(2).trim());
		}
	}
}
