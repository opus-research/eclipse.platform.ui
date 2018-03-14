/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 446652
 *******************************************************************************/
package org.eclipse.e4.ui.dialogs.textbundles;

import org.eclipse.osgi.util.NLS;

public class DialogMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.dialogs.textbundles.messages"; //$NON-NLS-1$

	public static String SelectionDialog_selectLabel;
	public static String SelectionDialog_deselectLabel;

	public static String ListSelection_title;
	public static String ListSelection_message;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, DialogMessages.class);
	}
}