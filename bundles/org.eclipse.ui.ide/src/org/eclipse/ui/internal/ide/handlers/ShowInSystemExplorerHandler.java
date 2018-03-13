/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.106
 */
public class ShowInSystemExplorerHandler extends AbstractHandler {

	/**
	 * Command id
	 */
	public static final String ID = "org.eclipse.ui.showIn.systemExplorer"; //$NON-NLS-1$

	private static final String VARIABLE_RESOURCE = "${selected_resource_loc}"; //$NON-NLS-1$
	private static final String VARIABLE_FOLDER = "${selected_resource_parent}"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ILog log = IDEWorkbenchPlugin.getDefault().getLog();

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if ((selection == null) || (selection.isEmpty())
				|| (!(selection instanceof IStructuredSelection))) {
			return null;
		}

		Object selectedObject = ((IStructuredSelection) selection)
				.getFirstElement();
		IResource item = (IResource) org.eclipse.ui.internal.util.Util
				.getAdapter(selectedObject, IResource.class);
		if (item == null) {
			return null;
		}

		String logMsgPrefix;
		try {
			logMsgPrefix = event.getCommand().getName() + ": "; //$NON-NLS-1$
		} catch (NotDefinedException e) {
			// will used id instead...
			logMsgPrefix = event.getCommand().getId() + ": "; //$NON-NLS-1$
		}

		try {
			File canonicalPath = getSystemExplorerPath(item);
			if (canonicalPath == null) {
				StatusManager
						.getManager()
						.handle(new Status(
								IStatus.ERROR,
								IDEWorkbenchPlugin.getDefault().getBundle()
										.getSymbolicName(),
								logMsgPrefix
										+ IDEWorkbenchMessages.ShowInSystemExplorerHandler_notDetermineLocation),
								StatusManager.SHOW | StatusManager.LOG);
				return null;
			}
			String[] launchCmd = formCommandParts(canonicalPath);

			if (launchCmd.length == 0) {
				StatusManager
						.getManager()
						.handle(new Status(
								IStatus.ERROR,
								IDEWorkbenchPlugin.getDefault().getBundle()
										.getSymbolicName(),
								logMsgPrefix
										+ IDEWorkbenchMessages.ShowInSystemExplorerHandler_commandUnavailable),
								StatusManager.SHOW | StatusManager.LOG);
				return null;
			}

			Process p = Runtime.getRuntime().exec(launchCmd, null,
					item.getWorkspace().getRoot().getLocation().toFile());
			int retCode = p.waitFor();
			if (retCode != 0 && !Util.isWindows()) {
				log.log(new Status(IStatus.ERROR, IDEWorkbenchPlugin
						.getDefault().getBundle().getSymbolicName(),
						logMsgPrefix + "Execution of '" + Util.toString(launchCmd) //$NON-NLS-1$
								+ "' failed with return code: " + retCode)); //$NON-NLS-1$
			}
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, IDEWorkbenchPlugin.getDefault()
					.getBundle().getSymbolicName(), logMsgPrefix
					+ "Unhandled failure.", e)); //$NON-NLS-1$
			throw new ExecutionException("Show in Explorer command failed.", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Prepare command for launching system explorer to show a path
	 * 
	 * @param path
	 *            the path to show
	 * @return the command part array that shows the path
	 */
	private String[] formCommandParts(File path) throws IOException {
		List commandParts = new ArrayList();
		String command = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(IDEInternalPreferences.WORKBENCH_SYSTEM_EXPLORER);
		// Split the command before replacing variables with paths, as the paths
		// can contain spaces and we want a path to be a complete part.
		StringTokenizer tokenizer = new StringTokenizer(command);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			String replacedCommandPart = replaceVariables(token, path);
			commandParts.add(replacedCommandPart);
		}
		return (String[]) commandParts.toArray(new String[commandParts.size()]);
	}

	private String replaceVariables(String commandPart, File path) throws IOException {
		commandPart = Util.replaceAll(commandPart, VARIABLE_RESOURCE, path.getCanonicalPath());
		File parent = path.getParentFile();
		if (parent != null) {
			commandPart = Util.replaceAll(commandPart, VARIABLE_FOLDER, parent.getCanonicalPath());
		}
		return commandPart;
	}

	/**
	 * Returns the path used for a resource when showing it in the system
	 * explorer
	 * 
	 * @see File#getCanonicalPath()
	 * @param resource
	 *            the {@link IResource} object to be used
	 * @return the canonical path to show in the system explorer for this
	 *         resource, or null if it cannot be determined
	 * @throws IOException
	 *             if an I/O error occurs while trying to determine the path
	 */
	private File getSystemExplorerPath(IResource resource) throws IOException {
		IPath location = resource.getLocation();
		if (location == null)
			return null;
		return location.toFile();
	}

	/**
	 * The default command for launching the system explorer on this platform.
	 * 
	 * @return The default command which launches the system explorer on this
	 *         system, or an empty string if no default exists.
	 */
	public static String getDefaultCommand() {
		if (Util.isGtk()) {
			return "dbus-send --print-reply --dest=org.freedesktop.FileManager1 /org/freedesktop/FileManager1 org.freedesktop.FileManager1.ShowItems array:string:file://${selected_resource_loc} string:\"\""; //$NON-NLS-1$
		} else if (Util.isWindows()) {
			return "explorer /E,/select=${selected_resource_loc}"; //$NON-NLS-1$
		} else if (Util.isMac()) {
			return "open -R ${selected_resource_loc}"; //$NON-NLS-1$
		}

		// if all else fails, return empty default
		return ""; //$NON-NLS-1$
	}
}
