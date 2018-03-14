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
		String header = NLS.bind(pattern,
				String.format("%.2f", event.getTotalDuration() / 1000.0), startTime); //$NON-NLS-1$

		MultiStatus loggedEvent = new MultiStatus(PreferenceConstants.PLUGIN_ID,
				IStatus.WARNING, header, null);

		for (int i = 0; i < event.getSampleCount(); i++) {
			StackSample sample = event.getStackTraceSamples()[i];

			double deltaInSeconds = (sample.getTimestamp() - lastTimestamp) / 1000.0;
			MultiStatus traceStatus = null;
			ThreadInfo[] threads = sample.getStackTraces();

			for (int j = 0; j < threads.length; j++) {
				if (threads[j].getThreadName().equals("main")) { //$NON-NLS-1$
					Exception stackTrace = new Exception(
							Messages.DefaultUiFreezeEventLogger_stack_trace_header);
					stackTrace.setStackTrace(threads[j].getStackTrace());

					String traceText = NLS.bind(
							Messages.DefaultUiFreezeEventLogger_sample_header_2,
							dateFormat.format(sample.getTimestamp()),
							String.format("%.3f", deltaInSeconds)); //$NON-NLS-1$
					traceStatus = new MultiStatus(PreferenceConstants.PLUGIN_ID, IStatus.INFO,
							String.format("%s\n%s", traceText, createThreadMessage(threads[j])), //$NON-NLS-1$
							stackTrace);
					loggedEvent.add(traceStatus);

					for (int k = 0; k < j; k++) {
						traceStatus.add(createThreadStatus(threads[k]));
					}
				} else if (traceStatus != null) {
					traceStatus.add(createThreadStatus(threads[j]));
				}
			}

			lastTimestamp = sample.getTimestamp();
		}

		MonitoringPlugin.getDefault().getLog().log(loggedEvent);
	}

	private static IStatus createThreadStatus(ThreadInfo thread) {
		Exception stackTrace = new Exception(
				Messages.DefaultUiFreezeEventLogger_stack_trace_header);
		stackTrace.setStackTrace(thread.getStackTrace());

		StringBuilder threadText = createThreadMessage(thread);

		if (thread.getLockName() != null && !thread.getLockName().isEmpty()) {
			LockInfo lock = thread.getLockInfo();
			threadText.append(NLS.bind(
					Messages.DefaultUiFreezeEventLogger_waiting_for_1,
					getClassAndHashCode(lock)));

			if (thread.getLockOwnerName() != null
					&& !thread.getLockOwnerName().isEmpty()) {
				threadText.append(NLS.bind(
						Messages.DefaultUiFreezeEventLogger_lock_owner_2,
						thread.getLockOwnerName(), thread.getLockOwnerId()));
			}
		}

		for (LockInfo lockInfo : thread.getLockedSynchronizers()) {
			threadText.append(NLS.bind(
					Messages.DefaultUiFreezeEventLogger_holding_1,
					getClassAndHashCode(lockInfo)));
		}

		return new Status(IStatus.INFO, PreferenceConstants.PLUGIN_ID,
				threadText.toString(), stackTrace);
	}
	
	private static StringBuilder createThreadMessage(ThreadInfo thread) {
		String threadDetails = NLS.bind(
				Messages.DefaultUiFreezeEventLogger_thread_details,
				thread.getThreadId(), thread.getThreadState());

		StringBuilder threadText = new StringBuilder(NLS.bind(
				Messages.DefaultUiFreezeEventLogger_thread_header_2,
				thread.getThreadName(), threadDetails));
		
		return threadText;
	}
	
	private static String getClassAndHashCode(LockInfo info) {
		return String.format("%s@%08x", info.getClassName(), info.getIdentityHashCode()); //$NON-NLS-1$
	}
}
