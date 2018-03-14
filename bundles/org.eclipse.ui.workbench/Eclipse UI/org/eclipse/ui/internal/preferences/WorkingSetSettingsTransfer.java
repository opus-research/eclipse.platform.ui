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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AbstractWorkingSetManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSetManager;

/**
 * The WorkingSetSettingsTransfer is the settings transfer for the workbench
 * working sets.
 *
 * @since 3.3
 *
 */
public class WorkingSetSettingsTransfer extends WorkbenchSettingsTransfer {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.preferences.SettingsTransfer#getName()
	 */
	@Override
	public String getName() {
		return WorkbenchMessages.WorkingSets_Name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.preferences.SettingsTransfer#transferSettings(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		IPath newDataLocation = getDataLocation(newWorkspaceRoot);
		createDirectoryHierarchy(newDataLocation);
		IPath workingSetState = newDataLocation
				.append(WorkingSetManager.WORKING_SET_STATE_FILENAME);
		return saveWorkingSetState(workingSetState.toOSString());
	}

	private IStatus saveWorkingSetState(String targetFileLocation) {
		try {
			IWorkingSetManager manager = PlatformUI.getWorkbench()
					.getWorkingSetManager();
			if (manager instanceof AbstractWorkingSetManager) {
				File targetFile = new File(targetFileLocation);
				((AbstractWorkingSetManager) manager).saveState(targetFile);
			} else {
				return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
						WorkbenchMessages.WorkingSets_CannotSave);
			}
		} catch (IOException e) {
			new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					WorkbenchMessages.ProblemSavingWorkingSetState_message, e);
		}
		return Status.OK_STATUS;
	}
}
