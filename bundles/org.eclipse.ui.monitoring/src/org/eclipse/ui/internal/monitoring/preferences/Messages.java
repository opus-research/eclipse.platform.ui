/*******************************************************************************
 * Copyright (c) 2014, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String FilterInputDialog_filter_input_label;
	public static String FilterInputDialog_header;
	public static String FilterInputDialog_invalid_method_name;
	public static String FilterInputDialog_noninteresting_thread_filter_message;
	public static String FilterInputDialog_note_label;
	public static String FilterInputDialog_title;
	public static String FilterInputDialog_ui_thread_filter_message;
	public static String MonitoringPreferenceListener_preference_error_header;
	public static String MonitoringPreferenceListener_preference_error;
	public static String MonitoringPreferencePage_add_ui_thread_filter_button_label;
	public static String MonitoringPreferencePage_add_noninteresting_thread_filter_button_label;
	public static String MonitoringPreferencePage_deadlock_threshold_label;
	public static String MonitoringPreferencePage_deadlock_threshold_too_low_error;
	public static String MonitoringPreferencePage_enable_monitoring_label;
	public static String MonitoringPreferencePage_error_threshold_label;
	public static String MonitoringPreferencePage_error_threshold_too_low_error;
	public static String MonitoringPreferencePage_log_freeze_events_label;
	public static String MonitoringPreferencePage_max_stack_samples_label;
	public static String MonitoringPreferencePage_noninteresting_thread_filter_label;
	public static String MonitoringPreferencePage_remove_ui_thread_filter_button_label;
	public static String MonitoringPreferencePage_remove_noninteresting_thread_filter_button_label;
	public static String MonitoringPreferencePage_ui_thread_filter_label;
	public static String MonitoringPreferencePage_warning_threshold_label;

	public static String JobCancelabilityMonitorPreferencePage_enable_monitoring_label;
	public static String JobCancelabilityMonitorPreferencePage_warning_threshold_label;
	public static String JobCancelabilityMonitorPreferencePage_error_threshold_label;
	public static String JobCancelabilityMonitorPreferencePage_max_stack_samples_label;
	public static String JobCancelabilityMonitorPreferencePage_log_non_cancelable_user_job_label;
	public static String JobCancelabilityMonitorPreferencePage_not_log_fast_system_job_label;

	private Messages() {
		// Do not instantiate.
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
