/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.preferencestylingaddon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.ui.internal.css.swt.preference.IPreferenceNodeOverridable;

public class PreferenceNode implements IPreferenceNodeOverridable {
	private String id;

	private Map<String, String> overriddenPreferences;

	public PreferenceNode(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void overridePreference(String name, String value) {
		if (overriddenPreferences == null) {
			overriddenPreferences = new HashMap<String, String>();
		}
		overriddenPreferences.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getOverriddenPreferences() {
		return overriddenPreferences == null ? Collections.EMPTY_MAP : overriddenPreferences;
	}
}
