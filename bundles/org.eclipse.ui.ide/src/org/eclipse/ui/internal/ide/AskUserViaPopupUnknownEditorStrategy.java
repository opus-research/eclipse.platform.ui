/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others
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

import org.eclipse.core.runtime.IStatus;
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

	private int status = IStatus.ERROR;

	@Override
	public IEditorDescriptor getEditorDescriptor(String fileName, IEditorRegistry editorRegistry) {
		IEditorDescriptor editorDesc = null;
		int retVal;

		EditorSelectionDialog dialog = new EditorSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setFileName(fileName);
		dialog.setBlockOnOpen(true);

		retVal = dialog.open();
		editorDesc = dialog.getSelectedEditor();

		// compute status
		if (retVal == IDialogConstants.OK_ID && editorDesc != null) {
			status = IStatus.OK;
		} else if (IDialogConstants.CANCEL_ID == retVal) {
			status = IStatus.CANCEL;
		} else if (editorDesc == null)
			status = IStatus.ERROR;

		return editorDesc;
	}

	/**
	 * @return Returns the status.
	 */
	@Override
	public int getStatus() {
		return status;
	}
}
