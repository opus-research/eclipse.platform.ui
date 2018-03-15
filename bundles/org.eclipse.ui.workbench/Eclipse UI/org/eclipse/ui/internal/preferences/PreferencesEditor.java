/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;
import org.eclipse.ui.part.EditorPart;

public class PreferencesEditor extends EditorPart {

	public static final String EDITOR_ID = "org.eclipse.ui.ide.preferencesEditor"; //$NON-NLS-1$

	public static final class PreferencesEditorInput implements IEditorInput {

		public static PreferencesEditorInput INSTANCE;

		public static final class PreferenceEditorInputFactory implements IElementFactory {
			protected static final String FACTORY_ID = "org.eclipse.ui.ide.preferencesEditorInputFactory"; //$NON-NLS-1$

			@Override
			public IAdaptable createElement(IMemento memento) {
				return PreferencesEditorInput.INSTANCE;
			}

		}

		static {
			INSTANCE = new PreferencesEditorInput();
		}

		private PreferencesEditorInput() {
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return JFaceResources.getString("PreferenceDialog.title"); //$NON-NLS-1$
		}

		@Override
		public IPersistableElement getPersistable() {
			return new IPersistableElement() {
				@Override
				public void saveState(IMemento memento) {
				}
				@Override
				public String getFactoryId() {
					return PreferenceEditorInputFactory.FACTORY_ID;
				}
			};
		}

		@Override
		public String getToolTipText() {
			return JFaceResources.getString("PreferenceDialog.title"); //$NON-NLS-1$
		}

	}

	WorkbenchPreferenceDialog dialog = null;

	public PreferencesEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!dialog.isCurrentPageValid()) {
			MessageDialog.openError(getSite().getShell(), WorkbenchMessages.PreferenceEditor_couldNotSave,
					WorkbenchMessages.PreferenceEditor_invalidContent);
		} else {
			dialog.okPressed();
		}
	}

	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(new PreferencesEditorInput());
		final PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
		dialog = new WorkbenchPreferenceDialog(site.getShell(), preferenceManager);
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		parent.setLayout(layout);
		Control res = dialog.createDialogArea(parent);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		res.setLayoutData(layoutData);
		// create buttons but make them invisible
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
		dialog.createButtonsForButtonBar(buttonComposite);
		GridDataFactory.swtDefaults().exclude(true).applyTo(buttonComposite);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		super.dispose();
		dialog.close();
	}
}
