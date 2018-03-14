/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Sebastian Davids - bug 128529
 * Semion Chichelnitsky (semion@il.ibm.com) - bug 278064
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal.copy;

import org.eclipse.osgi.util.NLS;

/**
 * Based on org.eclipse.ui.internal.WorkbenchMessages
 */
public class WorkbenchSWTMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.workbench.swt.internal.copy.messages";//$NON-NLS-1$

	public static String FilteredTree_AccessibleListenerClearButton;
	public static String FilteredTree_ClearToolTip;
	public static String FilteredTree_FilterMessage;
	public static String FilteredTree_AccessibleListenerFiltered;
	public static String ViewLabel_unknown;
	public static String ShowView_noDesc;
	public static String ShowView_selectViewHelp;
	public static String ICategory_other;
	public static String ICategory_general;
	public static String ShowView_shellTitle;

	// IDEApplication messages
	public static String IDEApplication_workspaceMandatoryTitle;
	public static String IDEApplication_workspaceMandatoryMessage;
	public static String IDEApplication_workspaceCannotLockTitle;
	public static String IDEApplication_workspaceCannotLockMessage;
	public static String IDEApplication_workspaceCannotBeSetTitle;
	public static String IDEApplication_workspaceCannotBeSetMessage;
	public static String InternalError;
	public static String IDEApplication_versionTitle;
	public static String IDEApplication_versionMessage;

	// About Dialog
	public static String AboutDialog_shellTitle;
	public static String AboutDialog_defaultProductName;

	public static String AboutDialog_DetailsButton;
	public static String ProductInfoDialog_errorTitle;
	public static String ProductInfoDialog_unableToOpenWebBrowser;
	public static String PreferencesExportDialog_ErrorDialogTitle;
	public static String AboutPluginsDialog_shellTitle;
	public static String AboutPluginsDialog_pluginName;
	public static String AboutPluginsDialog_pluginId;
	public static String AboutPluginsDialog_version;
	public static String AboutPluginsDialog_signed;
	public static String AboutPluginsDialog_provider;
	public static String AboutPluginsDialog_state_installed;
	public static String AboutPluginsDialog_state_resolved;
	public static String AboutPluginsDialog_state_starting;
	public static String AboutPluginsDialog_state_stopping;
	public static String AboutPluginsDialog_state_uninstalled;
	public static String AboutPluginsDialog_state_active;
	public static String AboutPluginsDialog_state_unknown;
	public static String AboutPluginsDialog_moreInfo;
	public static String AboutPluginsDialog_signingInfo_show;
	public static String AboutPluginsDialog_signingInfo_hide;
	public static String AboutPluginsDialog_columns;
	public static String AboutPluginsDialog_errorTitle;
	public static String AboutPluginsDialog_unableToOpenFile;
	public static String AboutPluginsDialog_filterTextMessage;
	public static String AboutFeaturesDialog_shellTitle;
	public static String AboutFeaturesDialog_featureName;
	public static String AboutFeaturesDialog_featureId;
	public static String AboutFeaturesDialog_version;
	public static String AboutFeaturesDialog_signed;
	public static String AboutFeaturesDialog_provider;
	public static String AboutFeaturesDialog_moreInfo;
	public static String AboutFeaturesDialog_pluginsInfo;
	public static String AboutFeaturesDialog_columns;
	public static String AboutFeaturesDialog_noInformation;
	public static String AboutFeaturesDialog_pluginInfoTitle;
	public static String AboutFeaturesDialog_pluginInfoMessage;
	public static String AboutFeaturesDialog_noInfoTitle;

	public static String AboutFeaturesDialog_SimpleTitle;
	public static String AboutSystemDialog_browseErrorLogName;
	public static String AboutSystemDialog_copyToClipboardName;
	public static String AboutSystemDialog_noLogTitle;
	public static String AboutSystemDialog_noLogMessage;

	public static String AboutSystemPage_FetchJobTitle;

	public static String AboutSystemPage_RetrievingSystemInfo;

	// Bundle Signing
	public static String BundleSigningTray_Signing_Certificate;
	public static String BundleSigningTray_Signing_Date;
	public static String BundleSigningTray_Working;
	public static String BundleSigningTray_Cant_Find_Service;
	public static String BundleSigningTray_Unget_Signing_Service;
	public static String BundleSigningTray_Unknown;
	public static String BundleSigningTray_Unsigned;
	public static String BundleSigningTray_Determine_Signer_For;

	// =================================================================
	// System Summary
	// =================================================================
	public static String SystemSummary_title;
	public static String SystemSummary_timeStamp;
	public static String SystemSummary_systemProperties;
	public static String SystemSummary_features;
	public static String SystemSummary_pluginRegistry;
	public static String SystemSummary_userPreferences;
	public static String SystemSummary_sectionTitle;
	public static String SystemSummary_sectionError;

	// paramter 0 is the feature name, parameter 1 is the version and parameter
	// 2 is the Id
	public static String SystemSummary_featureVersion;
	public static String SystemMenuMovePane_PaneName;

	public static String SystemSummary_descriptorIdVersionState;

	public static String InstallationDialog_ShellTitle;

	public static String AboutDialog_CanNotCreateExtension;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, WorkbenchSWTMessages.class);
	}
}
