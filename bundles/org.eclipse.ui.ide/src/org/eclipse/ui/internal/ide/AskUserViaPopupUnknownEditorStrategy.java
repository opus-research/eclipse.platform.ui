/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 485201
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.ide.IUnknownEditorStrategy;

/**
 * @since 3.12
 *
 */
public class AskUserViaPopupUnknownEditorStrategy implements IUnknownEditorStrategy {

	private boolean userCancelled = false;

	@Override
	public IEditorDescriptor getEditorDescriptor(String fileName, IEditorRegistry editorRegistry) {
		EditorSelectionDialog dialog = new EditorSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setFileName(fileName);
		dialog.setBlockOnOpen(true);
		int retVal = dialog.open();
		userCancelled = retVal == IDialogConstants.CANCEL_ID;
		return dialog.getSelectedEditor();
	}

	/**
	 * @return Returns true if user cancelled the operation.
	 */
	public boolean isUserCancelled() {
		return userCancelled;
	}

}
