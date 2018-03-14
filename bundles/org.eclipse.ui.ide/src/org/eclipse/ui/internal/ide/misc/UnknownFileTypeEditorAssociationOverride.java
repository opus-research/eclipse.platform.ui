/*******************************************************************************
 * Copyright (c) 2015 Zend Technologies Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kaloyan Raev - [142228] initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IEditorAssociationOverride;
import org.eclipse.ui.internal.ide.OpenUnknownFileTypesInTextEditorPreference;

/**
 * Overrides the default editor of text files, which have no editor association,
 * with the default text editor. This way it is avoided to open text files with
 * the system editor outside of the IDE.
 */
public class UnknownFileTypeEditorAssociationOverride implements IEditorAssociationOverride {

	/**
	 * The ID of the default text editor.
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$

	@Override
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		return editorDescriptors;
	}

	@Override
	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		return editorDescriptors;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		// Override with default text editor only if no association is found yet
		if (editorDescriptor == null && OpenUnknownFileTypesInTextEditorPreference.getValue()) {
			IURIEditorInput uriEditorInput = (IURIEditorInput) editorInput;
			try {
				InputStream is = uriEditorInput.getURI().toURL().openStream();
				if (TextFileDetector.isTextFile(is)) {
					return getTextEditorDescriptor();
				}
			} catch (IOException e) {
				// Problem reading the editor input - avoid overriding
			}
		}
		return editorDescriptor;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		// Override with default text editor only if no association is found yet
		if (editorDescriptor == null && OpenUnknownFileTypesInTextEditorPreference.getValue()) {
			try {
				if (TextFileDetector.isTextFile(new FileInputStream(fileName))) {
					return getTextEditorDescriptor();
				}
			} catch (IOException e) {
				// Problem reading the editor input - avoid overriding
			}
		}
		return editorDescriptor;
	}

	private IEditorDescriptor getTextEditorDescriptor() {
		return PlatformUI.getWorkbench().getEditorRegistry().findEditor(DEFAULT_TEXT_EDITOR_ID);
	}

}
