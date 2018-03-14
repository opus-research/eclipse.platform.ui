/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids  <sdavids@gmx.de> - Fix for Bug 132156 [Dialogs] Progress Preferences dialog problems
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.e4new.PreferenceStore;
import org.eclipse.e4.ui.progress.legacy.ViewSettingsDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The JobsViewPreferenceDialog is the dialog that
 * allows the user to set the preferences.
 */
public class JobsViewPreferenceDialog extends ViewSettingsDialog {

	private BooleanFieldEditor showSystemJob;
	private BooleanFieldEditor runInBackground;
	private IPreferenceStore preferenceStore = new PreferenceStore();

	/**
	 * Create a new instance of the receiver.
	 * @param parentShell
	 */
	public JobsViewPreferenceDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create a new instance of the receiver.
	 * @param parentShell
	 */
	public JobsViewPreferenceDialog(Shell parentShell, IPreferenceStore preferenceStore) {
		super(parentShell);
		this.preferenceStore = preferenceStore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ProgressMessages.JobsViewPreferenceDialog_Title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite) super.createDialogArea(parent);
		
		Composite editArea = new Composite(top, SWT.NONE);
		editArea.setLayout(new GridLayout());
		editArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		
		runInBackground = new BooleanFieldEditor(IProgressConstants.RUN_IN_BACKGROUND, ProgressMessages.JobsViewPreferenceDialog_RunInBackground, editArea);//$NON-NLS-1$
		runInBackground.setPreferenceName(IProgressConstants.RUN_IN_BACKGROUND);
		runInBackground.setPreferenceStore(preferenceStore);
		runInBackground.load();
		
		showSystemJob = new BooleanFieldEditor(IProgressConstants.SHOW_SYSTEM_JOBS, ProgressMessages.JobsViewPreferenceDialog_ShowSystemJobs, editArea);//$NON-NLS-1$
		showSystemJob.setPreferenceName(IProgressConstants.SHOW_SYSTEM_JOBS);
		showSystemJob.setPreferenceStore(preferenceStore);
		showSystemJob.load();
		
		Dialog.applyDialogFont(top);
		
		return top;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		runInBackground.store();
		showSystemJob.store();
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		runInBackground.loadDefault();
		showSystemJob.loadDefault();
	}
}
