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
package org.eclipse.e4.ui.css.swt.helpers;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class IEclipsePreferencesHelper {
	private final static String PROPS_OVERRIDDEN_BY_CSS_PROP = "overriddenByCSS";

	private final static String SEPARATOR = ",";

	private final static String MULTI_VALUE_FORMATTER = "%s" + SEPARATOR + "%s";

	public static void appendOverriddenPropertyName(
			IEclipsePreferences preferences, String name) {
		String value = preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, "");
		if (!value.contains(name)) {
			preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP,
					value.length() == 0 ? name : String.format(MULTI_VALUE_FORMATTER, value, name));
		}
	}

	public static String[] getOverriddenPropertyNames(
			IEclipsePreferences preferences) {
		String value = preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, null);
		return value == null ? new String[0] : value.split(SEPARATOR);
	}

	public static void removeOverriddenPropertyNames(
			IEclipsePreferences preferences) {
		preferences.remove(PROPS_OVERRIDDEN_BY_CSS_PROP);
	}
}
