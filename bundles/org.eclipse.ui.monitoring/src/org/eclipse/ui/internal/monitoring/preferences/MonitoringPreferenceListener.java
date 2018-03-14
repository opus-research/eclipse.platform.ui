/*******************************************************************************
 * Copyright (C) 2014, Google Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.monitoring.EventLoopMonitorThread;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.internal.monitoring.MonitoringStartup;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Listens to preference changes and restarts the monitoring thread when necessary.
 */
public class MonitoringPreferenceListener implements IPropertyChangeListener {
	private EventLoopMonitorThread monitoringThread;
	/**
	 * A flag to handle the resetting of the {@link EventLoopMonitorThread}. The method
	 * {@link #restartMonitoringThread()} can be called multiple times if multiple preferences are
	 * changed via the preference page. {@code monitorThreadRestartInProgress} is set on the first
	 * call to {@link #restartMonitoringThread()}. Subsequent calls to restartMonitorThread do not
	 * schedule more resets while the flag is enabled. Once the scheduled asyncExec event executes,
	 * the flag is reset.
	 */
	private volatile boolean monitorThreadRestartInProgress;

	public MonitoringPreferenceListener(EventLoopMonitorThread thread) {
		monitoringThread = thread;
		monitorThreadRestartInProgress = false;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(PreferenceConstants.MONITORING_ENABLED)) {
			if (monitoringThread != null && event.getNewValue().equals(false)) {
				monitoringThread.shutdown();
				monitoringThread = null;
			} else if (monitoringThread == null && event.getNewValue().equals(true)) {
				monitoringThread = MonitoringStartup.createAndStartMonitorThread();
			}
		} else if (property.equals(PreferenceConstants.FORCE_DEADLOCK_LOG_TIME_MILLIS)
				|| property.equals(PreferenceConstants.MAX_LOG_TRACE_COUNT)
				|| property.equals(PreferenceConstants.MAX_EVENT_LOG_TIME_MILLIS)
				|| property.equals(PreferenceConstants.MAX_EVENT_SAMPLE_TIME_MILLIS)
				|| property.equals(PreferenceConstants.SAMPLE_INTERVAL_TIME_MILLIS)
				|| property.equals(PreferenceConstants.DUMP_ALL_THREADS)
				|| property.equals(PreferenceConstants.LOG_TO_ERROR_LOG)
				|| property.equals(PreferenceConstants.FILTER_TRACES)) {
			restartMonitoringThread();
		}
	}

	private void restartMonitoringThread() {
		// If monitoringThread is null, the plug-in is not enabled.
		if (monitoringThread == null || monitorThreadRestartInProgress) {
			return;
		}

		monitorThreadRestartInProgress = true;

		final Display display = MonitoringPlugin.getDefault().getWorkbench().getDisplay();
		// Schedule the event to restart the thread after all preferences have been properly updated. 
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				monitorThreadRestartInProgress = false;
				EventLoopMonitorThread thread = MonitoringStartup.createAndStartMonitorThread();
				// If thread is null, the newly-defined preferences are invalid.
				if (thread == null) {
					MessageDialog.openInformation(
							MonitoringPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
							Messages.MonitoringPreferenceListener_preference_error_header,
							Messages.MonitoringPreferenceListener_preference_error);
					return;
				}

				monitoringThread.shutdown();
				monitoringThread = thread;
			}
		});
	}
}
