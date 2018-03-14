/*******************************************************************************
 * Copyright (c) 2014-2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 459938
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.preference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.preference.EclipsePreferencesElement;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class EclipsePreferencesHandler implements ICSSPropertyHandler {
	public final static String PREFERENCES_PROP = "preferences";
	public final static String DEFAULT_PREFERENCES_PROP = "default-preferences";
	private final static String INITIAL_INSTANCE_PREFERENCES_PATH = "/" + InstanceScope.SCOPE + "/";

	private final static Pattern PROPERTY_NAME_AND_VALUE_PATTERN = Pattern
			.compile("(.+)\\s*=\\s*(.*)");

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		boolean isDefaultPreferencesProp = property.equals(DEFAULT_PREFERENCES_PROP);
		boolean isRegularPreferencesProp = property.equals(PREFERENCES_PROP);

		if (!(isRegularPreferencesProp || isDefaultPreferencesProp)
				|| !(element instanceof EclipsePreferencesElement)) {
			return false;
		}

		IEclipsePreferences preferences = (IEclipsePreferences) ((EclipsePreferencesElement) element)
				.getNativeWidget();

		boolean force = false;
		if (isDefaultPreferencesProp) {
			String absolutePath = preferences.absolutePath();
			if (absolutePath.indexOf(INITIAL_INSTANCE_PREFERENCES_PATH) == 0) {
				// By default we receive the preferences from the instance
				// scope, but let's just make sure that's really what we get...
				// (i.e.: this is how
				// org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine.StylingPreferencesHandler.getPreferences()
				// gives us the preferences)
				String path = absolutePath.substring(INITIAL_INSTANCE_PREFERENCES_PATH.length());
				preferences = DefaultScope.INSTANCE.getNode(path);

				// When we're dealing with the default scope, we always
				// override previous values.
				force = true;
			}
		}

		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList list = (CSSValueList) value;
			int length = list.getLength();
			for (int i = 0; i < length; i++) {
				overrideProperty(preferences, list.item(i), force);
			}
		} else {
			overrideProperty(preferences, value, force);
		}

		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	protected void overrideProperty(IEclipsePreferences preferences,
			CSSValue value, boolean force) {
		Matcher matcher = PROPERTY_NAME_AND_VALUE_PATTERN.matcher(value
				.getCssText());
		if (matcher.find()) {
			overrideProperty(preferences, matcher.group(1).trim(), matcher.group(2).trim(), force);
		}
	}

	protected void overrideProperty(IEclipsePreferences preferences,
			String name, String value, boolean force) {
		if (force || preferences.get(name, null) == null || EclipsePreferencesHelper.isThemeChanged()) {
			preferences.put(name, value);
			EclipsePreferencesHelper.appendOverriddenPropertyName(preferences,
					name);
		}
	}
}
