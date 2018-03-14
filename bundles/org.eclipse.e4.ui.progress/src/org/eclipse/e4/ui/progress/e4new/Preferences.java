package org.eclipse.e4.ui.progress.e4new;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;

public class Preferences {
	
	private static Map<String, String> preferences;
	
	@Inject
	private static synchronized void updatePreferences(MApplication application) {
		preferences = application.getPersistedState();
	}
	
	public static synchronized String get(String key) {
		return preferences.get(key);
	}

	public static synchronized String set(String key, String value) {
		return preferences.put(key, value);
	}
	
}
