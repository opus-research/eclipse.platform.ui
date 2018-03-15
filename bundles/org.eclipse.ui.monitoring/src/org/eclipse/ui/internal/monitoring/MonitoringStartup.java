/*******************************************************************************
 * Copyright (C) 2014, 2015 Google Inc and others.
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

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.monitoring.preferences.MonitoringPreferenceListener;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Starts the event loop monitoring thread. Initializes preferences from {@link IPreferenceStore}.
 */
public class MonitoringStartup implements IStartup {
	private EventLoopMonitorThread monitoringThread;
	private MonitorCancellationMonitoringPolicy monitorCancellationMonitoringPolicy;

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

		if (preferences.getBoolean(PreferenceConstants.JOB_MONITORING_ENABLED)) {
			monitorCancellationMonitoringPolicy = new MonitorCancellationMonitoringPolicy();
			monitorCancellationMonitoringPolicy.setEnabled(true);
			monitorCancellationMonitoringPolicy.setWarningThreshold(TimeUnit.MILLISECONDS.toNanos(preferences.getLong(PreferenceConstants.JOB_MONITORING_WARNING_THRESHOLD_MILLIS)));
			monitorCancellationMonitoringPolicy.setErrorThreshold(TimeUnit.MILLISECONDS.toNanos(preferences.getLong(PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS)));
			monitorCancellationMonitoringPolicy.setMaxStackSamples(preferences.getInt(PreferenceConstants.JOB_MONITORING_MAX_STACK_SAMPLES));
			monitorCancellationMonitoringPolicy.setAlwaysReportNonCancellableUserJobAsError(preferences.getBoolean(PreferenceConstants.JOB_MONITORING_LOG_NON_CANCELLABLE_USER_JOB));
			monitorCancellationMonitoringPolicy.setDoNotReportNonCancellableFastSystemJob(preferences.getBoolean(PreferenceConstants.JOB_MONITORING_DO_NOT_LOG_FAST_SYSTEM_JOB));
			MonitoringPlugin.getDefault().getBundle().getBundleContext().registerService(JobManager.MonitorCancellationMonitoringPolicy.class, monitorCancellationMonitoringPolicy, null);
		}

		preferences.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String changedProperty = event.getProperty();
				if (PreferenceConstants.JOB_MONITORING_ENABLED.equals(changedProperty)) {
					if (monitorCancellationMonitoringPolicy == null) {
						monitorCancellationMonitoringPolicy = new MonitorCancellationMonitoringPolicy();
						MonitoringPlugin.getDefault().getBundle().getBundleContext().registerService(JobManager.MonitorCancellationMonitoringPolicy.class, monitorCancellationMonitoringPolicy, null);
					}
					monitorCancellationMonitoringPolicy.setEnabled((Boolean) event.getNewValue());
				} else if (PreferenceConstants.JOB_MONITORING_WARNING_THRESHOLD_MILLIS.equals(changedProperty)) {
					monitorCancellationMonitoringPolicy.setWarningThreshold(TimeUnit.MILLISECONDS.toNanos((Integer) event.getNewValue()));
				} else if (PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS.equals(changedProperty)) {
					monitorCancellationMonitoringPolicy.setErrorThreshold(TimeUnit.MILLISECONDS.toNanos((Integer) event.getNewValue()));
				} else if (PreferenceConstants.JOB_MONITORING_MAX_STACK_SAMPLES.equals(changedProperty)) {
					monitorCancellationMonitoringPolicy.setMaxStackSamples((Integer) event.getNewValue());
				} else if (PreferenceConstants.JOB_MONITORING_LOG_NON_CANCELLABLE_USER_JOB.equals(changedProperty)) {
					monitorCancellationMonitoringPolicy.setAlwaysReportNonCancellableUserJobAsError((Boolean) event.getNewValue());
				} else if (PreferenceConstants.JOB_MONITORING_DO_NOT_LOG_FAST_SYSTEM_JOB.equals(changedProperty)) {
					monitorCancellationMonitoringPolicy.setDoNotReportNonCancellableFastSystemJob((Boolean) event.getNewValue());
				}
			}
		});
	}

	@SuppressWarnings("restriction")
	static class MonitorCancellationMonitoringPolicy implements JobManager.MonitorCancellationMonitoringPolicy {

		private boolean enabled;
		private long errorThreshold;
		private long warningThreshold;
		private int maxStackSamples;
		private boolean alwaysReportNonCancellableUserJobAsError;
		private boolean doNotReportNonCancellableFastSystemJob;

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public void setErrorThreshold(long errorThreshold) {
			this.errorThreshold = errorThreshold;
		}

		public void setWarningThreshold(long warningThreshold) {
			this.warningThreshold = warningThreshold;
		}

		public void setMaxStackSamples(int maxStackSamples) {
			this.maxStackSamples = maxStackSamples;
		}

		public void setAlwaysReportNonCancellableUserJobAsError(boolean alwaysReportNonCancellableUserJobAsError) {
			this.alwaysReportNonCancellableUserJobAsError = alwaysReportNonCancellableUserJobAsError;
		}

		public void setDoNotReportNonCancellableFastSystemJob(boolean doNotReportNonCancellableFastSystemJob) {
			this.doNotReportNonCancellableFastSystemJob = doNotReportNonCancellableFastSystemJob;
		}

		@Override
		public boolean enabled() {
			return enabled;
		}

		@Override
		public long errorThreshold() {
			return errorThreshold;
		}

		@Override
		public long warningThreshold() {
			return warningThreshold;
		}

		@Override
		public int maxStackSamples() {
			return maxStackSamples;
		}

		@Override
		public boolean alwaysReportNonCancellableUserJobAsError() {
			return alwaysReportNonCancellableUserJobAsError;
		}

		@Override
		public boolean doNotReportNonCancellableFastSystemJob() {
			return doNotReportNonCancellableFastSystemJob;
		}

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
