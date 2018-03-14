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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class EclipsePreferencesHelper {
	private final static String PROPS_OVERRIDDEN_BY_CSS_PROP = "overriddenByCSS";

	private final static String SEPARATOR = ",";

	private final static String VALUE_FORMATTER = "'%s'";

	private final static String MULTI_VALUE_FORMATTER = "%s" + SEPARATOR
			+ VALUE_FORMATTER;

	public static void appendOverriddenPropertyName(
			IEclipsePreferences preferences, String name) {
		String value = preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, "");
		if (!value.contains(name)) {
			preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP,
					value.length() == 0 ? String.format(
							VALUE_FORMATTER, name) : String.format(
									MULTI_VALUE_FORMATTER, value, name));
		}
	}

	public static List<String> getOverriddenPropertyNames(
			IEclipsePreferences preferences) {
		String value = preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, null);
		if (value == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>();
		for (String name : value.split(SEPARATOR)) {
			result.add(name.substring(1, name.length() - 1));
		}
		return result;
	}

	public static void removeOverriddenPropertyNames(
			IEclipsePreferences preferences) {
		preferences.remove(PROPS_OVERRIDDEN_BY_CSS_PROP);
	}
}
