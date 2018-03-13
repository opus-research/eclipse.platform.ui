/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bundle activator
 */
public class SWTRenderersActivator implements BundleActivator {
	/**
	 * The bundle symbolic name.
	 */
	public static final String PI_WORKBENCH_RENDERERS = "org.eclipse.e4.ui.workbench"; //$NON-NLS-1$

	public static final String TRACE_PARTS = "/trace/parts"; //$NON-NLS-1$

	private ServiceTracker debugTracker;

	private DebugTrace trace;

	/**
	 * The bundle associated this plug-in
	 */
	private static BundleContext bundleContext;

	private static SWTRenderersActivator activator;

	public static SWTRenderersActivator getDefault() {
		return activator;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		activator = this;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		activator = null;
		bundleContext = null;
	}

	static BundleContext getContext() {
		return bundleContext;
	}

	public DebugOptions getDebugOptions() {
		if (debugTracker == null) {
			if (bundleContext == null)
				return null;
			debugTracker = new ServiceTracker(bundleContext,
					DebugOptions.class.getName(), null);
			debugTracker.open();
		}
		return (DebugOptions) debugTracker.getService();
	}

	public DebugTrace getTrace() {
		if (trace == null) {
			trace = getDebugOptions().newDebugTrace(PI_WORKBENCH_RENDERERS);
		}
		return trace;
	}

	public static boolean isTracing(String option) {
		final DebugOptions debugOptions = activator.getDebugOptions();
		return debugOptions.isDebugEnabled()
				&& debugOptions.getBooleanOption(PI_WORKBENCH_RENDERERS
						+ option, false);
	}

	public static void trace(String option, String msg, Throwable error) {
		if (isTracing(option)) {
			System.out.println(msg);
			if (error != null) {
				error.printStackTrace(System.out);
			}
		}
		activator.getTrace().trace(option, msg, error);
	}

}
