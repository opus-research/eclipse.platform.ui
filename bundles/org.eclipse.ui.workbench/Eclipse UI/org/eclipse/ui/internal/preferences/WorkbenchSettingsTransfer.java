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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

	protected void copyFile(String srcDir, String targetDir, String fileName) throws IOException {
		Path srcFile = Paths.get(srcDir, fileName);
		if (Files.exists(srcFile)) {
			Path targetFile = Paths.get(targetDir, fileName);
			Files.copy(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
