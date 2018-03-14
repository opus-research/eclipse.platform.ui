/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Mickael Istria (Red Hat Inc.) - 226046 Allow user to specify filters
 *******************************************************************************/
/*
 * Created on Feb 9, 2004
 *  
 */
package org.eclipse.ui.internal.navigator.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;

/**
 * 
 * @since 3.2
 * 
 */
public class CommonFilterSelectionDialog extends TrayDialog {
   
	private static final String FILTER_ICON = "icons/full/elcl16/filter_ps.gif"; //$NON-NLS-1$
	private static final String CONTENT_ICON = "icons/full/elcl16/content.gif"; //$NON-NLS-1$

	private static final int TAB_WIDTH_IN_DLUS = 300;

	private static final int TAB_HEIGHT_IN_DLUS = 150;

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	private TabFolder customizationsTabFolder;

	private CommonFiltersTab commonFiltersTab;

	private ContentExtensionsTab contentExtensionsTab;

	private Label descriptionText;

	private ISelectionChangedListener updateDescriptionSelectionListener; 

	private String helpContext;
	private UserFiltersTab userFiltersTab;
	
	/**
	 * Public only for tests.
	 * 
	 * @param aCommonViewer
	 */
	public CommonFilterSelectionDialog(CommonViewer aCommonViewer) {
		super(aCommonViewer.getControl().getShell());
		setShellStyle(SWT.RESIZE | getShellStyle());

		commonViewer = aCommonViewer;
		contentService = commonViewer.getNavigatorContentService();

		INavigatorViewerDescriptor viewerDescriptor = contentService.getViewerDescriptor();
		helpContext = viewerDescriptor
				.getStringConfigProperty(INavigatorViewerDescriptor.PROP_CUSTOMIZE_VIEW_DIALOG_HELP_CONTEXT);
		
//		for (ICommonFilterDescriptor filterDesc : this.commonViewer.getNavigatorContentService().getFilterService().getVisibleFilterDescriptors()) {
//			ViewerFilter filter = this.commonViewer.getNavigatorContentService().getFilterService().getViewerFilter(filterDesc);
//			if (filter instanceof UserFilterViewFilter) {
//				((UserF))
//			}
//		}

		if (helpContext != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					aCommonViewer.getControl().getShell(), helpContext);
		}
	}

	public boolean isHelpAvailable() {
		return helpContext != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		 
		getShell()
				.setText(
						CommonNavigatorMessages.CommonFilterSelectionDialog_Available_customization_);
		 
		
		Composite superComposite = (Composite) super.createDialogArea(parent);
		 
		createCustomizationsTabFolder(superComposite); 
		
		commonFiltersTab = new CommonFiltersTab(customizationsTabFolder,
				contentService);
		createTabItem(
				customizationsTabFolder,
				CommonNavigatorMessages.CommonFilterSelectionDialog_Available_Filters,
				commonFiltersTab, FILTER_ICON);
		
		this.userFiltersTab = new UserFiltersTab(customizationsTabFolder, this.commonViewer);
		createTabItem(
				customizationsTabFolder,
				CommonNavigatorMessages.CommonFilterSelectionDialog_User_Resource_Filters,
				userFiltersTab, FILTER_ICON);
		

		boolean hideExtensionsTab = contentService.getViewerDescriptor()
				.getBooleanConfigProperty(
						INavigatorViewerDescriptor.PROP_HIDE_AVAILABLE_EXT_TAB);

		if (!hideExtensionsTab) { 
			contentExtensionsTab = new ContentExtensionsTab(
					customizationsTabFolder, contentService);

			createTabItem(
					customizationsTabFolder,
					CommonNavigatorMessages.CommonFilterSelectionDialog_Available_Content,
					contentExtensionsTab, CONTENT_ICON);
			
		}

		createDescriptionText(superComposite);

		if (commonFiltersTab != null) {
			commonFiltersTab.addSelectionChangedListener(getSelectionListener());
		}

		if (contentExtensionsTab != null) {
			contentExtensionsTab
					.addSelectionChangedListener(getSelectionListener());
		}
	
		commonFiltersTab.setInitialFocus();
		
		return customizationsTabFolder;
	}

	private void createCustomizationsTabFolder(Composite superComposite) {
		customizationsTabFolder = new TabFolder (superComposite, SWT.RESIZE);
 
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertHorizontalDLUsToPixels(TAB_WIDTH_IN_DLUS);
		gd.heightHint = convertVerticalDLUsToPixels(TAB_HEIGHT_IN_DLUS);
		
		customizationsTabFolder.setLayout(new GridLayout());
		customizationsTabFolder.setLayoutData(gd);

		customizationsTabFolder.setFont(superComposite.getFont()); 

		customizationsTabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (descriptionText != null) {
					descriptionText.setText(""); //$NON-NLS-1$
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});
	  
	}

	private TabItem createTabItem(TabFolder aTabFolder, String label,
			Composite composite, String imageKey) {
		TabItem extensionsTabItem = new TabItem(aTabFolder, SWT.BORDER);
		extensionsTabItem.setText(label);
 		extensionsTabItem.setControl(composite); 
 		extensionsTabItem.setImage(NavigatorPlugin.getDefault().getImage(imageKey));
 		return extensionsTabItem;
	}

	private void createDescriptionText(Composite composite) {

		descriptionText = new Label(composite, SWT.WRAP);
		descriptionText.setFont(composite.getFont());
		descriptionText.setBackground(composite.getBackground());
		GridData descriptionTextGridData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		descriptionTextGridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(descriptionTextGridData);
	}

	private ISelectionChangedListener getSelectionListener() {
		if (updateDescriptionSelectionListener == null) {
			updateDescriptionSelectionListener = new FilterDialogSelectionListener(
					descriptionText);
		}
		return updateDescriptionSelectionListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		if (contentExtensionsTab != null) {
			List checkedExtensions = new ArrayList();
			TableItem[] tableItems = contentExtensionsTab.getTable().getItems();
			INavigatorContentDescriptor descriptor;
			for (int i = 0; i < tableItems.length; i++) {
				descriptor = (INavigatorContentDescriptor) tableItems[i]
						.getData();

				if (tableItems[i].getChecked()) {
					checkedExtensions.add(descriptor.getId());
				}
			}
			String[] contentExtensionIdsToActivate = (String[]) checkedExtensions
					.toArray(new String[checkedExtensions.size()]);
			UpdateActiveExtensionsOperation updateExtensions = new UpdateActiveExtensionsOperation(
					commonViewer, contentExtensionIdsToActivate);
			updateExtensions.execute(null, null);
		}

		if (this.userFiltersTab != null) {
			this.commonViewer.setData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA, this.userFiltersTab.getUserFilters());
		}
		
		if (commonFiltersTab != null) {
			Set checkedFilters = commonFiltersTab.getCheckedItems();
			
			String[] filterIdsToActivate = new String[checkedFilters.size()];
			int indx = 0;
			for (Iterator iterator = checkedFilters.iterator(); iterator
					.hasNext();) {
				ICommonFilterDescriptor descriptor = (ICommonFilterDescriptor) iterator
						.next();

				filterIdsToActivate[indx++] = descriptor.getId();

			} 
			UpdateActiveFiltersOperation updateFilters = new UpdateActiveFiltersOperation(
					commonViewer, filterIdsToActivate);
			updateFilters.execute(null, null);
		}
		
		super.okPressed();
	}
}
