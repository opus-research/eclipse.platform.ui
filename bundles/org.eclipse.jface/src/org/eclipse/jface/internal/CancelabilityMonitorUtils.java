/*******************************************************************************
 * Copyright (c) 2016 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mikaël Barbero (Eclipse Foundation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal;

import org.eclipse.core.internal.runtime.CancelabilityMonitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Policy;

/**
 * Utility methods to work with {@link CancelabilityMonitor}.
 *
 * @since 3.13
 */
public class CancelabilityMonitorUtils {

	/**
	 * Creates a monitoring progress monitor wrapping the given {@code monitor}
	 * if condition is true and if monitoring is enabled.
	 *
	 * @param condition
	 *            whether a monitoring wrapper should be created
	 * @param monitor
	 *            the monitor to be wrapped
	 * @param taskName
	 *            the name of the task the monitor will indicate progress (used
	 *            for logging)
	 * @return a monitoring progress monitor or the given progress monitor.
	 * @see InternalPolicy#getCancelabilityMonitorOptions()
	 */
	public static IProgressMonitor aboutToStart(boolean condition, IProgressMonitor monitor, String taskName) {
		final IProgressMonitor pm;
		if (condition) {
			pm = aboutToStart(monitor, taskName);
		} else {
			pm = monitor;
		}
		return pm;
	}

	/**
	 * Creates a monitoring progress monitor wrapping the given {@code monitor}
	 * if monitoring is enabled.
	 *
	 * @param monitor
	 *            the monitor to be wrapped
	 * @param taskName
	 *            the name of the task the monitor will indicate progress (used
	 *            for logging)
	 * @return a monitoring progress monitor or the given progress monitor.
	 * @see InternalPolicy#getCancelabilityMonitorOptions()
	 */
	private static IProgressMonitor aboutToStart(IProgressMonitor monitor, String taskName) {
		final IProgressMonitor pm;
		CancelabilityMonitor.Options monitoringOptions = InternalPolicy.getCancelabilityMonitorOptions();
		if (monitoringOptions != null && monitoringOptions.enabled()) {
			pm = new CancelabilityMonitor(taskName, monitor, monitoringOptions).aboutToStart();
		} else {
			pm = monitor;
		}
		return pm;
	}

	/**
	 * Indicates that the task using the given monitor has stopped. If the given
	 * monitor is a {@link CancelabilityMonitor}, the elapsed time is recorded
	 * and the cancelability status of the task if log if required. Do nothing
	 * if the given monitor is not a {@link CancelabilityMonitor}.
	 *
	 * @param pm
	 *            the monitor to stop.
	 */
	public static void hasStopped(IProgressMonitor pm) {
		if (pm instanceof CancelabilityMonitor) {
			((CancelabilityMonitor) pm).hasStopped();
			IStatus cancelabilityStatus = ((CancelabilityMonitor) pm).createCancelabilityStatus();
			if (!cancelabilityStatus.isOK()) {
				Policy.getLog().log(cancelabilityStatus);
			}
		}
	}
}
