/*******************************************************************************
 * Copyright (C) 2014 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.monitoring;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Definitions of the preference constants.
 *
 * @since 1.0
 */
public class PreferenceConstants {
	public static final String PLUGIN_ID = "org.eclipse.ui.monitoring"; //$NON-NLS-1$
	/**
	 * If true, enables the monitoring thread which logs events which take long time to process.
	 */
	public static final String MONITORING_ENABLED = "monitoring_enabled"; //$NON-NLS-1$
	/**
	 * Events that took longer than the specified duration in milliseconds are logged as warnings.
	 */
	public static final String LONG_EVENT_WARNING_THRESHOLD_MILLIS = "long_event_warning_threshold"; //$NON-NLS-1$
	/**
	 * Events that took longer than the specified duration in milliseconds are logged as errors.
	 */
	public static final String LONG_EVENT_ERROR_THRESHOLD_MILLIS = "long_event_error_threshold"; //$NON-NLS-1$
	/**
	 * Events that took longer than the specified duration are reported as deadlocks without waiting
	 * for the event to finish.
	 */
	public static final String DEADLOCK_REPORTING_THRESHOLD_MILLIS = "deadlock_reporting_threshold"; //$NON-NLS-1$
	/**
	 * Maximum number of stack trace samples to write out to the log.
	 */
	public static final String MAX_STACK_SAMPLES = "max_stack_samples"; //$NON-NLS-1$
	/**
	 * If true, log freeze events to the Eclipse error log.
	 */
	public static final String LOG_TO_ERROR_LOG = "log_to_error_log"; //$NON-NLS-1$
	/**
	 * Comma separated fully qualified method names of stack frames. The names may contain
	 * '*' and '?' wildcard characters. A UI freeze is not logged if any of the stack traces
	 * of the UI thread contains at least one method matching the filter.
	 */
	public static final String UI_THREAD_FILTER = "ui_thread_filter"; //$NON-NLS-1$
	/**
	 * Comma separated fully qualified method names of stack frames. The names may contain
	 * '*' and '?' wildcard characters. A non-UI thread is not included in the logged UI freeze
	 * message if all stack frames of the thread match the filter.
	 */
	public static final String NONINTERESTING_THREAD_FILTER = "noninteresting_thread_filter"; //$NON-NLS-1$

	/**
	 * If true, enables the monitoring of tasks which do not check for cancellation often enough.
	 */
	public static final String TASK_MONITORING_ENABLED = "task_monitoring_enabled"; //$NON-NLS-1$

	/**
	 * Time between two calls of {@link IProgressMonitor#isCanceled()} that took longer than the specified duration in milliseconds are logged as warning.
	 */
	public static final String TASK_MONITORING_WARNING_THRESHOLD_MILLIS = "task_monitoring_warning_threshold"; //$NON-NLS-1$

	/**
	 * Time between two calls of {@link IProgressMonitor#isCanceled()} that took longer than the specified duration in milliseconds are logged as errors.
	 */
	public static final String TASK_MONITORING_ERROR_THRESHOLD_MILLIS = "task_monitoring_error_threshold"; //$NON-NLS-1$

	/**
	 * Maximum number of stack trace samples to write out to the log.
	 */
	public static final String TASK_MONITORING_MAX_STACK_SAMPLES = "task_monitoring_max_stack_sample"; //$NON-NLS-1$

	/**
	 * If true, all user tasks which don't check for cancellation will be reported as errors, independently of how
	 * fast they go. Otherwise, they will be reported as warning or error whether they execute faster than the error or
	 * warning thresholds, accordingly.
	 */
	public static final String TASK_MONITORING_LOG_NON_CANCELLABLE_USER_JOB = "task_monitoring_log_cancellable_user_job"; //$NON-NLS-1$

	private PreferenceConstants() {}
}
