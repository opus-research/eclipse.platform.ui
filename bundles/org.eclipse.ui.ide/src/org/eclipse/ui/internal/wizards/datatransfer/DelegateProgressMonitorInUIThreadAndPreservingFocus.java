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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

/**
 * A progress monitor that delegates report to another one, but ensure focus is
 * preserved during the beginTask operation.
 *
 * @since 3.12
 */
class DelegateProgressMonitorInUIThreadAndPreservingFocus implements IProgressMonitorWithBlocking {
	private IProgressMonitorWithBlocking delegate;
	private Set<IProgressMonitor> cancelTargets = new HashSet<>();
	private Combo focusControl;
	private Display display;

	/**
	 * @param delegate
	 * @param focusControl
	 */
	public DelegateProgressMonitorInUIThreadAndPreservingFocus(IProgressMonitorWithBlocking delegate,
			Combo focusControl) {
		this.delegate = delegate;
		this.display = focusControl.getDisplay();
		this.focusControl = focusControl;
	}

	private void inUIThread(Runnable r) {
		if (display == Display.getCurrent()) {
			r.run();
		} else {
			Display.getDefault().asyncExec(r);
		}
	}

	@Override
	public void worked(int work) {
		inUIThread(() -> delegate.worked(work));
	}

	@Override
	public void subTask(String name) {
		inUIThread(() -> delegate.subTask(name));
	}

	@Override
	public void setTaskName(String name) {
		inUIThread(() -> delegate.setTaskName(name));
	}

	@Override
	public void setCanceled(boolean value) {
		inUIThread(() -> delegate.setCanceled(value));
		this.cancelTargets.stream().forEach(monitor -> monitor.setCanceled(value));
	}

	@Override
	public boolean isCanceled() {
		return delegate.isCanceled();
	}

	@Override
	public void internalWorked(double work) {
		inUIThread(() -> delegate.internalWorked(work));
	}

	@Override
	public void done() {
		inUIThread(() -> delegate.done());
	}

	@Override
	public void beginTask(String name, int totalWork) {
		inUIThread(() -> {
			Point initialSelection = focusControl.getSelection();
			delegate.beginTask(name, totalWork);
			// this is necessary because ProgressMonitorPart
			// sets focus on Stop button
			focusControl.setFocus();
			focusControl.setSelection(initialSelection);
		});
	}

	@Override
	public void setBlocked(IStatus reason) {
		inUIThread(() -> delegate.setBlocked(reason));
	}

	@Override
	public void clearBlocked() {
		inUIThread(() -> delegate.clearBlocked());
	}

	public void addCancelTarget(IProgressMonitor cancelTarget) {
		this.cancelTargets.add(cancelTarget);
	}
}