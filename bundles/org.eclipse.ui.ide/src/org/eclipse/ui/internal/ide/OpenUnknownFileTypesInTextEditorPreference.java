/*******************************************************************************
 * Copyright (c) 2015 Zend Technologies Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kaloyan Raev <kaloyan.r@zend.com> - Bug 142228
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.ide.IDE;

/**
 * Utility class for storing and retrieving the value of the
 * OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR preference.
 *
 * @see org.eclipse.ui.ide.IDE.Preferences#OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR
 */
public class OpenUnknownFileTypesInTextEditorPreference {

	/**
	 * Returns the current value of the OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR
	 * preference.
	 *
	 * @return the current value
	 *
	 * @see org.eclipse.ui.ide.IDE.Preferences#OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR
	 */
	public static boolean getValue() {
		return getPreferenceStore().getBoolean(IDE.Preferences.OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR);
	}

	/**
	 * Sets the current value of the OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR
	 * preference.
	 *
	 * @param value
	 *            the new current value
	 *
	 * @see org.eclipse.ui.ide.IDE.Preferences#OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR
	 */
	public static void setValue(boolean value) {
		getPreferenceStore().setValue(IDE.Preferences.OPEN_UNKNOWN_TEXT_FILE_IN_TEXT_EDITOR, value);
	}

	private static IPreferenceStore getPreferenceStore() {
		return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
	}

}
