/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;
import org.eclipse.ui.statushandlers.StatusManager.INotificationTypes;

/**
 * This is a default workbench error handler.
 *
 * @see WorkbenchAdvisor#getWorkbenchErrorHandler()
 * @since 3.3
 */
public class WorkbenchErrorHandler extends AbstractStatusHandler {

	private ExecutorService uiCheckService = Executors.newFixedThreadPool(1);

	/**
	 * This method checks if is is safe to access UI thread from non-UI code.
	 * The implementation assumes that it is not safe to use UI thread if
	 * execution of a no-op task on UI thread takes longer than 5 seconds.
	 *
	 * @return true if the error handler can run synchronous operations on UI
	 *         thread if {@link #handle(StatusAdapter, int)} is called from
	 *         non-UI code
	 */
	private boolean isSafeToAccessUiThread() {
		Future<?> future = uiCheckService.submit(() -> {
			Display.getDefault().syncExec(() -> {
				/* if this task returns, UI thread is not blocked */
			});
		});
		try {
			// Consider UI thread as blocked if the task needs longer
			future.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean supportsNotification(int type) {
		if (type == INotificationTypes.HANDLED) {
			return true;
		}
		return super.supportsNotification(type);
	}

	private WorkbenchStatusDialogManager statusDialogManager;

	@Override
	public void handle(final StatusAdapter statusAdapter, int style) {
		statusAdapter.setProperty(WorkbenchStatusDialogManager.HINT, Integer.valueOf(style));
		if (((style & StatusManager.SHOW) == StatusManager.SHOW)
				|| ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

			final boolean block = ((style & StatusManager.BLOCK) == StatusManager.BLOCK);

			if (Display.getCurrent() != null) {
				showStatusAdapter(statusAdapter, block);
			} else {
				if (block) {
					if (isSafeToAccessUiThread()) {
						Display.getDefault().syncExec(() -> showStatusAdapter(statusAdapter, true));
					} else {
						// UI thread is blocked: we should not deadlock
						// so post the async task on UI thread
						Display.getDefault().asyncExec(() -> showStatusAdapter(statusAdapter, true));
					}
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							showStatusAdapter(statusAdapter, false);
						}
					});
				}
			}
		}

		if ((style & StatusManager.LOG) == StatusManager.LOG) {
			StatusManager.getManager().addLoggedStatus(
					statusAdapter.getStatus());
			WorkbenchPlugin.getDefault().getLog()
					.log(statusAdapter.getStatus());
		}
	}

	/**
	 * Requests the status dialog manager to show the status adapter.
	 *
	 * @param statusAdapter
	 *            the status adapter to show
	 * @param block
	 *            <code>true</code> to request a modal dialog and suspend the
	 *            calling thread till the dialog is closed, <code>false</code>
	 *            otherwise.
	 */
	private void showStatusAdapter(StatusAdapter statusAdapter, boolean block) {
		if (!PlatformUI.isWorkbenchRunning()) {
			// we are shutting down, so just log
			WorkbenchPlugin.log(statusAdapter.getStatus());
			return;
		}

		getStatusDialogManager().addStatusAdapter(statusAdapter, block);

		if (block) {
			Shell shell;
			while ((shell = getStatusDialogShell()) != null
					&& !shell.isDisposed()) {
				if (!shell.getDisplay().readAndDispatch()) {
					Display.getDefault().sleep();
				}
			}
		}
	}

	private Shell getStatusDialogShell() {
		return (Shell) getStatusDialogManager().getProperty(
				IStatusDialogConstants.SHELL);
	}

	/**
	 * This method returns current {@link WorkbenchStatusDialogManager}.
	 *
	 * @return current {@link WorkbenchStatusDialogManager}
	 */
	private WorkbenchStatusDialogManager getStatusDialogManager() {
		if (statusDialogManager == null) {
			synchronized (this) {
				if (statusDialogManager == null) {
					statusDialogManager = new WorkbenchStatusDialogManager(null);
					statusDialogManager.setProperty(
							IStatusDialogConstants.SHOW_SUPPORT, Boolean.TRUE);
					statusDialogManager.setProperty(
							IStatusDialogConstants.HANDLE_OK_STATUSES,
							Boolean.TRUE);
					statusDialogManager.setProperty(
							IStatusDialogConstants.ERRORLOG_LINK, Boolean.TRUE);
					configureStatusDialog(statusDialogManager);
				}
			}
		}
		return statusDialogManager;
	}

	/**
	 * This methods should be overridden to configure
	 * {@link WorkbenchStatusDialogManager} behavior. It is advised to use only
	 * following methods of {@link WorkbenchStatusDialogManager}:
	 * <ul>
	 * <li>{@link WorkbenchStatusDialogManager#enableDefaultSupportArea(boolean)}</li>
	 * <li>{@link WorkbenchStatusDialogManager#setDetailsAreaProvider(AbstractStatusAreaProvider)}</li>
	 * <li>{@link WorkbenchStatusDialogManager#setSupportAreaProvider(AbstractStatusAreaProvider)}</li>
	 * </ul>
	 * Default configuration does nothing.
	 *
	 * @param statusDialog
	 *            a status dialog to be configured.
	 * @since 3.4
	 */
	protected void configureStatusDialog(
			final WorkbenchStatusDialogManager statusDialog) {
		// default configuration does nothing
	}
}
