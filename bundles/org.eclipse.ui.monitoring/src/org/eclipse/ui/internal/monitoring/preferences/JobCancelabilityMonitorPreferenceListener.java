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

import java.util.concurrent.TimeUnit;

import org.eclipse.core.internal.jobs.JobCancelabilityMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.monitoring.PreferenceConstants;

@SuppressWarnings("restriction")
public final class JobCancelabilityMonitorPreferenceListener implements IPropertyChangeListener {

	private JobCancelabilityMonitor.BasicOptionsImpl options;

	public JobCancelabilityMonitorPreferenceListener(JobCancelabilityMonitor.BasicOptionsImpl options) {
		this.options = options;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String changedProperty = event.getProperty();
		if (PreferenceConstants.JOB_MONITORING_ENABLED.equals(changedProperty)) {
			if (options == null) {
				options = new JobCancelabilityMonitor.BasicOptionsImpl();
				MonitoringPlugin.getDefault().getBundle().getBundleContext().registerService(JobCancelabilityMonitor.Options.class, options, null);
			}
			options.setEnabled((Boolean) event.getNewValue());
		} else if (PreferenceConstants.JOB_MONITORING_WARNING_THRESHOLD_MILLIS.equals(changedProperty)) {
			options.setWarningThreshold(TimeUnit.MILLISECONDS.toNanos((Integer) event.getNewValue()));
		} else if (PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS.equals(changedProperty)) {
			options.setErrorThreshold(TimeUnit.MILLISECONDS.toNanos((Integer) event.getNewValue()));
		} else if (PreferenceConstants.JOB_MONITORING_MAX_STACK_SAMPLES.equals(changedProperty)) {
			options.setMaxStackSamples((Integer) event.getNewValue());
		} else if (PreferenceConstants.JOB_MONITORING_LOG_NON_CANCELLABLE_USER_JOB.equals(changedProperty)) {
			options.setAlwaysReportNonCancelableUserJobAsError((Boolean) event.getNewValue());
		}
	}
}