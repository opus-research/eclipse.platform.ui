package org.eclipse.ui.internal.preferences;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The PreferenceSettingsTransfer is the settings transfer for the workbench
 * preferences.
 *
 * @since 3.5
 *
 */
public class PreferencesSettingsTransfer extends WorkbenchSettingsTransfer{

	@Override
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return WorkbenchMessages.WorkbenchPreferences_Name;
	}

}
