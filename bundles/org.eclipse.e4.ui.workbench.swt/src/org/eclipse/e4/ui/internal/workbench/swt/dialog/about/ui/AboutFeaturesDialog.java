/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *      font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt.dialog.about.ui;

import org.eclipse.e4.ui.internal.workbench.swt.dialog.about.IWorkbenchHelpContextIds;
import org.eclipse.e4.ui.workbench.swt.internal.copy.WorkbenchSWTMessages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * Displays information about the product plugins.
 * 
 * PRIVATE This class is internal to the workbench and must not be called
 * outside the workbench.
 */
public class AboutFeaturesDialog extends ProductInfoDialog {
	/**
	 * Constructor for AboutFeaturesDialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param productName
	 *            the product name
	 * @param bundleGroupInfos
	 *            the bundle info
	 */
	public AboutFeaturesDialog(Shell parentShell, String productName, AboutBundleGroupData[] bundleGroupInfos,
			AboutBundleGroupData initialSelection) {
		super(parentShell);
		AboutFeaturesPage page = new AboutFeaturesPage();
		page.setProductName(productName);
		page.setBundleGroupInfos(bundleGroupInfos);
		page.setInitialSelection(initialSelection);
		String title;
		if (productName != null)
			title = NLS.bind(WorkbenchSWTMessages.AboutFeaturesDialog_shellTitle, productName);
		else
			title = WorkbenchSWTMessages.AboutFeaturesDialog_SimpleTitle;
		initializeDialog(page, title, IWorkbenchHelpContextIds.ABOUT_FEATURES_DIALOG);
	}
}
