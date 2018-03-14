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
package org.eclipse.ui.internal.monitoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.monitoring.IUiFreezeEventLogger;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Writes {@link UiFreezeEvent}s to the Eclipse error log.
 */
public class DefaultUiFreezeEventLogger implements IUiFreezeEventLogger {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$

	/**
	 * Parses the given {@link UiFreezeEvent} into a {@link MultiStatus} and saves it to the log.
	 * 
	 * @param event the event that caused the UI thread to freeze
	 */
	@Override
	public void log(UiFreezeEvent event) {
		long lastTimestamp = event.getStartTimestamp();
		String startTime = dateFormat.format(new Date(lastTimestamp));

		String pattern = event.isStillRunning()
				? Messages.DefaultUiFreezeEventLogger_ui_delay_header_running_2
						: Messages.DefaultUiFreezeEventLogger_ui_delay_header_non_running_2;
		String header =
				NLS.bind(pattern, String.format("%.2f", event.getTotalDuration() / 1000.0), startTime); //$NON-NLS-1$

		MultiStatus loggedEvent = new MultiStatus(
				PreferenceConstants.PLUGIN_ID, IStatus.WARNING, header, null);

		for (int i = 0; i < event.getSampleCount(); i++) {
			StackSample sample = event.getStackTraceSamples()[i];

			double deltaInSeconds = (sample.getTimestamp() - lastTimestamp) / 1000.0;

			String traceText = NLS.bind(Messages.DefaultUiFreezeEventLogger_sample_header_2,
					dateFormat.format(sample.getTimestamp()), String.format("%.3f", deltaInSeconds)); //$NON-NLS-1$
			MultiStatus traceStatus = new MultiStatus(
					PreferenceConstants.PLUGIN_ID, IStatus.INFO, traceText, null);
			loggedEvent.add(traceStatus);

			for (ThreadInfo thread : sample.getStackTraces()) {
				Exception stackTrace =
						new Exception(Messages.DefaultUiFreezeEventLogger_stack_trace_header);
				stackTrace.setStackTrace(thread.getStackTrace());

				String threadDetails = NLS.bind(Messages.DefaultUiFreezeEventLogger_thread_details,
						thread.getThreadId(), thread.getThreadState());

				StringBuilder threadText = new StringBuilder(
						NLS.bind(Messages.DefaultUiFreezeEventLogger_thread_header_2,
								thread.getThreadName(), threadDetails));

				if (thread.getLockName() != null && !thread.getLockName().isEmpty()) {
					LockInfo lock = thread.getLockInfo();
					threadText.append(NLS.bind(Messages.DefaultUiFreezeEventLogger_waiting_for_1,
							getClassAndHashCode(lock)));

					if (thread.getLockOwnerName() != null && !thread.getLockOwnerName().isEmpty()) {
						threadText.append(NLS.bind(Messages.DefaultUiFreezeEventLogger_lock_owner_2,
								thread.getLockOwnerName(), thread.getLockOwnerId()));
					}
				}

				for (LockInfo lockInfo : thread.getLockedSynchronizers()) {
					threadText.append(NLS.bind(Messages.DefaultUiFreezeEventLogger_holding_1,
							getClassAndHashCode(lockInfo)));
				}
				IStatus threadStatus = new Status(IStatus.INFO, PreferenceConstants.PLUGIN_ID,
						threadText.toString(), stackTrace);
				traceStatus.add(threadStatus);
			}
			lastTimestamp = sample.getTimestamp();
		}

		if (loggedEvent.getChildren().length > 0) {
			MonitoringPlugin.getDefault().getLog().log(loggedEvent);
		}
	}

	private static String getClassAndHashCode(LockInfo info) {
		return String.format("%s@%08x", info.getClassName(), info.getIdentityHashCode()); //$NON-NLS-1$
	}
}
