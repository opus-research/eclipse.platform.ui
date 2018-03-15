/*******************************************************************************
 * Copyright (c) 2016 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Richard <axel.richard@obeo.fr> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Preference page that allows to enable auto-save for textual editors.
 *
 * @since 3.12
 *
 */
public class AutoSavePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button autoSaveButton;

	private Group autoSaveGroup;

	private Composite intervalComposite;

	private Label intervalMessageBegin;

	private Composite intervalFieldComposite;

	private IntegerFieldEditor intervalField;

	private Label intervalMessageEnd;

	private Label resetMessage;

	private Label noteMessage;

	private IPropertyChangeListener validityChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updateValidState();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do here.
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);
		createAutoSaveCheckbox(composite);
		createAutoSaveGroup(composite);
		createIntervalPart();
		createMessagesPart();
		return composite;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		boolean autoSave = getPreferenceStore().getDefaultBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
		autoSaveButton.setSelection(autoSave);
		int interval = getPreferenceStore().getDefaultInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		intervalField.setStringValue(String.valueOf(interval));
		intervalField.setEnabled(autoSave, intervalFieldComposite);

		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (isValid()) {
			getPreferenceStore().setValue(IDEInternalPreferences.SAVE_AUTOMATICALLY, autoSaveButton.getSelection());
			getPreferenceStore().setValue(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL,
					intervalField.getTextControl(intervalFieldComposite).getText());
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		intervalField.setPropertyChangeListener(null);
		noteMessage.dispose();
		resetMessage.dispose();
		intervalMessageEnd.dispose();
		intervalField.dispose();
		intervalMessageBegin.dispose();
		intervalFieldComposite.dispose();
		intervalComposite.dispose();
		autoSaveGroup.dispose();
		autoSaveButton.dispose();
		super.dispose();
	}

	/**
	 * For tests purpose only. Check auto-save checkbox button state
	 * programmatically. A call of this method when the auto-save preference
	 * page is closed will return false.
	 *
	 * @return true if auto-save checkbox button is selected, false otherwise
	 */
	protected boolean isAutoSaveButtonSelected() {
		final boolean enabled;
		if (autoSaveButton != null && !autoSaveButton.isDisposed()) {
			enabled = autoSaveButton.getSelection();
		} else {
			enabled = false;
		}
		return enabled;
	}

	/**
	 * For tests purpose only. Select/unselect auto-save checkbox button
	 * programmatically. A call of this method when the auto-save preference
	 * page is closed won't do anything.
	 *
	 * @param enable
	 *            true to select auto-save, false to unselect
	 */
	protected void selectAutoSaveButton(boolean enable) {
		if (autoSaveButton != null && !autoSaveButton.isDisposed()) {
			autoSaveButton.setSelection(enable);
			autoSaveButton.notifyListeners(SWT.Selection, new Event());
		}
	}

	/**
	 * For tests purpose only. Get auto-save interval text field value
	 * programmatically. A call of this method when the auto-save preference
	 * page is closed will return 0.
	 *
	 * @return the value contained in the interval text field, 0 otherwise.
	 */
	protected int getAutoSaveIntervalTextValue() {
		final int interval;
		if (intervalField != null && intervalFieldComposite != null
				&& !intervalField.getTextControl(intervalFieldComposite).isDisposed()) {
			interval = intervalField.getIntValue();
		} else {
			interval = 0;
		}
		return interval;
	}

	/**
	 * For tests purpose only.Set auto-save interval text field value
	 * programmatically. A call of this method when the auto-save preference
	 * page is closed won't do anything.
	 *
	 * @param interval
	 *            the value to set (between 0 and Integer.MAX_VALUE).
	 */
	protected void setAutoSaveIntervalTextValue(int interval) {
		if (intervalField != null && intervalFieldComposite != null
				&& !intervalField.getTextControl(intervalFieldComposite).isDisposed() && autoSaveButton != null
				&& !autoSaveButton.isDisposed() && autoSaveButton.getSelection()) {
			intervalField.setStringValue(String.valueOf(interval));
		}
	}

	/**
	 * Update 'Apply' button state based on the value of the interval.
	 */
	protected void updateValidState() {
		if (intervalField != null && !intervalField.isValid()) {
			setValid(false);
		} else {
			setValid(true);
		}

	}

	/**
	 * Creates the top-level widget that will contain all widgets of this
	 * preference page.
	 *
	 * @param parent
	 *            the parent composite
	 * @return the new widget
	 */
	protected Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		return composite;
	}

	/**
	 * Creates a checkbox widget, that will allow to enable/disable the
	 * auto-save of editors in eclipse.
	 *
	 * @param composite
	 *            the parent composite
	 */
	private void createAutoSaveCheckbox(Composite composite) {
		autoSaveButton = new Button(composite, SWT.CHECK);
		autoSaveButton.setText(IDEWorkbenchMessages.AutoSavePreferencPage_autoSaveButton);
		autoSaveButton.setToolTipText(IDEWorkbenchMessages.AutoSavePreferencPage_autoSaveButton);
		autoSaveButton.setSelection(getPreferenceStore().getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY));
		autoSaveButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean autoSave = autoSaveButton.getSelection();
				getPreferenceStore().setValue(IDEInternalPreferences.SAVE_AUTOMATICALLY, autoSave);
				Display display = autoSaveButton.getDisplay();
				noteMessage.setEnabled(autoSave);
				resetMessage.setEnabled(autoSave);
				intervalMessageEnd.setEnabled(autoSave);
				intervalField.getTextControl(intervalFieldComposite).setEnabled(autoSave);
				intervalMessageBegin.setEnabled(autoSave);
				intervalFieldComposite.setEnabled(autoSave);
				intervalComposite.setEnabled(autoSave);
				autoSaveGroup.setEnabled(autoSave);
				if (autoSave) {
					noteMessage.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					resetMessage.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					intervalMessageEnd.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					intervalMessageBegin.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				} else {
					noteMessage.setForeground(display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					resetMessage.setForeground(display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					intervalMessageEnd.setForeground(display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					intervalMessageBegin.setForeground(display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do here
			}
		});
	}

	/**
	 * Creates a group widget that will contain the interval text field and
	 * messages.
	 *
	 * @param composite
	 *            the parent composite
	 */
	private void createAutoSaveGroup(Composite composite) {
		autoSaveGroup = new Group(composite, SWT.NONE);
		GridLayout autoSaveGroupLayout = new GridLayout();
		autoSaveGroupLayout.numColumns = 1;
		autoSaveGroupLayout.marginWidth = 0;
		autoSaveGroupLayout.marginHeight = 0;
		autoSaveGroup.setLayout(autoSaveGroupLayout);
		GridData autoSaveGroupLayoutData = new GridData();
		autoSaveGroupLayoutData.horizontalAlignment = GridData.FILL;
		autoSaveGroupLayoutData.grabExcessHorizontalSpace = true;
		autoSaveGroup.setLayoutData(autoSaveGroupLayoutData);
		autoSaveGroup.setEnabled(autoSaveButton.getSelection());
	}

	/**
	 * Creates a widget with a text, a text field (to handle the interval) and a
	 * text.
	 */
	private void createIntervalPart() {
		intervalComposite = new Composite(autoSaveGroup, SWT.NONE);
		GridLayout intervalCompositeLayout = new GridLayout();
		intervalCompositeLayout.numColumns = 3;
		intervalCompositeLayout.marginWidth = 0;
		intervalCompositeLayout.marginHeight = 10;
		intervalComposite.setLayout(intervalCompositeLayout);
		GridData intervalCompositeLayoutData = new GridData();
		intervalCompositeLayoutData.horizontalAlignment = GridData.FILL;
		intervalCompositeLayoutData.grabExcessHorizontalSpace = true;
		intervalComposite.setLayoutData(intervalCompositeLayoutData);
		intervalComposite.setEnabled(autoSaveButton.getSelection());

		intervalMessageBegin = new Label(intervalComposite, SWT.NONE);
		intervalMessageBegin.setText(IDEWorkbenchMessages.AutoSavePreferencPage_intervalMessageBegin);

		intervalFieldComposite = new Composite(intervalComposite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		intervalFieldComposite.setLayout(layout);
		intervalFieldComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		intervalField = new IntegerFieldEditor(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL, "", //$NON-NLS-1$
				intervalFieldComposite);

		intervalField.setPreferenceStore(IDEWorkbenchPlugin.getDefault().getPreferenceStore());
		intervalField.setPage(this);
		intervalField.setTextLimit(10);
		intervalField.setErrorMessage(IDEWorkbenchMessages.AutoSavePreferencPage_errorMessage);
		intervalField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		intervalField.setValidRange(1, Integer.MAX_VALUE);
		intervalField.load();
		intervalField.getLabelControl(intervalFieldComposite).setEnabled(autoSaveButton.getSelection());
		intervalField.getTextControl(intervalFieldComposite).setEnabled(autoSaveButton.getSelection());
		intervalField.setPropertyChangeListener(validityChangeListener);

		intervalMessageEnd = new Label(intervalComposite, NONE);
		intervalMessageEnd.setText(IDEWorkbenchMessages.AutoSavePreferencPage_intervalMessageEnd);
	}

	/**
	 * Creates information messages.
	 */
	private void createMessagesPart() {
		resetMessage = new Label(autoSaveGroup, NONE);
		resetMessage.setText(IDEWorkbenchMessages.AutoSavePreferencPage_resetMessage);

		noteMessage = new Label(autoSaveGroup, NONE);
		noteMessage.setText(
				IDEWorkbenchMessages.AutoSavePreferencPage_noteMessageBegin + System.getProperty("line.separator") //$NON-NLS-1$
						+ IDEWorkbenchMessages.AutoSavePreferencPage_noteMessageEnd);
	}

}
