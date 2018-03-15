package org.eclipse.ui.internal.ide;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that prompts the user to import the preferences from another workspace
 * location.
 *
 * @since 3.13
 *
 */
public class ImportPreferencesDialog extends MessageDialog {


	private ChooseWorkspaceData launchData;

	/**
	 * @param parentShell
	 * @param launchData
	 */
	public ImportPreferencesDialog(Shell parentShell, ChooseWorkspaceData launchData) {
		super(parentShell, IDEWorkbenchMessages.ImportPreferencesDialog_dialogTitle, null,
				IDEWorkbenchMessages.ImportPreferencesDialog_message, MessageDialog.NONE, 0,
				new String[] { IDEWorkbenchMessages.ImportPreferencesDialog_importButtonLabel,
						IDialogConstants.CANCEL_LABEL });
		this.launchData = launchData;
		setShellStyle(SWT.SHEET);
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createBrowseRow(composite);
		createAlwaysUseThisCheckbox(composite);
		return composite;
	}

	private void createBrowseRow(Composite parent) {
		Combo text = new Combo(parent, SWT.BORDER | SWT.LEAD | SWT.DROP_DOWN);
		text.setFocus();
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		for (String recentWorkspace : launchData.getRecentWorkspaces()) {
			if (recentWorkspace != null) {
				text.add(recentWorkspace);
			}
		}
		text.select(0);

		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		browseButton.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_browseLabel);
		setButtonLayoutData(browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
				dialog.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_directoryBrowserTitle);
				dialog.setMessage(IDEWorkbenchMessages.ChooseWorkspaceDialog_directoryBrowserMessage);
				dialog.setFilterPath(text.getText());
				String dir = dialog.open();
				if (dir != null) {
					text.setText(TextProcessor.process(dir));
				}
			}
		});
	}

	private void createAlwaysUseThisCheckbox(Composite parent) {
		Button alwaysUseThisCheckbox = new Button(parent, SWT.CHECK);
		alwaysUseThisCheckbox.setText(IDEWorkbenchMessages.ImportPreferencesDialog_alwaysUseSelectedButtonLabel);
	}
}
