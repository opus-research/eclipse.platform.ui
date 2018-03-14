/*******************************************************************************
 * Copyright (c) 2015 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.util;

import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * A "mock" NavigatorActivationService.
 */
public class TestNavigatorActivationService implements INavigatorActivationService {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorActivationService#activateExtensions(java.lang.String[], boolean)
	 */
	public INavigatorContentDescriptor[] activateExtensions(String[] extensionIds,
			boolean toDeactivateAllOthers) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorActivationService#deactivateExtensions(java.lang.String[], boolean)
	 */
	public INavigatorContentDescriptor[] deactivateExtensions(String[] extensionIds,
			boolean toActivateAllOthers) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorActivationService#isNavigatorExtensionActive(java.lang.String)
	 */
	public boolean isNavigatorExtensionActive(String aNavigatorExtensionId) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorActivationService#persistExtensionActivations()
	 */
	public void persistExtensionActivations() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorActivationService#addExtensionActivationListener(org.eclipse.ui.navigator.IExtensionActivationListener)
	 */
	public void addExtensionActivationListener(IExtensionActivationListener aListener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.INavigatorActivationService#removeExtensionActivationListener(org.eclipse.ui.navigator.IExtensionActivationListener)
	 */
	public void removeExtensionActivationListener(IExtensionActivationListener aListener) {
	}

}
