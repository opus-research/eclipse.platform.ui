/*******************************************************************************
 * Copyright (c) 2014 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.util;

import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.MenuInsertionPoint;

/**
 * A "mock" INavigatorViewerDescriptor.
 */
public class TestNavigatorViewerDescriptor implements INavigatorViewerDescriptor {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getViewerId()
	 */
	public String getViewerId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getPopupMenuId()
	 */
	public String getPopupMenuId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#isVisibleContentExtension(java.lang.String)
	 */
	public boolean isVisibleContentExtension(String aContentExtensionId) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#isVisibleActionExtension(java.lang.String)
	 */
	public boolean isVisibleActionExtension(String anActionExtensionId) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#isRootExtension(java.lang.String)
	 */
	public boolean isRootExtension(String aContentExtensionId) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#hasOverriddenRootExtensions()
	 */
	public boolean hasOverriddenRootExtensions() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#allowsPlatformContributionsToContextMenu()
	 */
	public boolean allowsPlatformContributionsToContextMenu() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getCustomInsertionPoints()
	 */
	public MenuInsertionPoint[] getCustomInsertionPoints() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getStringConfigProperty(java.lang.String)
	 */
	public String getStringConfigProperty(String aPropertyName) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getBooleanConfigProperty(java.lang.String)
	 */
	public boolean getBooleanConfigProperty(String aPropertyName) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorViewerDescriptor#getHelpContext()
	 */
	public String getHelpContext() {
		return null;
	}

}
