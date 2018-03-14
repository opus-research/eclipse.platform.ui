/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 *   	should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * Abstract baseclass for various working set dialogs.
 * 
 * @since 3.2
 */
public abstract class AbstractWorkingSetDialog extends SelectionDialog
		implements IWorkingSetSelectionDialog {

	private static final int ID_NEW = IDialogConstants.CLIENT_ID + 1;
	private static final int ID_DETAILS = ID_NEW + 1;
	private static final int ID_REMOVE = ID_DETAILS + 1;
	private static final int ID_SELECTALL = ID_REMOVE + 1;
	private static final int ID_DESELECTALL = ID_SELECTALL + 1;
	
	private Button newButton;

	private Button detailsButton;

	private Button removeButton;
	
	private Button selectAllButton;
	
	private Button deselectAllButton;

	private IWorkingSet[] result;

	private List<IWorkingSet> addedWorkingSets;

	private List<IWorkingSet> removedWorkingSets;

	private Map editedWorkingSets;

	private List removedMRUWorkingSets;

	private Set<String> workingSetIds;
	
	private boolean canEdit;

	private Button useDefaultWorkingSetCheckbox;
	private ComboViewer defaultWorkingSetSelector;
	private ControlDecoration missingDefaultWorkingSetDecoration;
	private boolean useDefaultWorkingSet;
	private IWorkingSet defaultWorkingSet;

	protected AbstractWorkingSetDialog(Shell parentShell, String[] workingSetIds, boolean canEdit) {
		super(parentShell);
		if (workingSetIds != null) {
			this.workingSetIds = new HashSet<String>();
			for (int i = 0; i < workingSetIds.length; i++) {
				this.workingSetIds.add(workingSetIds[i]);
			}
		}
		this.canEdit = canEdit;
	}

	/**
	 * Return the set of supported working set types.
	 * 
	 * @return the supported working set types
	 */
	protected Set getSupportedWorkingSetIds() {
		return workingSetIds;
	}

	/**
	 * Adds the modify buttons to the dialog.
	 * 
	 * @param composite
	 *            Composite to add the buttons to
	 */
	protected void addModifyButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		buttonComposite.setLayoutData(data);

		newButton = createButton(buttonComposite, ID_NEW,
				WorkbenchMessages.WorkingSetSelectionDialog_newButton_label,
				false);
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet();
			}
		});

		if (canEdit) {
			detailsButton = createButton(
					buttonComposite,
					ID_DETAILS,
					WorkbenchMessages.WorkingSetSelectionDialog_detailsButton_label,
					false);
			detailsButton.setEnabled(false);
			detailsButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					editSelectedWorkingSet();
				}
			});

			removeButton = createButton(
					buttonComposite,
					ID_REMOVE,
					WorkbenchMessages.WorkingSetSelectionDialog_removeButton_label,
					false);
			removeButton.setEnabled(false);
			removeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeSelectedWorkingSets();
				}
			});
		}
		
		layout.numColumns = 1; // must manually reset the number of columns because createButton increments it - we want these buttons to be laid out vertically.
	}

	/**
	 * Add the select/deselect buttons.
	 * 
	 * @param composite Composite to add the buttons to
	 */
	protected void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		buttonComposite.setLayoutData(data);
		
		selectAllButton = createButton(
				buttonComposite,
				ID_SELECTALL,
				WorkbenchMessages.SelectionDialog_selectLabel,
				false);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllSets();
			}
		});
		
		deselectAllButton = createButton(
				buttonComposite,
				ID_DESELECTALL,
				WorkbenchMessages.SelectionDialog_deselectLabel,
				false);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectAllSets();
			}
		});
	}
	
	protected void addDefaultWorkingSetConfiguration(Composite composite) {
		Composite line = new Composite(composite, SWT.NONE);
		line.setLayout(new GridLayout(2, false));
		this.useDefaultWorkingSetCheckbox = new Button(line, SWT.CHECK);
		this.useDefaultWorkingSetCheckbox
				.setText(WorkbenchMessages.WorkingSetSelectionDialog_defaultWorkingSet);
		this.defaultWorkingSetSelector = new ComboViewer(line);
		this.defaultWorkingSetSelector.setLabelProvider(new WorkingSetLabelProvider());
		this.defaultWorkingSetSelector.setContentProvider(new ArrayContentProvider());
		this.defaultWorkingSetSelector.addFilter(new WorkingSetFilter(null));
		IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager()
				.getWorkingSets();
		defaultWorkingSetSelector.setInput(workingSets);
		// workingSetSelector.setFilters(new ViewerFilter[] { new Filter() });
		if (this.defaultWorkingSet != null) {
			this.defaultWorkingSetSelector.setSelection(new StructuredSelection(
					this.defaultWorkingSet), true);
		}
		this.useDefaultWorkingSetCheckbox.setSelection(this.useDefaultWorkingSet);
		this.defaultWorkingSetSelector.getControl().setEnabled(this.useDefaultWorkingSet);

		this.missingDefaultWorkingSetDecoration = new ControlDecoration(
				this.defaultWorkingSetSelector.getControl(), SWT.TOP | SWT.LEFT);
		this.missingDefaultWorkingSetDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());

		this.useDefaultWorkingSetCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractWorkingSetDialog.this.useDefaultWorkingSet = AbstractWorkingSetDialog.this.useDefaultWorkingSetCheckbox
						.getSelection();
				updateButtonAvailability();
			}
		});
		this.defaultWorkingSetSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = AbstractWorkingSetDialog.this.defaultWorkingSetSelector
						.getSelection();
				if (selection.isEmpty()) {
					AbstractWorkingSetDialog.this.defaultWorkingSet = null;
				} else {
					AbstractWorkingSetDialog.this.defaultWorkingSet = (IWorkingSet) ((IStructuredSelection) selection)
							.getFirstElement();
				}
				updateButtonAvailability();
			}
		});
	}

	/**
	 * Select all working sets.
	 */
	protected abstract void selectAllSets();
	
	/**
	 * Deselect all working sets.
	 */
	protected abstract void deselectAllSets();

	/**
	 * Opens a working set wizard for editing the currently selected working
	 * set.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 */
	void editSelectedWorkingSet() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		IWorkingSet editWorkingSet = (IWorkingSet) getSelectedWorkingSets()
				.get(0);
		IWorkingSetEditWizard wizard = manager
				.createWorkingSetEditWizard(editWorkingSet);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		IWorkingSet originalWorkingSet = (IWorkingSet) editedWorkingSets
				.get(editWorkingSet);
		boolean firstEdit = originalWorkingSet == null;

		// save the original working set values for restoration when selection
		// dialog is cancelled.
		if (firstEdit) {
			originalWorkingSet = new WorkingSet(editWorkingSet.getName(),
					editWorkingSet.getLabel(), editWorkingSet.getElements());
		} else {
			editedWorkingSets.remove(editWorkingSet);
		}
		dialog.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_EDIT_WIZARD);
		if (dialog.open() == Window.OK) {
			editWorkingSet = wizard.getSelection();
			availableWorkingSetsChanged();
			// make sure ok button is enabled when the selected working set
			// is edited. Fixes bug 33386.
			updateButtonAvailability();
		}
		editedWorkingSets.put(editWorkingSet, originalWorkingSet);
	}

	/**
	 * Opens a working set wizard for creating a new working set.
	 */
	void createWorkingSet() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		String ids[] = null;
		if (workingSetIds != null) {
			ids = workingSetIds.toArray(new String[workingSetIds.size()]);
		}
		IWorkingSetNewWizard wizard = manager.createWorkingSetNewWizard(ids);
		// the wizard can never be null since we have at least a resource
		// working set
		// creation page
		WizardDialog dialog = new WizardDialog(getShell(), wizard);

		dialog.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);
		if (dialog.open() == Window.OK) {
			IWorkingSet workingSet = wizard.getSelection();
			manager.addWorkingSet(workingSet);
			addedWorkingSets.add(workingSet);
			availableWorkingSetsChanged();
		}
	}

	protected abstract List getSelectedWorkingSets();

	/**
	 * Notifies the dialog that there has been a change to the sets available
	 * for use. In other words, the user has either added, deleted or renamed a
	 * set.
     * <p>
     * Subclasses should override, but should call <code>super.availableWorkingSetsChanged</code>
     * to update the selection button enablements.
     * </p>
	 */
	protected void availableWorkingSetsChanged() {
		List<IWorkingSet> allWorkingSets = Arrays.asList(PlatformUI.getWorkbench()
				.getWorkingSetManager().getWorkingSets());
		if (!(selectAllButton == null || selectAllButton.isDisposed())){
			selectAllButton.setEnabled(!allWorkingSets.isEmpty());
		}
		if (!(deselectAllButton == null || deselectAllButton.isDisposed())){
			deselectAllButton.setEnabled(!allWorkingSets.isEmpty());
		}

		if (this.useDefaultWorkingSetCheckbox != null
				&& !this.useDefaultWorkingSetCheckbox.isDisposed()) {
			if (allWorkingSets.isEmpty()) {
				this.useDefaultWorkingSetCheckbox.setSelection(false);
			}
			this.useDefaultWorkingSetCheckbox.setEnabled(!allWorkingSets.isEmpty());
		}
		if (this.defaultWorkingSetSelector != null
				&& !this.defaultWorkingSetSelector.getControl().isDisposed()) {
			this.defaultWorkingSetSelector.setInput(allWorkingSets);
			// setInput tends to remove current selection
			if (allWorkingSets.contains(this.defaultWorkingSet)) {
				this.defaultWorkingSetSelector.setSelection(new StructuredSelection(
						this.defaultWorkingSet));
			}
			this.defaultWorkingSetSelector.getControl().setEnabled(!allWorkingSets.isEmpty());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#getSelection()
	 */
	@Override
	public IWorkingSet[] getSelection() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(org.eclipse.ui.IWorkingSet[])
	 */
	@Override
	public void setSelection(IWorkingSet[] selection) {
		result = selection;
	}

	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#open()
	 */
	@Override
	public int open() {
		addedWorkingSets = new ArrayList();
		removedWorkingSets = new ArrayList();
		editedWorkingSets = new HashMap();
		removedMRUWorkingSets = new ArrayList();
		return super.open();
	}

	/**
	 * Return the list of working sets that were added during the life of this
	 * dialog.
	 * 
	 * @return the working sets
	 */
	protected final List getAddedWorkingSets() {
		return addedWorkingSets;
	}

	/**
	 * Return the map of working sets that were edited during the life of this
	 * dialog.
	 * 
	 * @return the working sets
	 */
	protected final Map getEditedWorkingSets() {
		return editedWorkingSets;
	}

	/**
	 * Return the list of working sets that were removed from the MRU list
	 * during the life of this dialog.
	 * 
	 * @return the working sets
	 */
	protected final List getRemovedMRUWorkingSets() {
		return removedMRUWorkingSets;
	}

	/**
	 * Return the list of working sets that were removed during the life of this
	 * dialog.
	 * 
	 * @return the working sets
	 */
	protected final List getRemovedWorkingSets() {
		return removedWorkingSets;
	}

	/**
	 * Updates the modify buttons' enabled state based on the current seleciton.
	 */
	protected void updateButtonAvailability() {

		List<IWorkingSet> selection = getSelectedWorkingSets();
		boolean hasSelection = selection != null && !selection.isEmpty();
		boolean hasSingleSelection = hasSelection;
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
				.getWorkingSetRegistry();

		newButton.setEnabled(registry.hasNewPageWorkingSetDescriptor());

		if (canEdit)
			removeButton.setEnabled(hasSelection);

		IWorkingSet selectedWorkingSet = null;
		if (hasSelection) {
			hasSingleSelection = selection.size() == 1;
			if (hasSingleSelection) {
				selectedWorkingSet = selection.get(0);
			}
		}
		if (canEdit)
			detailsButton.setEnabled(hasSingleSelection
				&& selectedWorkingSet.isEditable());

		boolean missingDefaultWorkingSet = false;
		if (this.defaultWorkingSetSelector != null
				&& !this.defaultWorkingSetSelector.getControl().isDisposed()) {
			defaultWorkingSetSelector.getControl().setEnabled(
					useDefaultWorkingSetCheckbox.getSelection());
			missingDefaultWorkingSet = this.useDefaultWorkingSetCheckbox != null
				&& !this.useDefaultWorkingSetCheckbox.isDisposed()
				&& this.useDefaultWorkingSetCheckbox.getSelection()
				&& this.defaultWorkingSetSelector.getSelection().isEmpty();
			if (missingDefaultWorkingSet) {
				this.missingDefaultWorkingSetDecoration.show();
			} else {
				this.missingDefaultWorkingSetDecoration.hide();
			}
		}
		
		if (getOkButton() != null && !getOkButton().isDisposed()) {
			getOkButton().setEnabled(!missingDefaultWorkingSet);
		}
	}

	/**
	 * Removes the selected working sets from the workbench.
	 */
	protected void removeSelectedWorkingSets() {
		List selection = getSelectedWorkingSets();
		removeSelectedWorkingSets(selection);
	}

	/**
	 * Remove the working sets contained in the provided selection from the
	 * working set manager.
	 * 
	 * @param selection
	 *            the sets
	 */
	protected void removeSelectedWorkingSets(List selection) {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iter.next();
			if (getAddedWorkingSets().contains(workingSet)) {
				getAddedWorkingSets().remove(workingSet);
			} else {
				IWorkingSet[] recentWorkingSets = manager
						.getRecentWorkingSets();
				for (int i = 0; i < recentWorkingSets.length; i++) {
					if (workingSet.equals(recentWorkingSets[i])) {
						getRemovedMRUWorkingSets().add(workingSet);
						break;
					}
				}
				getRemovedWorkingSets().add(workingSet);
			}
			manager.removeWorkingSet(workingSet);
		}
		availableWorkingSetsChanged();
	}

	@Override
	protected void okPressed() {
		if (this.useDefaultWorkingSetCheckbox.getSelection()) {
			setDefaultWorkingSet((IWorkingSet) ((IStructuredSelection) this.defaultWorkingSetSelector
				.getSelection()).getFirstElement());
		}
		super.okPressed();
	}

	@Override
	public IWorkingSet getDefaultWorkingSet() {
		if (this.useDefaultWorkingSet) {
			return this.defaultWorkingSet;
		}
		return null;
	}

	@Override
	public void setDefaultWorkingSet(IWorkingSet defaultWorkingSet) {
		this.defaultWorkingSet = defaultWorkingSet;
		this.useDefaultWorkingSet = defaultWorkingSet != null;
	}
}
