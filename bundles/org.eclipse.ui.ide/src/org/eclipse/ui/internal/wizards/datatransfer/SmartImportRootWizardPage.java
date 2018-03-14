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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

/**
 * This page of {@link SmartImportWizard} simply asks for what to import, and asks
 * user a basic choice of how to import (import raw, infer sub- projects...)
 *
 * @since 3.12
 *
 */
public class SmartImportRootWizardPage extends WizardPage {

	static final String IMPORTED_SOURCES = SmartImportRootWizardPage.class.getName() + ".knownSources"; //$NON-NLS-1$

	private File selection;
	private boolean detectNestedProjects = true;
	private boolean configureProjects = true;
	private Set<IWorkingSet> workingSets;
	private ControlDecoration rootDirectoryTextDecorator;
	private WorkingSetConfigurationBlock workingSetsBlock;

	private Combo rootDirectoryText;
	// Proposal part
	private CheckboxTreeViewer tree;
	private Set<File> alreadyExistingProjects;
	private Set<File> notAlreadyExistingProjects;
	private Label selectionSummary;
	protected Map<File, List<ProjectConfigurator>> potentialProjects;

	private class FolderForProjectsLabelProvider extends CellLabelProvider implements IColorProvider {
		public String getText(Object o) {
			File file = (File) o;
			String label = file.getAbsolutePath();
			if (label.startsWith(getWizard().getImportJob().getRoot().getAbsolutePath())) {
				label = label
						.substring(getWizard().getImportJob().getRoot().getParentFile().getAbsolutePath().length() + 1);
			}
			return label;
		}

		@Override
		public Color getBackground(Object o) {
			return null;
		}

		@Override
		public Color getForeground(Object o) {
			if (alreadyExistingProjects.contains(o)) {
				return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			Color color = getForeground(cell.getElement());
			if (color != null) {
				cell.setForeground(color);
			}
		}
	}

	private class ProjectConfiguratorLabelProvider extends CellLabelProvider implements IColorProvider {
		public String getText(Object o) {
			File file = (File) o;
			if (alreadyExistingProjects.contains(file)) {
				return DataTransferMessages.SmartImportProposals_alreadyImportedAsProject_title;
			}
			List<ProjectConfigurator> configurators = SmartImportRootWizardPage.this.potentialProjects.get(file);
			if (configurators.isEmpty()) {
				return ""; //$NON-NLS-1$
			}
			return ProjectConfiguratorExtensionManager.getLabel(configurators.get(0));
		}

		@Override
		public Color getBackground(Object o) {
			return null;
		}

		@Override
		public Color getForeground(Object o) {
			if (alreadyExistingProjects.contains(o)) {
				return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			Color color = getForeground(cell.getElement());
			if (color != null) {
				cell.setForeground(color);
			}
		}
	}

	/**
	 * Constructs a new instance of that page
	 *
	 * @param wizard
	 *            the container wizard (most likely an {@link SmartImportWizard}
	 * @param initialSelection
	 *            initial selection (directory or archive)
	 * @param initialWorkingSets
	 *            initial working sets
	 */
	public SmartImportRootWizardPage(SmartImportWizard wizard, File initialSelection,
			Set<IWorkingSet> initialWorkingSets) {
		super(SmartImportRootWizardPage.class.getName());
		this.selection = initialSelection;
		this.workingSets = initialWorkingSets;
		if (this.workingSets == null) {
			this.workingSets = new HashSet<>();
		}
		setWizard(wizard);
	}

	@Override
	public void setWizard(IWizard easymportWizard) {
		Assert.isTrue(easymportWizard instanceof SmartImportWizard);
		super.setWizard(easymportWizard);
	}

	@Override
	public SmartImportWizard getWizard() {
		return (SmartImportWizard) super.getWizard();
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(DataTransferMessages.SmartImportWizardPage_importProjectsInFolderTitle);
		setDescription(DataTransferMessages.SmartImportWizardPage_importProjectsInFolderDescription);
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(4, false));
		Label rootDirectoryLabel = new Label(res, SWT.NONE);
		rootDirectoryLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		rootDirectoryLabel.setText(DataTransferMessages.SmartImportWizardPage_selectRootDirectory);
		rootDirectoryText = new Combo(res, SWT.BORDER);
		String[] knownSources = getWizard().getDialogSettings().getArray(IMPORTED_SOURCES);
		if (knownSources != null) {
			rootDirectoryText.setItems(knownSources);
		}
		GridData rootDirectoryTextLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		rootDirectoryText.setLayoutData(rootDirectoryTextLayoutData);
		rootDirectoryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				SmartImportRootWizardPage.this.selection = new File(((Combo) e.widget).getText());
				updateProposalsTreeContent();
				SmartImportRootWizardPage.this.validatePage();
			}
		});
		this.rootDirectoryTextDecorator = new ControlDecoration(rootDirectoryText, SWT.TOP | SWT.LEFT);
		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		rootDirectoryTextLayoutData.horizontalIndent += errorImage.getBounds().width;
		this.rootDirectoryTextDecorator.setImage(errorImage);
		this.rootDirectoryTextDecorator
				.setDescriptionText(DataTransferMessages.SmartImportWizardPage_incorrectRootDirectory);
		this.rootDirectoryTextDecorator.hide();
		Button directoryButton = new Button(res, SWT.PUSH);
		directoryButton.setText(DataTransferMessages.SmartImportWizardPage_browse);
		directoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText(DataTransferMessages.SmartImportWizardPage_selectFolderOrArchiveToImport);
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
					SmartImportRootWizardPage.this.selection = new File(directory);
					proposalsSelectionChanged();
					SmartImportRootWizardPage.this.validatePage();
				}
			}
		});
		Button browseZipButton = new Button(res, SWT.PUSH);
		browseZipButton.setText(DataTransferMessages.SmartImportWizardPage_selectArchiveButton);
		browseZipButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setText(DataTransferMessages.SmartImportWizardPage_selectArchiveTitle);
				dialog.setFilterExtensions(new String[] { "*.zip;*.tar;*.tar.gz" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { DataTransferMessages.SmartImportWizardPage_allSupportedArchives });
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
					SmartImportRootWizardPage.this.selection = new File(zip);
					updateProposalsTreeContent();
					SmartImportRootWizardPage.this.validatePage();
				}
			}
		});

		Link showDetectorsLink = new Link(res, SWT.NONE);
		showDetectorsLink.setText(DataTransferMessages.SmartImportWizardPage_showAvailableDetectors);
		showDetectorsLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		showDetectorsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder message = new StringBuilder();
				message.append(DataTransferMessages.SmartImportWizardPage_availableDetectors_description);
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
						DataTransferMessages.SmartImportWizardPage_availableDetectors_title, message.toString());
			}
		});
		final Button detectNestedProjectsCheckbox = new Button(res, SWT.CHECK);
		detectNestedProjectsCheckbox.setText(DataTransferMessages.SmartImportWizardPage_detectNestedProjects);
		detectNestedProjectsCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		detectNestedProjectsCheckbox.setSelection(this.detectNestedProjects);
		detectNestedProjectsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SmartImportRootWizardPage.this.detectNestedProjects = detectNestedProjectsCheckbox.getSelection();
				updateProposalsTreeContent();
				setPageComplete(isPageComplete());
			}
		});
		final Button configureProjectsCheckbox = new Button(res, SWT.CHECK);
		configureProjectsCheckbox.setText(DataTransferMessages.SmartImportWizardPage_configureProjects);
		configureProjectsCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		configureProjectsCheckbox.setSelection(this.configureProjects);
		configureProjectsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SmartImportRootWizardPage.this.configureProjects = configureProjectsCheckbox.getSelection();
				updateProposalsTreeContent();
				setPageComplete(isPageComplete());
			}
		});

		Composite proposalParent = new Composite(res, SWT.NONE);
		proposalParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		proposalParent.setLayout(new FillLayout());
		createProposalsGroup(proposalParent);

		Group workingSetsGroup = new Group(res, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		layoutData.verticalIndent = 20;
		workingSetsGroup.setLayoutData(layoutData);
		workingSetsGroup.setLayout(new GridLayout(1, false));
		workingSetsGroup.setText(DataTransferMessages.SmartImportWizardPage_workingSets);
		workingSetsBlock = new WorkingSetConfigurationBlock(getDialogSettings(),
				"org.eclipse.ui.resourceWorkingSetPage"); //$NON-NLS-1$
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

	/**
	 * @param res
	 */
	private void createProposalsGroup(Composite parent) {
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(2, false));
		selectionSummary = new Label(res, SWT.NONE);
		selectionSummary.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		selectionSummary.setText(NLS.bind(DataTransferMessages.SmartImportProposals_selectionSummary, 0, 0));
		FilteredTree filterTree = new FilteredTree(res, SWT.BORDER | SWT.CHECK, new PatternFilter(), true) {
			@Override
			public CheckboxTreeViewer doCreateTreeViewer(Composite treeParent, int style) {
				return new CheckboxTreeViewer(treeParent, style);
			}
		};
		tree = (CheckboxTreeViewer) filterTree.getViewer();
		GridData treeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeGridData.heightHint = 90;
		tree.getControl().setLayoutData(treeGridData);
		tree.setContentProvider(new ITreeContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				Map<File, ?> potentialProjects = (Map<File, ?>) inputElement;
				return potentialProjects.keySet().toArray(new File[potentialProjects.size()]);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}

		});
		tree.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer v, Object o1, Object o2) {
				return ((File) o1).getAbsolutePath().compareTo(((File) o2).getAbsolutePath());
			}
		});
		tree.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				return getWizard().getImportJob() == null || getWizard().getImportJob().getDirectoriesToImport() == null
						|| getWizard().getImportJob().getDirectoriesToImport().contains(element);
			}
		});
		tree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (SmartImportRootWizardPage.this.alreadyExistingProjects.contains(event.getElement())) {
					tree.setChecked(event.getElement(), false);
				} else {
					proposalsSelectionChanged();
				}
			}
		});

		tree.getTree().setHeaderVisible(true);
		ViewerColumn pathColumn = new TreeViewerColumn(tree, SWT.NONE);
		pathColumn.setLabelProvider(new FolderForProjectsLabelProvider());
		tree.getTree().getColumn(0).setText(DataTransferMessages.SmartImportProposals_folder);
		tree.getTree().getColumn(0).setWidth(400);
		ViewerColumn projectTypeColumn = new TreeViewerColumn(tree, SWT.NONE);
		projectTypeColumn.setLabelProvider(new ProjectConfiguratorLabelProvider());
		tree.getTree().getColumn(1).setText(DataTransferMessages.SmartImportProposals_importAs);
		tree.getTree().getColumn(1).setWidth(250);

		Composite selectionButtonsGroup = new Composite(res, SWT.NONE);
		selectionButtonsGroup.setLayout(new GridLayout(1, false));
		selectionButtonsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		Button selectAllButton = new Button(selectionButtonsGroup, SWT.PUSH);
		selectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		selectAllButton.setText(DataTransferMessages.DataTransfer_selectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tree.setCheckedElements(SmartImportRootWizardPage.this.notAlreadyExistingProjects.toArray());
				proposalsSelectionChanged();
			}
		});
		Button deselectAllButton = new Button(selectionButtonsGroup, SWT.PUSH);
		deselectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		deselectAllButton.setText(DataTransferMessages.DataTransfer_deselectAll);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tree.setCheckedElements(new Object[0]);
				proposalsSelectionChanged();
			}
		});

		tree.setInput(Collections.emptyMap());
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
		return sourceIsValid() && getWizard().getImportJob() != null
				&& tree.getCheckedElements().length > 0;
	}

	private boolean sourceIsValid() {
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
				for (IWorkingSet workingSet : SmartImportRootWizardPage.this.workingSetsBlock.getSelectedWorkingSets()) {
					SmartImportRootWizardPage.this.workingSets.add(workingSet);
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

	private void updateProposalsTreeContent() {
		if (sourceIsValid()) {
			try {
				getContainer().run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						SmartImportRootWizardPage.this.potentialProjects = getWizard().getImportJob()
								.getImportProposals(monitor);
						if (!potentialProjects.containsKey(getWizard().getImportJob().getRoot())) {
							potentialProjects.put(getWizard().getImportJob().getRoot(), Collections.emptyList());
						}

						SmartImportRootWizardPage.this.notAlreadyExistingProjects = new HashSet<>(
								potentialProjects.keySet());
						SmartImportRootWizardPage.this.alreadyExistingProjects = new HashSet<>();
						for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
							SmartImportRootWizardPage.this.notAlreadyExistingProjects
									.remove(project.getLocation().toFile());
							SmartImportRootWizardPage.this.alreadyExistingProjects.add(project.getLocation().toFile());
						}
					}
				});
				tree.setInput(potentialProjects);
				tree.setCheckedElements(SmartImportRootWizardPage.this.notAlreadyExistingProjects.toArray());
			} catch (InterruptedException ex) {
				tree.setInput(Collections.EMPTY_MAP);
				getWizard().getImportJob().resetProposals();
			} catch (InvocationTargetException ex) {
				MessageDialog.openError(getShell(), DataTransferMessages.SmartImportJob_sorry,
						DataTransferMessages.SmartImportJob_internalError);
				IDEWorkbenchPlugin.log(ex.getMessage(), ex);
				getWizard().getImportJob().resetProposals();
			}
		} else {
			tree.setInput(Collections.emptyMap());
		}
		proposalsSelectionChanged();
	}

	private void proposalsSelectionChanged() {
		if (getWizard().getImportJob() != null) {
			Map<File, Collection<?>> input = (Map<File, Collection<?>>) tree.getInput();
			if (input.size() == 1 && input.values().iterator().next().isEmpty()) {
				getWizard().getImportJob().setDirectoriesToImport(null);
				getWizard().getImportJob().setExcludedDirectories(null);
				selectionSummary.setText(NLS.bind(DataTransferMessages.SmartImportProposals_selectionSummary,
						getWizard().getImportJob().getImportProposals(null).size(), tree.getCheckedElements().length));
			} else {
				Object[] selected = tree.getCheckedElements();
				Set<File> excludedDirectories = new HashSet(((Map<File, ?>) this.tree.getInput()).keySet());
				Set<File> selectedProjects = new HashSet<>();
				for (Object item : selected) {
					File directory = (File) item;
					excludedDirectories.remove(directory);
					selectedProjects.add(directory);
				}
				getWizard().getImportJob().setDirectoriesToImport(selectedProjects);
				getWizard().getImportJob().setExcludedDirectories(excludedDirectories);
				selectionSummary.setText(NLS.bind(DataTransferMessages.SmartImportProposals_selectionSummary,
						getWizard().getImportJob().getImportProposals(null).size(),
						getWizard().getImportJob().getDirectoriesToImport().size()));
			}
		}
		setPageComplete(isPageComplete());
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

