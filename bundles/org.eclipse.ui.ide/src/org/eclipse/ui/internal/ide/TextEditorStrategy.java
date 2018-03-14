/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - extracted from IDE.getEditorDescription
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 485201
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ide.IUnknownEditorStrategy;

/**
 * @since 3.12
 *
 */
public final class TextEditorStrategy implements IUnknownEditorStrategy {

	private int status = IStatus.ERROR;

	@Override
	public IEditorDescriptor getEditorDescriptor(String name, IEditorRegistry editorReg) {
		IEditorDescriptor editorDesc = null;
		editorDesc = editorReg.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);

		status = (editorDesc != null) ? IStatus.OK : IStatus.ERROR;

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