/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A progress monitor that delegates report to another one, but keep control on
 * the canceled state. A typical usage is to have multiple operations hooked on
 * the same progress monitor: use this DelegateProgressMonitorExceptCancel for
 * each operation so they can be canceled separately but still report to same
 * progress monitor when running.
 *
 * @since 3.12
 */
class DelegateProgressMonitorExceptCancel implements IProgressMonitor {
	private boolean canceled;
	private IProgressMonitor delegate;

	/**
	 * @param delegate
	 */
	public DelegateProgressMonitorExceptCancel(IProgressMonitor delegate) {
		this.delegate = delegate;
	}

	@Override
	public void worked(int work) {
		if (!canceled) {
			delegate.worked(work);
		}
	}

	@Override
	public void subTask(String name) {
		if (!canceled) {
			delegate.subTask(name);
		}
	}

	@Override
	public void setTaskName(String name) {
		if (!canceled) {
			delegate.setTaskName(name);
		}
	}

	@Override
	public void setCanceled(boolean value) {
		this.canceled = value;
	}

	@Override
	public boolean isCanceled() {
		return this.canceled;
	}

	@Override
	public void internalWorked(double work) {
		if (!canceled) {
			delegate.internalWorked(work);
		}
	}

	@Override
	public void done() {
		if (!canceled) {
			delegate.done();
		}
	}

	@Override
	public void beginTask(String name, int totalWork) {
		if (!canceled) {
			delegate.beginTask(name, totalWork);
		}
	}
}