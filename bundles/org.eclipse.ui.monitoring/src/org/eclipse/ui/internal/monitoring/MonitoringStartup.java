/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
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

import static org.eclipse.ui.monitoring.PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS;
import static org.eclipse.ui.monitoring.PreferenceConstants.DUMP_ALL_THREADS;
import static org.eclipse.ui.monitoring.PreferenceConstants.FILTER_TRACES;
import static org.eclipse.ui.monitoring.PreferenceConstants.INITIAL_SAMPLE_DELAY_MILLIS;
import static org.eclipse.ui.monitoring.PreferenceConstants.LOG_TO_ERROR_LOG;
import static org.eclipse.ui.monitoring.PreferenceConstants.LONG_EVENT_THRESHOLD_MILLIS;
import static org.eclipse.ui.monitoring.PreferenceConstants.MAX_STACK_SAMPLES;
import static org.eclipse.ui.monitoring.PreferenceConstants.MONITORING_ENABLED;
import static org.eclipse.ui.monitoring.PreferenceConstants.PLUGIN_ID;
import static org.eclipse.ui.monitoring.PreferenceConstants.SAMPLE_INTERVAL_MILLIS;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.monitoring.preferences.MonitoringPreferenceListener;

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
		if (preferences.getBoolean(MONITORING_ENABLED)) {
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
		// Final setup and start of the monitoring thread.
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
	    IPreferencesService preferences = Platform.getPreferencesService();
		EventLoopMonitorThread.Parameters args = new EventLoopMonitorThread.Parameters();

		args.longEventThreshold = preferences.getInt(PLUGIN_ID, LONG_EVENT_THRESHOLD_MILLIS,
				500, null);
		args.maxStackSamples = preferences.getInt(PLUGIN_ID, MAX_STACK_SAMPLES, 3, null);
		args.sampleInterval = preferences.getInt(PLUGIN_ID, SAMPLE_INTERVAL_MILLIS, 300, null);
		args.initialSampleDelay = preferences.getInt(PLUGIN_ID, INITIAL_SAMPLE_DELAY_MILLIS,
				300, null);
		args.deadlockThreshold = preferences.getInt(PLUGIN_ID, DEADLOCK_REPORTING_THRESHOLD_MILLIS,
				600000, null);
		args.dumpAllThreads = preferences.getBoolean(PLUGIN_ID, DUMP_ALL_THREADS, false, null);
		args.filterTraces = preferences.getString(PLUGIN_ID, FILTER_TRACES, "", null); //$NON-NLS-1$
		args.logToErrorLog = preferences.getBoolean(PLUGIN_ID, LOG_TO_ERROR_LOG, true, null);

		return args;
	}
}
