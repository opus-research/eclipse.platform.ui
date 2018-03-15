/*******************************************************************************
 * Copyright (c) 2016 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mikaël Barbero (Eclipse Foundation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Initializes the default values of the monitoring plug-in preferences.
 */
public class JobCancelabilityMonitorPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MonitoringPlugin.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.JOB_MONITORING_ENABLED, false);
		store.setDefault(PreferenceConstants.JOB_MONITORING_WARNING_THRESHOLD_MILLIS, 1000); // 2 sec
		store.setDefault(PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS, 3000); // 3 sec
		store.setDefault(PreferenceConstants.JOB_MONITORING_MAX_STACK_SAMPLES, 3);
		store.setDefault(PreferenceConstants.JOB_MONITORING_LOG_NON_CANCELLABLE_USER_JOB, true);
	}
}
