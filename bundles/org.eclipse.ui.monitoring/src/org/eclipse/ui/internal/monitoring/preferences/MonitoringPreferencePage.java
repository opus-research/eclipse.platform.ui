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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Preference page that allows user to toggle plug in settings from Eclipse preferences.
 */
public class MonitoringPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {
	/**
	 * Checks that the capture threshold is less than the log threshold.
	 */
	private class LogThresholdFieldEditor extends IntegerFieldEditor {
		IntegerFieldEditor maxEventSampleTime;

		public LogThresholdFieldEditor(String name, String textLabel, Group group) {
			super(name, textLabel, group);
			this.setupField();
			this.fillIntoGrid(group, 2);
		}

		@Override
		protected boolean checkState() {
			try {
				if (maxEventSampleTime.getIntValue() > this.getIntValue()) {
					showErrorMessage();
					return false;
				}
			} catch (NumberFormatException e) {
				// With a number exception, fall through and allow the parent class handle state
			}
			return super.checkState();
		}

		public void setSampleFieldEditor(IntegerFieldEditor field) {
			this.maxEventSampleTime = field;
		}

		private void setupField() {
			super.setValidRange(1, Integer.MAX_VALUE);
			super.setErrorMessage(Messages.MonitoringPreferencePage_log_threshold_error);
			addField(this);
		}
	}

	public MonitoringPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		parent.setLayout(layout);

		final Group groupContainer = new Group(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout(1, false);
		groupContainer.setLayout(groupLayout);
		groupContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Group topGroup = new Group(groupContainer, SWT.NONE);
		GridLayout innerGroupLayout = new GridLayout(2, false);
		topGroup.setLayout(innerGroupLayout);
		topGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createBooleanFieldEditor(PreferenceConstants.MONITORING_ENABLED,
				Messages.MonitoringPreferencePage_enable_thread_label, topGroup);
		createIntegerFieldEditor(PreferenceConstants.FORCE_DEADLOCK_LOG_TIME_MILLIS,
				Messages.MonitoringPreferencePage_deadlock_label, topGroup);
		createIntegerFieldEditor(PreferenceConstants.MAX_LOG_TRACE_COUNT,
				Messages.MonitoringPreferencePage_stack_sample_label, topGroup);

		final LogThresholdFieldEditor maxEventLogTime = new LogThresholdFieldEditor(
				PreferenceConstants.MAX_EVENT_LOG_TIME_MILLIS,
				Messages.MonitoringPreferencePage_event_log_label, topGroup);

		IntegerFieldEditor maxEventSampleTime = new IntegerFieldEditor(
				PreferenceConstants.MAX_EVENT_SAMPLE_TIME_MILLIS,
				Messages.MonitoringPreferencePage_first_stack_label, topGroup) {
			@Override
			protected boolean checkState() {
				try {
					if (maxEventLogTime.getIntValue() < this.getIntValue()) {
						showErrorMessage();
						return false;
					}
				} catch (NumberFormatException e) {
					// With a number exception, fall through and allow the parent class handle state.
				}
				return super.checkState();
			}
		};

		maxEventSampleTime.setValidRange(1, Integer.MAX_VALUE);
		maxEventSampleTime.setErrorMessage(Messages.MonitoringPreferencePage_capture_threshold_error);
		maxEventLogTime.setSampleFieldEditor(maxEventSampleTime);
		maxEventSampleTime.fillIntoGrid(topGroup, 2);
		addField(maxEventSampleTime);

		createIntegerFieldEditor(PreferenceConstants.SAMPLE_INTERVAL_TIME_MILLIS,
				Messages.MonitoringPreferencePage_sample_interval_label, topGroup);
		topGroup.setLayout(innerGroupLayout);

		createBooleanFieldEditor(PreferenceConstants.DUMP_ALL_THREADS,
				Messages.MonitoringPreferencePage_dump_all_threads_label, topGroup);
		topGroup.setLayout(innerGroupLayout);

		createBooleanFieldEditor(PreferenceConstants.LOG_TO_ERROR_LOG,
				Messages.MonitoringPreferencePage_log_freeze_events_label, topGroup);
		topGroup.setLayout(innerGroupLayout);


		final Group bottomGroup = new Group(groupContainer, SWT.NONE);
		bottomGroup.setLayout(innerGroupLayout);
		bottomGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addField(new ListFieldEditor(PreferenceConstants.FILTER_TRACES,
				Messages.MonitoringPreferencePage_filter_label, bottomGroup));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(MonitoringPlugin.getDefault().getPreferenceStore());
	}

	private void createBooleanFieldEditor(String name, String labelText, Group group) {
		BooleanFieldEditor field = new BooleanFieldEditor(name, labelText, group);
		field.fillIntoGrid(group, 2);
		addField(field);
	}

	private void createIntegerFieldEditor(String name, String labelText, Group group) {
		IntegerFieldEditor field = new IntegerFieldEditor(name, labelText, group);
		field.setValidRange(1, Integer.MAX_VALUE);
		field.setErrorMessage(Messages.MonitoringPreferencePage_invalid_number_error);
		field.fillIntoGrid(group, 2);
		addField(field);
	}
}
