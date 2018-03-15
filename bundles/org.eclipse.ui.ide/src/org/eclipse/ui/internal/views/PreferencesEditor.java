package org.eclipse.ui.internal.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;
import org.eclipse.ui.part.EditorPart;

public class PreferencesEditor extends EditorPart {

	private static final class PreferencesEditorInput implements IEditorInput {

		private PreferenceManager manager;

		public PreferencesEditorInput(PreferenceManager manager) {
			this.manager = manager;
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
			return "Preferences";
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			// TODO Auto-generated method stub
			return "Preferences";
		}

	}

	WorkbenchPreferenceDialog dialog = null;

	public PreferencesEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!dialog.isCurrentPageValid()) {
			MessageDialog.openError(getSite().getShell(), "Invalid value on page",
					"Current page contains invalid values, please fix them first");
		} else {
			dialog.okPressed();
		}
	}

	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		final PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
		setInput(new PreferencesEditorInput(preferenceManager));
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		dialog.close();
	}
}
