/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin Boyle <martingboyle@gmail.com> - Fix for 
 *     		Bug 183013 [Wizards] Error importing into linked EFS folder - "undefined path variable"
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.bidi.StructuredTextTypeHandlerFactory;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ide.dialogs.IElementFilter;
import org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * The abstract superclass for a typical import wizard's main page.
 * <p>
 * Clients may subclass this page to inherit its common destination resource
 * selection facilities.
 * </p>
 * <p>
 * Subclasses must implement 
 * <ul>
 *   <li><code>createSourceGroup</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may override
 * <ul>
 *   <li><code>allowNewContainerName</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 */
public abstract class WizardResourceImportPage extends WizardDataTransferPage {
    private IResource currentResourceSelection;

    // initial value stores
    private String initialContainerFieldValue;

    protected java.util.List selectedTypes = new ArrayList();

    // widgets
    /**
     * @since 3.10
     */
    protected Text containerNameField;

    /**
     * @since 3.10
     */
	protected Button referenceAsNewProjectRadio;
    /**
     * @since 3.10
     */
	protected Button copyAsNewProjectRadio;
	/**
	 * @since 3.10
	 */
	protected Button referenceExistingProjectRadio;
	/**
	 * @since 3.10
	 */
	protected Button resourcesRadio;
	/**
	 * @since 3.10
	 */
    protected Button containerBrowseButton;
    
    /**
	 * The <code>selectionGroup</code> field should have been created with a
	 * private modifier. Subclasses should not access this field directly.
	 * 
	 * @noreference This field is not intended to be referenced by clients.
	 */
    protected ResourceTreeAndListGroup selectionGroup;

    //messages
    private static final String EMPTY_FOLDER_MESSAGE = IDEWorkbenchMessages.WizardImportPage_specifyFolder;

    private static final String EMPTY_PROJECT_MESSAGE = IDEWorkbenchMessages.WizardImportPage_specifyProject;
    
    private static final String INACCESSABLE_FOLDER_MESSAGE = IDEWorkbenchMessages.WizardImportPage_folderMustExist;


    /**
     * Creates an import wizard page. If the initial resource selection 
     * contains exactly one container resource then it will be used as the default
     * import destination.
     *
     * @param name the name of the page
     * @param selection the current resource selection
     */
    protected WizardResourceImportPage(String name,
            IStructuredSelection selection) {
        super(name);
        
        //Initialize to null
        currentResourceSelection = null;
        if (selection.size() == 1) {
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IAdaptable) {
                Object resource = ((IAdaptable) firstElement)
                        .getAdapter(IResource.class);
                if (resource != null) {
					currentResourceSelection = (IResource) resource;
				}
            }
        }

        if (currentResourceSelection != null) {
            if (currentResourceSelection.getType() == IResource.FILE) {
				currentResourceSelection = currentResourceSelection.getParent();
			}

            if (!currentResourceSelection.isAccessible()) {
				currentResourceSelection = null;
			}
        }

    }

    /**
     * The <code>WizardResourceImportPage</code> implementation of this 
     * <code>WizardDataTransferPage</code> method returns <code>true</code>. 
     * Subclasses may override this method.
     */
    protected boolean allowNewContainerName() {
        return true;
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {

        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        composite.setFont(parent.getFont());

        createSourceGroup(composite);

        createDestinationGroup(composite);

        createOptionsGroup(composite);

        restoreWidgetValues();
        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null);	// should not initially have error message

        setControl(composite);
    }

    /**
     * Creates the import destination specification controls.
     *
     * @param parent the parent control
     */
    protected final void createDestinationGroup(Composite parent) {
        // container specification group
        Composite containerGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        containerGroup.setLayout(layout);
        containerGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        containerGroup.setFont(parent.getFont());

        this.referenceAsNewProjectRadio = new Button(containerGroup, SWT.RADIO);
        this.referenceAsNewProjectRadio.setText(IDEWorkbenchMessages.WizardImportPage_ImportAsReferenceNewProject);
        this.referenceAsNewProjectRadio.setFont(parent.getFont());
        this.referenceAsNewProjectRadio.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1));
        
        this.copyAsNewProjectRadio = new Button(containerGroup, SWT.RADIO);
        this.copyAsNewProjectRadio.setText(IDEWorkbenchMessages.WizardImportPage_ImportAsCopiedNewProject);
        this.copyAsNewProjectRadio.setFont(parent.getFont());
        this.copyAsNewProjectRadio.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1));
        
        this.referenceExistingProjectRadio = new Button(containerGroup, SWT.RADIO);
        this.referenceExistingProjectRadio.setText(IDEWorkbenchMessages.WizardImportPage_ImportExistingProject);
        this.referenceExistingProjectRadio.setFont(parent.getFont());
        this.referenceExistingProjectRadio.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1));
        
        resourcesRadio = new Button(containerGroup, SWT.RADIO);
        resourcesRadio.setText(IDEWorkbenchMessages.WizardImportPage_folder);
        resourcesRadio.setFont(parent.getFont());
        // container name entry field
        containerNameField = new Text(containerGroup, SWT.SINGLE | SWT.BORDER);
        BidiUtils.applyBidiProcessing(containerNameField, StructuredTextTypeHandlerFactory.FILE);

        containerNameField.addListener(SWT.Modify, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        containerNameField.setLayoutData(data);
        containerNameField.setFont(parent.getFont());

        // container browse button
        containerBrowseButton = new Button(containerGroup, SWT.PUSH);
        containerBrowseButton.setText(IDEWorkbenchMessages.WizardImportPage_browse2);
        containerBrowseButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL));
        containerBrowseButton.addListener(SWT.Selection, this);
        containerBrowseButton.setFont(parent.getFont());
        setButtonLayoutData(containerBrowseButton);
   
        referenceAsNewProjectRadio.setSelection(false);
        referenceExistingProjectRadio.setSelection(false);
        resourcesRadio.setSelection(true);
        
        referenceAsNewProjectRadio.addListener(SWT.Selection, this);
        referenceExistingProjectRadio.addListener(SWT.Selection, this);
        resourcesRadio.addListener(SWT.Selection, this);
        initialPopulateContainerField();
    }

    /**
     *	Create the import source selection widget
     */
    protected void createFileSelectionGroup(Composite parent) {
    	
        //Just create with a dummy root.
        this.selectionGroup = new ResourceTreeAndListGroup(parent,
                new FileSystemElement("Dummy", null, true),//$NON-NLS-1$
                getFolderProvider(), new WorkbenchLabelProvider(),
                getFileProvider(), new WorkbenchLabelProvider(), SWT.NONE,
                DialogUtil.inRegularFontMode(parent));

        ICheckStateListener listener = new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateWidgetEnablements();
            }
        };

        WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
        this.selectionGroup.setTreeComparator(comparator);
        this.selectionGroup.setListComparator(comparator);
        this.selectionGroup.addCheckStateListener(listener);
    }

    /**
     * Creates the import source specification controls.
     * <p>
     * Subclasses must implement this method.
     * </p>
     *
     * @param parent the parent control
     */
    protected abstract void createSourceGroup(Composite parent);

    /*
     * @see WizardDataTransferPage.getErrorDialogTitle()
     */
    protected String getErrorDialogTitle() {
        return IDEWorkbenchMessages.WizardImportPage_errorDialogTitle;
    }

    /**
     * Returns the path of the container resource specified in the container
     * name entry field, or <code>null</code> if no name has been typed in.
     * <p>
     * The container specified by the full path might not exist and would need to
     * be created.
     * </p>
     *
     * @return the full path of the container resource specified in
     *   the container name entry field, or <code>null</code>
     */
    protected IPath getContainerFullPath() {
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

        //make the path absolute to allow for optional leading slash
        IPath testPath = getResourcePath();

        if (testPath.equals(workspace.getRoot().getFullPath())) {
			return testPath;
		}

        IStatus result = workspace.validatePath(testPath.toString(),
                IResource.PROJECT | IResource.FOLDER | IResource.ROOT);
        if (result.isOK()) {
            return testPath;
        }

        return null;
    }

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns 
     * only files as children.
     */
    protected abstract ITreeContentProvider getFileProvider();

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns 
     * only folders as children.
     */
    protected abstract ITreeContentProvider getFolderProvider();

    /**
     * Return the path for the resource field.
     * @return IPath
     */
    protected IPath getResourcePath() {
        return getPathFromText(this.containerNameField);
    }

    /**
     * Returns this page's list of currently-specified resources to be 
     * imported. This is the primary resource selection facility accessor for 
     * subclasses.
     *
     * @return a list of resources currently selected 
     * for export (element type: <code>IResource</code>)
     */
    protected java.util.List getSelectedResources() {
        return this.selectionGroup.getAllCheckedListItems();
    }

    /**
     * Returns this page's list of currently-specified resources to be 
     * imported filtered by the IElementFilter.
     * @since 3.10
     */
    protected void getSelectedResources(IElementFilter filter, IProgressMonitor monitor) throws InterruptedException {
        this.selectionGroup.getAllCheckedListItems(filter, monitor);
    }

    /**
     * <bold>DO NOT USE THIS METHOD</bold>
     * Returns this page's list of currently-specified resources to be 
     * imported filtered by the IElementFilter.
     * @deprecated Should use the API {@link IElementFilter}
     */
    protected void getSelectedResources(org.eclipse.ui.internal.ide.dialogs.IElementFilter filter, IProgressMonitor monitor) throws InterruptedException {
        this.selectionGroup.getAllCheckedListItems(filter, monitor);
    }

    /**
     * Returns the container resource specified in the container name entry field,
     * or <code>null</code> if such a container does not exist in the workbench.
     *
     * @return the container resource specified in the container name entry field,
     *   or <code>null</code>
     */
    protected IContainer getSpecifiedContainer() {
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
        IPath path = getContainerFullPath();
        if (workspace.getRoot().exists(path)){
        	IResource resource = workspace.getRoot().findMember(path);
        	if(resource.getType() == IResource.FILE) {
				return null;
			}
        	return (IContainer) resource;
        	
        }
            

        return null;
    }

    /**
     * Returns a collection of the currently-specified resource types for
     * use by the type selection dialog.
     */
    protected java.util.List getTypesToImport() {

        return selectedTypes;
    }

    /**
     * Opens a container selection dialog and displays the user's subsequent
     * container resource selection in this page's container name field.
     */
    protected void handleContainerBrowseButtonPressed() {
        // see if the user wishes to modify this container selection
        IPath containerPath = queryForContainer(getSpecifiedContainer(),
                IDEWorkbenchMessages.WizardImportPage_selectFolderLabel,
                IDEWorkbenchMessages.WizardImportPage_selectFolderTitle);

        // if a container was selected then put its name in the container name field
        if (containerPath != null) { // null means user cancelled
            setErrorMessage(null);
            containerNameField.setText(containerPath.makeRelative().toString());
        }
    }

    /**
     * The <code>WizardResourceImportPage</code> implementation of this 
     * <code>Listener</code> method handles all events and enablements for controls
     * on this page. Subclasses may extend.
     * @param event Event
     */
    public void handleEvent(Event event) {
        Widget source = event.widget;

        if (source == containerBrowseButton) {
			handleContainerBrowseButtonPressed();
		}

        updateWidgetEnablements();
    }

    /**
     *	Open a registered type selection dialog and note the selections
     *	in the receivers types-to-export field
     */
    protected void handleTypesEditButtonPressed() {

        TypeFilteringDialog dialog = new TypeFilteringDialog(getContainer()
                .getShell(), getTypesToImport());

        dialog.open();

        Object[] newSelectedTypes = dialog.getResult();
        if (newSelectedTypes != null) { // ie.- did not press Cancel
            this.selectedTypes = new ArrayList(newSelectedTypes.length);
            for (int i = 0; i < newSelectedTypes.length; i++) {
				this.selectedTypes.add(newSelectedTypes[i]);
			}

            setupSelectionsBasedOnSelectedTypes();
        }

    }

    /**
     * Sets the initial contents of the container name field.
     */
    protected final void initialPopulateContainerField() {
        if (initialContainerFieldValue != null) {
			containerNameField.setText(initialContainerFieldValue);
		} else if (currentResourceSelection != null) {
			containerNameField.setText(currentResourceSelection.getFullPath()
                    .makeRelative().toString());
		}
    }

    /**
     * Set all of the selections in the selection group to value
     * @param value boolean
     */
    protected void setAllSelections(boolean value) {
        selectionGroup.setAllSelections(value);
    }

    /**
     * Sets the value of this page's container resource field, or stores
     * it for future use if this page's controls do not exist yet.
     *
     * @param value String
     */
    public void setContainerFieldValue(String value) {
        if (containerNameField == null) {
			initialContainerFieldValue = value;
		} else {
			containerNameField.setText(value);
		}
    }

    /**
     * Update the tree to only select those elements that match the selected types.
     * Do nothing by default.
     */
    protected void setupSelectionsBasedOnSelectedTypes() {
    }

    /**
     * Update the selections with those in map .
     * @param map Map - key tree elements, values Lists of list elements
     */
    protected void updateSelections(final Map map) {

        Runnable runnable = new Runnable() {
            public void run() {
                selectionGroup.updateSelections(map);
            }
        };

        BusyIndicator.showWhile(getShell().getDisplay(), runnable);
    }

    /**
     * Check if widgets are enabled or disabled by a change in the dialog.
     */
    protected void updateWidgetEnablements() {
    	this.containerNameField.setEnabled(this.resourcesRadio.getSelection());
    	this.containerBrowseButton.setEnabled(this.resourcesRadio.getSelection());
    	if (this.selectionGroup != null) {
    		this.selectionGroup.setEnabled(this.resourcesRadio.getSelection());
    	}
    	
        boolean pageComplete = determinePageCompletion();
        setPageComplete(pageComplete);
        if (pageComplete) {
			setMessage(null);
		}
        super.updateWidgetEnablements();
    }

    /* (non-Javadoc)
     * Method declared on WizardDataTransferPage.
     */
    protected final boolean validateDestinationGroup() {

    	if (!this.containerBrowseButton.getSelection()) {
    		return true;
    	}
    	
        IPath containerPath = getContainerFullPath();
        if (containerPath == null) {
            setMessage(EMPTY_FOLDER_MESSAGE);
            return false;
        }

        // If the container exist, validate it
        IContainer container = getSpecifiedContainer();
        if (container == null) {
        	//If it exists but is not valid then abort
        	if(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().exists(getContainerFullPath())) {
				return false;
			}
        	
            //if it is does not exist be sure the project does
            IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
            IPath projectPath = containerPath.removeLastSegments(containerPath
                    .segmentCount() - 1);

            if (workspace.getRoot().exists(projectPath)) {
				return true;
			}
            setErrorMessage(IDEWorkbenchMessages.WizardImportPage_projectNotExist);
            return false;
        } 
        if (!container.isAccessible()) {
             setErrorMessage(INACCESSABLE_FOLDER_MESSAGE);
             return false;
        }
        if (container.getLocationURI() == null) {
          if (container.isLinked()) {
               setErrorMessage(IDEWorkbenchMessages.WizardImportPage_undefinedPathVariable);
          } else {
               setErrorMessage(IDEWorkbenchMessages.WizardImportPage_containerNotExist);
          }
         return false;
        }
        

        if (sourceConflictsWithDestination(containerPath)) {
            setErrorMessage(getSourceConflictMessage());
            return false;
        }

        if (container instanceof IWorkspaceRoot){
        	setErrorMessage(EMPTY_PROJECT_MESSAGE);
        	return false;
        }
        return true;

    }

    /**
     * Returns the error message for when the source conflicts
     * with the destination.
     */
    protected final String getSourceConflictMessage() {
        return (IDEWorkbenchMessages.WizardImportPage_importOnReceiver);
    }

    /**
     * Returns whether or not the source location conflicts
     * with the destination resource. By default this is not
     * checked, so <code>false</code> is returned.
     * 
     * @param sourcePath the path being checked
     * @return <code>true</code> if the source location conflicts with the
     *   destination resource, <code>false</code> if not
     */
    protected boolean sourceConflictsWithDestination(IPath sourcePath) {
        return false;
    }

}
