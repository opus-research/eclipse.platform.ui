/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kaloyan Raev - [142228] Open unknown file types in text editor
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.dialogs.EditorsPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Extends the Editors preference page with IDE-specific settings.
 *
 * Note: want IDE settings to appear in main Editors preference page (via
 * subclassing), however the superclass, EditorsPreferencePage, is internal
 */
public class IDEEditorsPreferencePage extends EditorsPreferencePage {

	private Button openUnknownTextFilesInTextEditor;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);

		PreferenceLinkArea fileEditorsArea = new PreferenceLinkArea(composite, SWT.NONE,
				"org.eclipse.ui.preferencePages.FileEditors", IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_FileEditorsRelatedLink,//$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		fileEditorsArea.getControl().setLayoutData(data);

        PreferenceLinkArea contentTypeArea = new PreferenceLinkArea(composite, SWT.NONE,
                "org.eclipse.ui.preferencePages.ContentTypes", IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_contentTypesRelatedLink,//$NON-NLS-1$
                (IWorkbenchPreferenceContainer) getContainer(),null);

        data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        contentTypeArea.getControl().setLayoutData(data);

		PreferenceLinkArea appearanceArea = new PreferenceLinkArea(composite, SWT.NONE,
				"org.eclipse.ui.preferencePages.Views", IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_viewsRelatedLink,//$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);

		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		appearanceArea.getControl().setLayoutData(data);

		createEditorHistoryGroup(composite);

		createSpace(composite);
		createOpenUnknownTextFilesInTextEditorTabsPref(composite);
		createShowMultipleEditorTabsPref(composite);
		createAllowInplaceEditorPref(composite);
		createUseIPersistablePref(composite);
		createPromptWhenStillOpenPref(composite);
		createEditorReuseGroup(composite);
		((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).setPreferenceVisibility(editorReuseGroup, showMultipleEditorTabs);

		applyDialogFont(composite);

        super.setHelpContext(parent);

		return composite;
	}

	protected void createOpenUnknownTextFilesInTextEditorTabsPref(Composite composite) {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		openUnknownTextFilesInTextEditor = new Button(composite, SWT.CHECK);
		openUnknownTextFilesInTextEditor
				.setText(IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_openUnknownTextFilesInTextEditor);
		openUnknownTextFilesInTextEditor.setSelection(store
				.getBoolean(IDE.Preferences.OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR));
		setButtonLayoutData(openUnknownTextFilesInTextEditor);
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		openUnknownTextFilesInTextEditor.setSelection(store
				.getDefaultBoolean(IDE.Preferences.OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR));
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(IDE.Preferences.OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR,
				openUnknownTextFilesInTextEditor.getSelection());
		return super.performOk();
	}

}
