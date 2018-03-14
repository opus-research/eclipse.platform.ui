/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 305336 - Ability to open a file from command line 
 *                      at a specific line/col
 ******************************************************************************/

package org.eclipse.ui.internal.ide.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Helper class used to process delayed events.
 * Events currently supported:
 * <ul>
 * <li>SWT.OpenDocument</li>
 * </ul>
 * @since 3.3
 */
public class DelayedEventsProcessor implements Listener {

	private ArrayList<String> filesToOpen = new ArrayList<String>(1);

	/**
	 * Constructor.
	 * @param display display used as a source of event
	 */
	public DelayedEventsProcessor(Display display) {
		display.addListener(SWT.OpenDocument, this);
	}

	@Override
	public void handleEvent(Event event) {
		final String path = event.text;
		if (path == null)
			return;
		// If we start supporting events that can arrive on a non-UI thread, the following
		// line will need to be in a "synchronized" block:
		filesToOpen.add(path);
	}
	
	/**
	 * Process delayed events.
	 * @param display display associated with the workbench 
	 */
	public void catchUp(Display display) {
		if (filesToOpen.isEmpty())
			return;
		
		// If we start supporting events that can arrive on a non-UI thread, the following
		// lines will need to be in a "synchronized" block:
		String[] filePaths = new String[filesToOpen.size()];
		filesToOpen.toArray(filePaths);
		filesToOpen.clear();

		for(int i = 0; i < filePaths.length; i++) {
			openFile(display, filePaths[i]);
		}
	}

	/**
	 * Opens a file from a path in the filesystem (asynchronously).
	 * 
	 * @param display
	 *            the display to run the asynchronous operation.
	 * @param initialPath
	 *            the path to be used. It can optionally be a path with
	 *            +line:col to open it at the given line/col.
	 * 
	 *            I.e.: my/file.py+10:3 will open file.py at line 10, column 3.
	 *            Note that the line and column are 1-based.
	 */
	public static void openFile(Display display, final String initialPath) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				String path = initialPath;
				// First check if it's an existing file... if it is, the
				// line/col are not specified.
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
				IFileInfo fetchInfo = fileStore.fetchInfo();
				int line = -1;
				int col = -1;
				if (fetchInfo.isDirectory() || !fetchInfo.exists()) {
					// Ok, we didn't have a match, so, let's go on and check if
					// we have a +line:col at the end -- and if we do, strip it
					// and get the line/col accordingly (note that the col is
					// optional).
					int plusIndex = path.lastIndexOf('+');
					if (plusIndex >= 0) {
						String lineCol = path.substring(plusIndex + 1);
						path = path.substring(0, plusIndex);

						int commaIndex = lineCol.indexOf(':');
						if (commaIndex != -1) {
							String lineStr = lineCol.substring(0, commaIndex);
							String colStr = lineCol.substring(commaIndex + 1, lineCol.length());
							try {
								line = Integer.parseInt(lineStr);
							} catch (NumberFormatException e1) {
							}
							try {
								col = Integer.parseInt(colStr);
							} catch (NumberFormatException e) {
							}
						} else {
							try {
								line = Integer.parseInt(lineCol);
							} catch (NumberFormatException e) {
							}
						}

						// Update the file store info to the new path
						fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
						fetchInfo = fileStore.fetchInfo();
					}
				}

				if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
					IWorkbenchPage page = window.getActivePage();
					if (page == null) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_noWindow, path);
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title,
								msg, SWT.SHEET);
					}
					try {
						IEditorPart openEditor = IDE.openInternalEditorOnFileStore(page, fileStore);
						Shell shell = window.getShell();
						if (shell != null) {
							if (shell.getMinimized())
								shell.setMinimized(false);
							shell.forceActive();
						}

						if (line >= 1) {
							try {
								// Do things with reflection to avoid having to
								// rely on the text editor plugins.
								Object documentProvider = invoke(openEditor, "getDocumentProvider"); //$NON-NLS-1$

								Object editorInput = invoke(openEditor, "getEditorInput"); //$NON-NLS-1$

								Object document = invoke(documentProvider, "getDocument", new Class[] { Object.class }, //$NON-NLS-1$
										new Object[] { editorInput });

								int numberOfLines = (Integer) invoke(document, "getNumberOfLines"); //$NON-NLS-1$
								if (line > numberOfLines) {
									line = numberOfLines;
								}
								int lineLength = (Integer) invoke(document, "getLineLength", new Class[] { int.class }, //$NON-NLS-1$
										new Object[] { line - 1 });
								if (col > lineLength) {
									col = lineLength;
								}
								if (col < 1) {
									col = 1;
								}
								int offset = (Integer) invoke(document, "getLineOffset", new Class[] { int.class }, //$NON-NLS-1$
										new Object[] { (line - 1) });
								offset += (col - 1);

								invoke(openEditor, "selectAndReveal", new Class[] { int.class, int.class }, //$NON-NLS-1$
										new Object[] { offset, 0 });
							} catch (Exception e) {
								// Ignore (not an ITextEditor).
							}
						}
					} catch (PartInitException e) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_errorOnOpen,
										fileStore.getName());
						CoreException eLog = new PartInitException(e.getMessage());
						IDEWorkbenchPlugin.log(msg, new Status(IStatus.ERROR, IDEApplication.PLUGIN_ID, msg, eLog));
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title,
								msg, SWT.SHEET);
					}
				} else {
					String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_fileNotFound, path);
					MessageDialog.open(MessageDialog.ERROR, window.getShell(),
							IDEWorkbenchMessages.OpenDelayedFileAction_title,
							msg, SWT.SHEET);
				}
			}

			/*
			 * Helper function to invoke a method on an object.
			 */
			private Object invoke(Object object, String method) throws NoSuchMethodException, SecurityException,
					IllegalAccessException, IllegalArgumentException, InvocationTargetException {
				Method getDocumentProvider = object.getClass().getMethod(method);
				return getDocumentProvider.invoke(object);
			}

			/*
			 * Helper function to invoke a method on an object with arguments.
			 */
			private Object invoke(Object object, String method, Class[] classes, Object[] params)
					throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
					InvocationTargetException {
				Method getDocumentProvider = object.getClass().getMethod(method, classes);
				return getDocumentProvider.invoke(object, params);
			}
		});
	}
}
