/*******************************************************************************
 * Copyright (C) 2014, Google Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.monitoring.preferences.MonitoringPreferenceListener;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Starts the event loop monitoring thread. Initializes preferences from {@link IPreferenceStore}.
 */
public class MonitoringStartup implements IStartup {
	private EventLoopMonitorThread monitoringThread;

	@Override
	public void earlyStartup() {
		setupPlugin();
	}

	private void setupPlugin() {
		if (monitoringThread != null) {
			return;
		}

		IPreferenceStore preferences = MonitoringPlugin.getDefault().getPreferenceStore();
		if (preferences.getBoolean(PreferenceConstants.MONITORING_ENABLED)) {
			monitoringThread = createAndStartMonitorThread();
		}

		preferences.addPropertyChangeListener(new MonitoringPreferenceListener(monitoringThread));
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
		// Final setup and start synced on display thread
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				// If we're still running when display gets disposed, shutdown the thread.
				display.disposeExec(new Runnable() {
					@Override
					public void run() {
						thread.shutdown();
					}
				});
				thread.start();
			}
		});

		return thread;
	}

	private static EventLoopMonitorThread.Parameters loadPreferences() {
		IPreferenceStore preferences = MonitoringPlugin.getDefault().getPreferenceStore();
		EventLoopMonitorThread.Parameters args = new EventLoopMonitorThread.Parameters();

		args.loggingThreshold = preferences.getInt(PreferenceConstants.MAX_EVENT_LOG_TIME_MILLIS);
		args.samplingThreshold = preferences.getInt(PreferenceConstants.MAX_EVENT_SAMPLE_TIME_MILLIS);
		args.dumpAllThreads = preferences.getBoolean(PreferenceConstants.DUMP_ALL_THREADS);
		args.minimumPollingDelay = preferences.getInt(PreferenceConstants.SAMPLE_INTERVAL_TIME_MILLIS);
		args.loggedTraceCount = preferences.getInt(PreferenceConstants.MAX_LOG_TRACE_COUNT);
		args.deadlockDelta = preferences.getInt(PreferenceConstants.FORCE_DEADLOCK_LOG_TIME_MILLIS);
		args.logLocally = preferences.getBoolean(PreferenceConstants.LOG_TO_ERROR_LOG);
		args.filterTraces = preferences.getString(PreferenceConstants.FILTER_TRACES);

		return args;
	}
}
