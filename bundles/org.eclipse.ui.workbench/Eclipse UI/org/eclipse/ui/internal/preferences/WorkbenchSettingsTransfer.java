/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bartosz Popiela <bartoszpop@gmail.com> - Bug 434108
 ******************************************************************************/

package org.eclipse.ui.internal.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.preferences.SettingsTransfer;

/**
 * The WorkbenchSettingsTransfer is the abstract superclass of settings
 * transfers in the workbench.
 *
 * @since 3.3
 *
 */
public abstract class WorkbenchSettingsTransfer extends SettingsTransfer {
	private static final int CHUNK_SIZE = 8192;
	private static final IPath DATA_LOCATION_SUFFIX = createDataLocationSuffix();

	private static IPath createDataLocationSuffix() {
		IPath dataLocation = getDataLocation();
		IPath currentWorkspaceRoot = Platform.getLocation();
		int segmentsToRemove = dataLocation.matchingFirstSegments(currentWorkspaceRoot);
		return dataLocation.removeFirstSegments(segmentsToRemove);
	}

	private static IPath getDataLocation() {
		return WorkbenchPlugin.getDefault().getDataLocation();
	}

	protected IPath getDataLocation(IPath workspaceRoot) {
		return workspaceRoot.append(getDataLocationSuffix());
	}

	protected IPath getDataLocationSuffix() {
		return DATA_LOCATION_SUFFIX;
	}

	protected void createDirectoryHierarchy(IPath path) {
		File workspaceFile = new File(path.toOSString());
		workspaceFile.mkdirs();
	}

	protected void copyFile(String srcPath, String targetPath, String fileName) throws IOException {
		File workbenchModel = new File(srcPath, fileName);
		if (workbenchModel.exists()) {
			byte[] bytes = new byte[CHUNK_SIZE];
			FileInputStream inputStream = new FileInputStream(workbenchModel);
			FileOutputStream outputStream = new FileOutputStream(new File(targetPath, fileName));
			int read = inputStream.read(bytes, 0, CHUNK_SIZE);
			while (read != -1) {
				outputStream.write(bytes, 0, read);
				read = inputStream.read(bytes, 0, CHUNK_SIZE);
			}
			inputStream.close();
			outputStream.close();
		}
	}
}
