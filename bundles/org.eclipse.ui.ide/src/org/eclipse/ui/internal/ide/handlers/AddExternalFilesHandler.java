/*******************************************************************************
 * Copyright (c) 2014 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * A command handler for adding external files, the action is available from the
 * context menu. Opens a dialog prompting for file(s) and performs a copy or
 * link operation of the selected file(s) based on user's choice.
 * 
 * @since 3.4
 *
 */
public class AddExternalFilesHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// open file system dialog to select files
		final Shell shell = HandlerUtil.getActiveShell(event);
		if (shell == null) {
			throw new ExecutionException("no active shell"); //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		if (dialog.open() == null) {
			return null;
		}
		final String[] files = getSelectedFiles(dialog.getFileNames(),
				dialog.getFilterPath());
		if (files.length == 0) {
			throw new ExecutionException("list of selected files is empty"); //$NON-NLS-1$
		}
		// get the current selection
		final IContainer container = getSelectedContainer(event);
		if (container == null) {
			throw new ExecutionException(
					"no selected resource of type container"); //$NON-NLS-1$
		}
		// perform the copy of files
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				CopyFilesAndFoldersOperation op = new CopyFilesAndFoldersOperation(
						shell);
				op.copyOrLinkFiles(files, container, DND.DROP_NONE);
			}

		});
		return null;
	}

	/**
	 * Returns the current <code>IContainer</code> selection based on the
	 * specified execution event.
	 * 
	 * @param event
	 *            the execution event that contains the application context
	 * @return the <code>IContainer</code> selection, or null otherwise
	 */
	private IContainer getSelectedContainer(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if ((selection == null) || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Object element = structuredSelection.getFirstElement();
		if ((element == null) || !(element instanceof IAdaptable)) {
			return null;
		}
		IAdaptable adaptable = (IAdaptable) element;
		return (IContainer) adaptable.getAdapter(IContainer.class);
	}

	/**
	 * Return the locations of the passed file names based on the specified
	 * directory path.
	 * 
	 * @param names
	 *            the file names
	 * @param path
	 *            the directory path string
	 * @return the file locations as array of strings
	 */
	private String[] getSelectedFiles(String[] names, String path) {
		String[] files = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			files[i] = (new Path(path)).append(names[i]).toOSString();
		}
		return files;
	}

}
