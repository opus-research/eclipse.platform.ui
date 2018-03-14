/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Snjezana Peco (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;

/**
 * This page of {@link EasymportWizard} simply asks for what to import, and asks
 * user a basic choice of how to import (import raw, infer sub- projects...)
 *
 * @since 3.12
 *
 */
public class SelectImportRootWizardPage extends WizardPage {

	static final String IMPORTED_SOURCES = SelectImportRootWizardPage.class.getName() + ".knownSources"; //$NON-NLS-1$

	private File selection;
	private boolean detectNestedProjects = true;
	private boolean configureProjects = true;
	private Set<IWorkingSet> workingSets;
	private ControlDecoration rootDirectoryTextDecorator;
	private WorkingSetConfigurationBlock workingSetsBlock;

	private Combo rootDirectoryText;

	/**
	 * Constructs a new instance of that page
	 *
	 * @param wizard
	 *            the container wizard (most likely an {@link EasymportWizard}
	 * @param initialSelection
	 *            initial selection (directory or archive)
	 * @param initialWorkingSets
	 *            initial working sets
	 */
	public SelectImportRootWizardPage(IWizard wizard, File initialSelection, Set<IWorkingSet> initialWorkingSets) {
		super(SelectImportRootWizardPage.class.getName());
		this.selection = initialSelection;
		this.workingSets = initialWorkingSets;
		if (this.workingSets == null) {
			this.workingSets = new HashSet<>();
		}
		setWizard(wizard);
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(DataTransferMessages.EasymportWizardPage_importProjectsInFolderTitle);
		setDescription(DataTransferMessages.EasymportWizardPage_importProjectsInFolderDescription);
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(4, false));
		Label rootDirectoryLabel = new Label(res, SWT.NONE);
		rootDirectoryLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		rootDirectoryLabel.setText(DataTransferMessages.EasymportWizardPage_selectRootDirectory);
		rootDirectoryText = new Combo(res, SWT.BORDER);
		String[] knownSources = getWizard().getDialogSettings().getArray(IMPORTED_SOURCES);
		if (knownSources != null) {
			rootDirectoryText.setItems(knownSources);
		}
		rootDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rootDirectoryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				SelectImportRootWizardPage.this.selection = new File(((Combo) e.widget).getText());
				SelectImportRootWizardPage.this.validatePage();
			}
		});
		this.rootDirectoryTextDecorator = new ControlDecoration(rootDirectoryText, SWT.TOP | SWT.LEFT);
		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		this.rootDirectoryTextDecorator.setImage(errorImage);
		this.rootDirectoryTextDecorator
				.setDescriptionText(DataTransferMessages.EasymportWizardPage_incorrectRootDirectory);
		this.rootDirectoryTextDecorator.hide();
		Button directoryButton = new Button(res, SWT.PUSH);
		directoryButton.setText(DataTransferMessages.EasymportWizardPage_browse);
		directoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText(DataTransferMessages.selectFolderToImport);
				if (rootDirectoryText.getText() != null) {
					File current = new File(rootDirectoryText.getText());
					if (current.isDirectory()) {
						dialog.setFilterPath(current.getAbsolutePath());
					} else if (current.isFile()) {
						dialog.setFilterPath(current.getParentFile().getAbsolutePath());
					}
				}
				if (dialog.getFilterPath() == null) {
					dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
				}
				String directory = dialog.open();
				if (directory != null) {
					rootDirectoryText.setText(directory);
					SelectImportRootWizardPage.this.selection = new File(directory);
					SelectImportRootWizardPage.this.validatePage();
				}
			}
		});
		Button browseZipButton = new Button(res, SWT.PUSH);
		browseZipButton.setText(DataTransferMessages.EasymportWizardPage_selectArchiveButton);
		browseZipButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setText(DataTransferMessages.EasymportWizardPage_selectArchiveTitle);
				dialog.setFilterExtensions(new String[] { "*.zip;*.tar;*.tar.gz" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { DataTransferMessages.EasymportWizardPage_allSupportedArchives });
				if (rootDirectoryText.getText() != null) {
					File current = new File(rootDirectoryText.getText());
					if (current.isDirectory()) {
						dialog.setFilterPath(current.getAbsolutePath());
					} else if (current.isFile()) {
						dialog.setFilterPath(current.getParentFile().getAbsolutePath());
						dialog.setFileName(current.getName());
					}
				}
				if (dialog.getFilterPath() == null) {
					dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
				}
				String zip = dialog.open();
				if (zip != null) {
					rootDirectoryText.setText(zip);
					SelectImportRootWizardPage.this.selection = new File(zip);
					SelectImportRootWizardPage.this.validatePage();
				}
			}
		});

		final Button importRawProjectRadio = new Button(res, SWT.RADIO);
		importRawProjectRadio.setText(DataTransferMessages.EasymportWizardPage_importRawProject);
		importRawProjectRadio.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		importRawProjectRadio.setSelection(!this.detectNestedProjects);
		importRawProjectRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = importRawProjectRadio.getSelection();
				if (selected) {
					SelectImportRootWizardPage.this.detectNestedProjects = false;
					SelectImportRootWizardPage.this.configureProjects = false;
				}
				setPageComplete(isPageComplete());
			}
		});
		final Button importAndConfigureProjectRadio = new Button(res, SWT.RADIO);
		importAndConfigureProjectRadio.setText(DataTransferMessages.EasymportWizardPage_importAndConfigureProject);
		importAndConfigureProjectRadio.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		importAndConfigureProjectRadio.setSelection(!this.detectNestedProjects);
		importAndConfigureProjectRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = importAndConfigureProjectRadio.getSelection();
				if (selected) {
					SelectImportRootWizardPage.this.detectNestedProjects = false;
					SelectImportRootWizardPage.this.configureProjects = true;
				}
				setPageComplete(isPageComplete());
			}
		});
		final Button detectNestedProjectCheckbox = new Button(res, SWT.RADIO);
		detectNestedProjectCheckbox.setText(DataTransferMessages.EasymportWizardPage_detectNestedProjects);
		detectNestedProjectCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		detectNestedProjectCheckbox.setSelection(this.detectNestedProjects);
		detectNestedProjectCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = detectNestedProjectCheckbox.getSelection();
				if (selected) {
					SelectImportRootWizardPage.this.detectNestedProjects = true;
					SelectImportRootWizardPage.this.configureProjects = true;
				}
				setPageComplete(isPageComplete());
			}
		});
		Link showDetectorsLink = new Link(res, SWT.NONE);
		showDetectorsLink.setText("<A>" + DataTransferMessages.EasymportWizardPage_showAvailableDetectors + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
		showDetectorsLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		showDetectorsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder message = new StringBuilder();
				message.append(DataTransferMessages.EasymportWizardPage_availableDetectors_description);
				message.append('\n');
				message.append('\n');
				List<String> extensionsLabels = new ArrayList<>(
						ProjectConfiguratorExtensionManager.getAllExtensionLabels());
				Collections.sort(extensionsLabels);
				for (String extensionLabel : extensionsLabels) {
					message.append("* "); //$NON-NLS-1$
					message.append(extensionLabel);
					message.append('\n');
				}
				MessageDialog.openInformation(getShell(),
						DataTransferMessages.EasymportWizardPage_availableDetectors_title, message.toString());
			}
		});

		Group workingSetsGroup = new Group(res, SWT.NONE);
		workingSetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		workingSetsGroup.setLayout(new GridLayout(1, false));
		workingSetsGroup.setText(DataTransferMessages.EasymportWizardPage_workingSets);
		workingSetsBlock = new WorkingSetConfigurationBlock(new String[] { "org.eclipse.ui.resourceWorkingSetPage" }, getDialogSettings()); //$NON-NLS-1$
		if (this.workingSets != null) {
			workingSetsBlock.setWorkingSets(this.workingSets.toArray(new IWorkingSet[this.workingSets.size()]));
		}
		workingSetsBlock.createContent(workingSetsGroup);

		if (this.selection == null && knownSources != null && knownSources.length > 0) {
			this.selection = new File(knownSources[0]);
		}
		if (this.selection != null) {
			rootDirectoryText.setText(this.selection.getAbsolutePath());
			validatePage();
		}

		setControl(res);
	}

	protected void validatePage() {
		if (!isPageComplete()) {
			this.rootDirectoryTextDecorator.show();
			setErrorMessage(this.rootDirectoryTextDecorator.getDescriptionText());
		} else {
			this.rootDirectoryTextDecorator.hide();
			setErrorMessage(null);
		}
		setPageComplete(isPageComplete());
	}

	@Override
	public boolean isPageComplete() {
		return this.selection != null && (this.selection.isDirectory() || isValidArchive(this.selection));
	}

	boolean isValidArchive(File file) {
		return ArchiveFileManipulations.isZipFile(file.getAbsolutePath())
				|| ArchiveFileManipulations.isTarFile(file.getAbsolutePath());
	}


	/**
	 *
	 * @return The selected source of import (can be a directory or an archive)
	 */
	public File getSelectedRoot() {
		return this.selection;
	}

	/**
	 * Sets the initial source to import.
	 *
	 * @param directoryOrArchive
	 */
	public void setInitialImportRoot(File directoryOrArchive) {
		this.selection = directoryOrArchive;
		this.rootDirectoryText.setText(directoryOrArchive.getAbsolutePath());
	}

	/**
	 *
	 * @return The user selected working sets to assign to imported projects.
	 */
	public Set<IWorkingSet> getSelectedWorkingSets() {
		this.workingSets.clear();
		// workingSetsBlock doesn't support listeners...
		Runnable workingSetsRetriever = new Runnable() {
			@Override
			public void run() {
				for (IWorkingSet workingSet : SelectImportRootWizardPage.this.workingSetsBlock.getSelectedWorkingSets()) {
					SelectImportRootWizardPage.this.workingSets.add(workingSet);
				}
			}
		};
		if (Display.getCurrent() == null) {
			getContainer().getShell().getDisplay().syncExec(workingSetsRetriever);
		} else {
			workingSetsRetriever.run();
		}
		return this.workingSets;
	}

	/**
	 * @return whether the job should recurse to find nested projects
	 */
	public boolean isDetectNestedProject() {
		return this.detectNestedProjects;
	}

	/**
	 * @return whether the job should configure projects (add natures etc...)
	 */
	public boolean isConfigureProjects() {
		return this.configureProjects;
	}

}

