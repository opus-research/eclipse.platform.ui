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
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.lifecycle.PostSave;
import org.eclipse.ui.internal.preferences.WorkbenchSettingsTransfer;

/**
 * The WorkbenchSettings handles the recording and restoring of workbench
 * settings.
 * 
 * @since 3.3
 * 
 */
public class WorkbenchLayoutSettingsTransfer extends WorkbenchSettingsTransfer {
	private static final String DELTAS_FILE = "deltas.xml"; //$NON-NLS-1$
	private static final String WORKBENCH_FILE = "workbench.xmi"; //$NON-NLS-1$
	private static final IPath LAYOUT_DATA_LOCATION = new Path(".metadata/.plugins/org.eclipse.e4.workbench"); //$NON-NLS-1$

	@Override
	public String getName() {
		return WorkbenchMessages.WorkbenchLayoutSettings_Name;
	}

	/**
	 * According to migration to e4 framework location of workbench.xmi file has
	 * changed to org.eclipse.e4.workbench directory, therefore
	 * {@link org.eclipse.ui.internal.preferences.WorkbenchSettingsTransfer#getDataLocationSuffix()
	 * getNewWorkbenchStateLocation(IPath)} returns out-of-date value.
	 * 
	 * @return layout data location path
	 * 
	 * @see org.eclipse.ui.internal.preferences.WorkbenchSettingsTransfer#getDataLocationSuffix()
	 */
	@Override
	protected IPath getDataLocationSuffix() {
		return LAYOUT_DATA_LOCATION;
	}

	@Override
	public IStatus transferSettings(IPath newWorkspaceRoot) {

		final IPath currentDataLocation = getDataLocation(Platform.getLocation());
		final IPath newDataLocation = getDataLocation(newWorkspaceRoot);

		Object workbenchListener = new Object() {
			@Inject
			@PostSave
			public void modelSaved(Logger logger) {
				createDirectoryHierarchy(newDataLocation);
				try {
					copyFile(currentDataLocation.toOSString(), newDataLocation.toOSString(), DELTAS_FILE);
					copyFile(currentDataLocation.toOSString(), newDataLocation.toOSString(), WORKBENCH_FILE);
				} catch (IOException e) {
					logger.error(e, WorkbenchMessages.Workbench_problemsSavingMsg);
				}
			}
		};
		IWorkbench workbench = findWorkbench();
		if (workbench != null) {
			workbench.addWorkbenchListener(workbenchListener);
		}

		return Status.OK_STATUS;
	}

	private IWorkbench findWorkbench() {
		IEclipseContext context = EclipseContextFactory
				.getServiceContext(WorkbenchPlugin.getDefault().getBundleContext());
		return context.get(org.eclipse.e4.ui.workbench.IWorkbench.class);
	}
}