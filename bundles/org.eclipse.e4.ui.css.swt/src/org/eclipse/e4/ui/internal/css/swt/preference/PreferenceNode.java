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

package org.eclipse.e4.ui.internal.css.swt.preference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class PreferenceNode {
	private String id;

	private Map<String, String> overriddenPreferences;

	private IEclipsePreferences prefNode;

	public PreferenceNode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void overridePreference(String name, String value) {
		String existingValue = getPreferenceNode().get(name, null);
		// Override the pref when it isn't overridden by the user or it
		// wasn't reset during closing the Workbench (i.e. after crash)
		if (existingValue == null || existingValue.equals(value)) {
			if (overriddenPreferences == null) {
				overriddenPreferences = new HashMap<String, String>();
			}
			overriddenPreferences.put(name, value);
			getPreferenceNode().put(name, value);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getOverriddenPreferences() {
		return overriddenPreferences == null ? Collections.EMPTY_MAP
				: overriddenPreferences;
	}

	public IEclipsePreferences getPreferenceNode() {
		if (prefNode == null) {
			prefNode = InstanceScope.INSTANCE.getNode(id);
		}
		return prefNode;
	}
}
