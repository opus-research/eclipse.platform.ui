/*******************************************************************************
 * Copyright (C) 2014, 2016 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.internal.jobs.JobCancelabilityMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.monitoring.preferences.JobCancelabilityMonitorPreferenceListener;
import org.eclipse.ui.internal.monitoring.preferences.MonitoringPreferenceListener;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Starts the event loop monitoring thread. Initializes preferences from {@link IPreferenceStore}.
 */
public class MonitoringStartup implements IStartup {
	private EventLoopMonitorThread monitoringThread;

	@SuppressWarnings("restriction")
	@Override
	public void earlyStartup() {
		if (monitoringThread != null) {
			return;
		}

		IPreferenceStore preferences = MonitoringPlugin.getDefault().getPreferenceStore();
		if (preferences.getBoolean(PreferenceConstants.MONITORING_ENABLED)) {
			monitoringThread = createAndStartMonitorThread();
		}

		preferences.addPropertyChangeListener(new MonitoringPreferenceListener(monitoringThread));

		JobCancelabilityMonitor.BasicOptionsImpl jobCancelabilityMonitorOptions = null;
		if (preferences.getBoolean(PreferenceConstants.JOB_MONITORING_ENABLED)) {
			jobCancelabilityMonitorOptions = createJobCancellabilityMonitorService(preferences);
		}

		preferences.addPropertyChangeListener(new JobCancelabilityMonitorPreferenceListener(jobCancelabilityMonitorOptions));
	}

	@SuppressWarnings("restriction")
	private JobCancelabilityMonitor.BasicOptionsImpl createJobCancellabilityMonitorService(IPreferenceStore preferences) {
		JobCancelabilityMonitor.BasicOptionsImpl jobCancelabilityMonitorOptions;
		jobCancelabilityMonitorOptions = new JobCancelabilityMonitor.BasicOptionsImpl();
		jobCancelabilityMonitorOptions.setEnabled(true);
		jobCancelabilityMonitorOptions.setErrorThreshold(TimeUnit.MILLISECONDS.toNanos(preferences.getInt(PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS)));
		jobCancelabilityMonitorOptions.setWarningThreshold(TimeUnit.MILLISECONDS.toNanos(preferences.getInt(PreferenceConstants.JOB_MONITORING_WARNING_THRESHOLD_MILLIS)));
		jobCancelabilityMonitorOptions.setMaxStackSamples(preferences.getInt(PreferenceConstants.JOB_MONITORING_MAX_STACK_SAMPLES));
		jobCancelabilityMonitorOptions.setAlwaysReportNonCancelableUserJobAsError(preferences.getBoolean(PreferenceConstants.JOB_MONITORING_LOG_NON_CANCELLABLE_USER_JOB));
		jobCancelabilityMonitorOptions.setDoNotReportNonCancelableFastSystemJob(preferences.getBoolean(PreferenceConstants.JOB_MONITORING_DO_NOT_LOG_FAST_SYSTEM_JOB));
		MonitoringPlugin.getDefault().getBundle().getBundleContext().registerService(JobCancelabilityMonitor.Options.class, jobCancelabilityMonitorOptions, null);
		return jobCancelabilityMonitorOptions;
	}

	/**
	 * Creates and starts a new monitoring thread.
	 */
	public static EventLoopMonitorThread createAndStartMonitorThread() {
		EventLoopMonitorThread.Parameters args = loadPreferences();
		EventLoopMonitorThread temporaryThread = null;

		try {
			temporaryThread = new EventLoopMonitorThread(args);
		} catch (IllegalArgumentException e) {
			MonitoringPlugin.logError(Messages.MonitoringStartup_initialization_error, e);
			return null;
		}

		final EventLoopMonitorThread thread = temporaryThread;
		final Display display = MonitoringPlugin.getDefault().getWorkbench().getDisplay();
		// Final setup and start asynchronously on the display thread.
		display.asyncExec(() -> {
			// If we're still running when display gets disposed, shutdown the thread.
			display.disposeExec(() -> thread.shutdown());
			thread.start();
		});

		return thread;
	}

	private static EventLoopMonitorThread.Parameters loadPreferences() {
		IPreferenceStore preferences = MonitoringPlugin.getDefault().getPreferenceStore();
		EventLoopMonitorThread.Parameters args = new EventLoopMonitorThread.Parameters();

		args.longEventWarningThreshold =
				preferences.getInt(PreferenceConstants.LONG_EVENT_WARNING_THRESHOLD_MILLIS);
		args.longEventErrorThreshold =
				preferences.getInt(PreferenceConstants.LONG_EVENT_ERROR_THRESHOLD_MILLIS);
		args.deadlockThreshold =
				preferences.getInt(PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS);
		args.maxStackSamples = preferences.getInt(PreferenceConstants.MAX_STACK_SAMPLES);
		args.uiThreadFilter = preferences.getString(PreferenceConstants.UI_THREAD_FILTER);
		args.noninterestingThreadFilter =
				preferences.getString(PreferenceConstants.NONINTERESTING_THREAD_FILTER);
		args.logToErrorLog = preferences.getBoolean(PreferenceConstants.LOG_TO_ERROR_LOG);

		return args;
	}
}
