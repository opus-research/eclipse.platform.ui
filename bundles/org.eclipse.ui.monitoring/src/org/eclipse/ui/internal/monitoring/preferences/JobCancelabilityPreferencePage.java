/*******************************************************************************
 * Copyright (C) 2016 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mikael Barbero (Eclipse Foundation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Preference page that allows user to toggle plug in settings from Eclipse preferences.
 */
public class JobCancelabilityPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	private static final int HOUR_IN_MS = 3600000;
	private static final IPreferenceStore preferences =
			MonitoringPlugin.getDefault().getPreferenceStore();
	private BooleanFieldEditor monitoringEnabled;
	private IntegerEditor maxTimeWarningThreshold;
	private IntegerEditor maxTimeErrorThreshold;
	private Map<FieldEditor, Composite> editors;

	private class IntegerEditor extends IntegerFieldEditor {
		public IntegerEditor(String name, String labelText, Composite parent, int min, int max) {
	    	super(name, labelText, parent);
	    	setValidRange(min, max);
		}

		@Override
		protected void valueChanged() {
			super.valueChanged();
			if (maxTimeWarningThreshold.isValid()) {
				maxTimeErrorThreshold.checkValue();
			}
		}

		@Override
		protected boolean checkState() {
			if (!super.checkState()) {
				return false;
			}

			String preferenceName = getPreferenceName();
			if (preferenceName.equals(PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS)) {
				if (maxTimeWarningThreshold.isValid() &&
						getIntValue() < maxTimeWarningThreshold.getIntValue()) {
					showMessage(Messages.MonitoringPreferencePage_error_threshold_too_low_error);
					return false;
				}
			}
			return true;
		}

		private boolean checkValue() {
	        boolean oldState = isValid();
	        refreshValidState();

	        boolean isValid = isValid();
	        if (isValid != oldState) {
				fireStateChanged(IS_VALID, oldState, isValid);
			}
	        return isValid;
	    }
	}

	public JobCancelabilityPreferencePage() {
		super(GRID);
		editors = new HashMap<>();
	}

	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		Composite container = new Composite(parent, SWT.NONE);

		createTopBlock(container);

		GridLayoutFactory.fillDefaults()
				.numColumns(1)
				.spacing(LayoutConstants.getSpacing())
				.generateLayout(container);

		GridLayoutFactory.fillDefaults()
				.numColumns(1)
				.spacing(LayoutConstants.getSpacing())
				.generateLayout(parent);
	}

	private Composite createTopBlock(Composite container) {
		Composite block = new Composite(container, SWT.NONE);

		monitoringEnabled = createBooleanEditor(PreferenceConstants.JOB_MONITORING_ENABLED,
				Messages.JobCancelabilityMonitorPreferencePage_enable_monitoring_label, block);

		maxTimeWarningThreshold = createIntegerEditor(
				PreferenceConstants.JOB_MONITORING_WARNING_THRESHOLD_MILLIS,
				Messages.JobCancelabilityMonitorPreferencePage_warning_threshold_label, block,
				3, HOUR_IN_MS);
		maxTimeErrorThreshold = createIntegerEditor(
				PreferenceConstants.JOB_MONITORING_ERROR_THRESHOLD_MILLIS,
				Messages.JobCancelabilityMonitorPreferencePage_error_threshold_label, block,
				3, HOUR_IN_MS);
		createIntegerEditor(
				PreferenceConstants.JOB_MONITORING_MAX_STACK_SAMPLES,
				Messages.JobCancelabilityMonitorPreferencePage_max_stack_samples_label, block, 0, 100);

		createBooleanEditor(PreferenceConstants.JOB_MONITORING_LOG_NON_CANCELLABLE_USER_JOB,
				Messages.JobCancelabilityMonitorPreferencePage_log_non_cancelable_user_job_label, block);

		GridLayoutFactory.fillDefaults()
				.numColumns(2)
				.spacing(LayoutConstants.getSpacing())
				.applyTo(block);
		return block;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(preferences);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(FieldEditor.VALUE)) {
    		Object source = event.getSource();
    		if (source instanceof FieldEditor) {
    			String preferenceName = ((FieldEditor) source).getPreferenceName();
				if (preferenceName.equals(PreferenceConstants.JOB_MONITORING_ENABLED)) {
    				boolean enabled = Boolean.TRUE.equals(event.getNewValue());
	    			enableDependentFields(enabled);
    			}
    		}
        }
		super.propertyChange(event);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		enableDependentFields(monitoringEnabled.getBooleanValue());
	}

	private void enableDependentFields(boolean enable) {
		for (Map.Entry<FieldEditor, Composite> entry : editors.entrySet()) {
			FieldEditor editor = entry.getKey();
			if (!editor.getPreferenceName().equals(PreferenceConstants.JOB_MONITORING_ENABLED)) {
				editor.setEnabled(enable, entry.getValue());
			}
		}
	}

	private BooleanFieldEditor createBooleanEditor(String name, String labelText,
			Composite parent) {
		BooleanFieldEditor field = new BooleanFieldEditor(name, labelText, parent);
		return addField(field, parent);
	}

	private IntegerEditor createIntegerEditor(String name, String labelText, Composite parent,
			int min, int max) {
		IntegerEditor field = new IntegerEditor(name, labelText, parent, min, max);
		return addField(field, parent);
	}

	private <T extends FieldEditor> T addField(T editor, Composite parent) {
		super.addField(editor);
		editor.fillIntoGrid(parent, 2);
		editors.put(editor, parent);
		if (!editor.getPreferenceName().equals(PreferenceConstants.JOB_MONITORING_ENABLED)) {
			boolean enabled = preferences.getBoolean(PreferenceConstants.JOB_MONITORING_ENABLED);
			editor.setEnabled(enabled, parent);
		}
		return editor;
	}
}
