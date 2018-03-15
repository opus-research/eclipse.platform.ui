/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Patrik Suzzi <psuzzi@gmail.com> - Bug 460683
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManager.JobMonitor;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The ProgressMonitorFocusJobDialog is a dialog that shows progress for a
 * particular job in a modal dialog so as to give a user accustomed to a modal
 * UI a more familiar feel.
 */
public class ProgressMonitorFocusJobDialog extends ProgressMonitorJobsDialog {
	Job job;
	private boolean showDialog;

	/**
	 * Create a new instance of the receiver with progress reported on the job.
	 *
	 * @param parentShell
	 *            The shell this is parented from.
	 */
	public ProgressMonitorFocusJobDialog(Shell parentShell) {
		super(parentShell == null ? ProgressManagerUtil.getNonModalShell()
				: parentShell);
		setShellStyle(getDefaultOrientation() | SWT.BORDER | SWT.TITLE
				| SWT.RESIZE | SWT.MAX | SWT.MODELESS);
		setCancelable(true);
		enableDetailsButton = true;
	}

	@Override
	protected void cancelPressed() {
		job.cancel();
		super.cancelPressed();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(job.getName());
		shell.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					cancelPressed();
					e.detail = SWT.TRAVERSE_NONE;
					e.doit = true;
				}
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button runInWorkspace = createButton(
				parent,
				IDialogConstants.CLOSE_ID,
				ProgressMessages.ProgressMonitorFocusJobDialog_RunInBackgroundButton,
				true);
		runInWorkspace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Rectangle shellPosition = getShell().getBounds();
				job.setProperty(IProgressConstants.PROPERTY_IN_DIALOG,
						Boolean.FALSE);
				finishedRun();
				ProgressManagerUtil.animateDown(shellPosition);
			}
		});
		runInWorkspace.setCursor(arrowCursor);

		cancel = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		cancel.setCursor(arrowCursor);

		createDetailsButton(parent);
	}

	/**
	 * Returns a listener that will close the dialog when the job completes.
	 *
	 * @return IJobChangeListener
	 */
	private IJobChangeListener createCloseListener() {
		return new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				// first of all, make sure this listener is removed
				event.getJob().removeJobChangeListener(this);
				if (!PlatformUI.isWorkbenchRunning()) {
					return;
				}
				// nothing to do if the dialog is already closed
				if (getShell() == null) {
					return;
				}
				WorkbenchJob closeJob = new WorkbenchJob(
						ProgressMessages.ProgressMonitorFocusJobDialog_CLoseDialogJob) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						Shell currentShell = getShell();
						if (currentShell == null || currentShell.isDisposed()) {
							return Status.CANCEL_STATUS;
						}
						finishedRun();
						return Status.OK_STATUS;
					}
				};
				closeJob.setSystem(true);
				closeJob.schedule();
			}
		};
	}

	@Override
	public int open() {
		int result = super.open();

		// add a listener that will close the dialog when the job completes.
		IJobChangeListener listener = createCloseListener();
		job.addJobChangeListener(listener);
		if (job.getState() == Job.NONE) {
			// if the job completed before we had a chance to add
			// the listener, just remove the listener and return
			job.removeJobChangeListener(listener);
			finishedRun();
			cleanUpFinishedJob();
		}

		return result;
	}

	/**
	 * Opens this dialog for the duration that the given job is running.
	 *
	 * @param jobToWatch
	 * @param originatingShell
	 *            The shell this request was created from. Do not block on this
	 *            shell.
	 */
	public void show(Job jobToWatch, final Shell originatingShell) {
		job = jobToWatch;
		// after the dialog is opened we can get access to its monitor
		job.setProperty(IProgressConstants.PROPERTY_IN_DIALOG, Boolean.TRUE);

		setOpenOnRun(false);
		aboutToRun();

		final Object jobIsDone = new Object();
		final JobChangeAdapter jobListener = new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				synchronized (jobIsDone) {
					jobIsDone.notify();
				}
			}
		};
		job.addJobChangeListener(jobListener);

		// start with a quick busy indicator. Lock the UI as we
		// want to preserve modality
		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(),
				new Runnable() {
					@Override
					public void run() {
						try {
							synchronized (jobIsDone) {
								if (job.getState() != Job.NONE) {
									jobIsDone.wait(ProgressManagerUtil.SHORT_OPERATION_TIME);
								}
							}
						} catch (InterruptedException e) {
							// Do not log as this is a common operation from the
							// lock listener
						}
					}
				});
		job.removeJobChangeListener(jobListener);

		WorkbenchJob openJob = new WorkbenchJob(
				ProgressMessages.ProgressMonitorFocusJobDialog_UserDialogJob) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				// if the job is done at this point, we don't need the dialog
				if (job.getState() == Job.NONE) {
					finishedRun();
					cleanUpFinishedJob();
					return Status.CANCEL_STATUS;
				}

				// now open the progress dialog if nothing else is
				if (!ProgressManagerUtil.safeToOpen(
						ProgressMonitorFocusJobDialog.this, originatingShell)) {
					return Status.CANCEL_STATUS;
				}

				// Do not bother if the parent is disposed
				if (getParentShell() != null && getParentShell().isDisposed()) {
					return Status.CANCEL_STATUS;
				}

				JobMonitor jobMonitor = ProgressManager.getInstance().progressFor(job);
				Display d = getShell() == null ? Display.getDefault() : getShell().getDisplay();
				jobMonitor.addProgressListener(new AccumulatingProgressMonitor(getProgressMonitor(), d));
				open();

				return Status.OK_STATUS;
			}
		};
		openJob.setSystem(true);
		openJob.schedule();

	}

	/**
	 * A progress monitor that accumulates <code>setTaskName</code>,
	 * <code>worked</code> and <code>subtask</code> calls in the following way
	 * by wrapping a standard progress monitor:
	 * <ul>
	 * <li>When a <code>setTaskName</code>, <code>worked</code> or
	 * <code>subtask</code> call occurs the first time, the progress monitor
	 * posts a runnable into the asynchronous SWT event queue.</li>
	 * <li>Subsequent calls to <code>setTaskName</code>, <code>worked</code> or
	 * <code>subtask</code> do not post a new runnable as long as a previous
	 * runnable still exists in the SWT event queue. In this case, the progress
	 * monitor just updates the internal state of the runnable that waits in the
	 * SWT event queue for its execution. If no runnable exists, a new one is
	 * created and posted into the event queue.
	 * </ul>
	 * <p>
	 * This class is internal to the framework; clients outside JFace should not
	 * use this class.
	 * </p>
	 */
	/* package */class AccumulatingProgressMonitor extends ProgressMonitorWrapper {

		/**
		 * The display.
		 */
		private Display display;

		/**
		 * The collector, or <code>null</code> if none.
		 */
		private Collector collector;

		private String currentTask = ""; //$NON-NLS-1$

		private class Collector implements Runnable {
			private String taskName;

			private String subTask;

			private double worked;

			private IProgressMonitor monitor;

			/**
			 * Create a new collector.
			 *
			 * @param taskName
			 * @param subTask
			 * @param work
			 * @param monitor
			 */
			public Collector(String taskName, String subTask, double work, IProgressMonitor monitor) {
				this.taskName = taskName;
				this.subTask = subTask;
				this.worked = work;
				this.monitor = monitor;
			}

			/**
			 * Set the task name
			 *
			 * @param name
			 */
			public void setTaskName(String name) {
				this.taskName = name;
			}

			/**
			 * Add worked to the work.
			 *
			 * @param workedIncrement
			 */
			public void worked(double workedIncrement) {
				this.worked = this.worked + workedIncrement;
			}

			/**
			 * Set the subTask name.
			 *
			 * @param subTaskName
			 */
			public void subTask(String subTaskName) {
				this.subTask = subTaskName;
			}

			/**
			 * Run the collector.
			 */
			@Override
			public void run() {
				clearCollector(this);
				if (taskName != null) {
					monitor.setTaskName(taskName);
				}
				if (subTask != null) {
					monitor.subTask(subTask);
				}
				if (worked > 0) {
					monitor.internalWorked(worked);
				}
			}
		}

		/**
		 * Creates an accumulating progress monitor wrapping the given one that
		 * uses the given display.
		 *
		 * @param monitor
		 *            the actual progress monitor to be wrapped
		 * @param display
		 *            the SWT display used to forward the calls to the wrapped
		 *            progress monitor
		 */
		public AccumulatingProgressMonitor(IProgressMonitor monitor, Display display) {
			super(monitor);
			Assert.isNotNull(display);
			this.display = display;
		}

		@Override
		public void beginTask(final String name, final int totalWork) {
			synchronized (this) {
				collector = null;
			}
			display.asyncExec(() -> {
				currentTask = name;
				getWrappedProgressMonitor().beginTask(name, totalWork);
			});
		}

		/**
		 * Clears the collector object used to accumulate work and subtask calls
		 * if it matches the given one.
		 *
		 * @param collectorToClear
		 */
		private synchronized void clearCollector(Collector collectorToClear) {
			// Check if the accumulator is still using the given collector.
			// If not, don't clear it.
			if (this.collector == collectorToClear) {
				this.collector = null;
			}
		}

		/**
		 * Creates a collector object to accumulate work and subtask calls.
		 *
		 * @param subTask
		 * @param work
		 */
		private void createCollector(String taskName, String subTask, double work) {
			collector = new Collector(taskName, subTask, work, getWrappedProgressMonitor());
			display.asyncExec(collector);
		}

		@Override
		public void done() {
			synchronized (this) {
				collector = null;
			}
			display.asyncExec(() -> getWrappedProgressMonitor().done());
		}

		@Override
		public synchronized void internalWorked(final double work) {
			if (collector == null) {
				createCollector(null, null, work);
			} else {
				collector.worked(work);
			}
		}

		@Override
		public synchronized void setTaskName(final String name) {
			currentTask = name;
			if (collector == null) {
				createCollector(name, null, 0);
			} else {
				collector.setTaskName(name);
			}
		}

		@Override
		public synchronized void subTask(final String name) {
			if (collector == null) {
				createCollector(null, name, 0);
			} else {
				collector.subTask(name);
			}
		}

		@Override
		public synchronized void worked(int work) {
			internalWorked(work);
		}

		@Override
		public void clearBlocked() {

			// If this is a monitor that can report blocking do so.
			// Don't bother with a collector as this should only ever
			// happen once and prevent any more progress.
			final IProgressMonitor pm = getWrappedProgressMonitor();
			if (!(pm instanceof IProgressMonitorWithBlocking)) {
				return;
			}

			display.asyncExec(() -> {
				((IProgressMonitorWithBlocking) pm).clearBlocked();
				Dialog.getBlockedHandler().clearBlocked();
			});
		}

		@Override
		public void setBlocked(final IStatus reason) {
			// If this is a monitor that can report blocking do so.
			// Don't bother with a collector as this should only ever
			// happen once and prevent any more progress.
			final IProgressMonitor pm = getWrappedProgressMonitor();
			if (!(pm instanceof IProgressMonitorWithBlocking)) {
				return;
			}

			display.asyncExec(() -> {
				((IProgressMonitorWithBlocking) pm).setBlocked(reason);
				// Do not give a shell as we want it to block until it opens.
				Dialog.getBlockedHandler().showBlocked(pm, reason, currentTask);
			});
		}
	}

	/**
	 * The job finished before we did anything so clean up the finished
	 * reference.
	 */
	private void cleanUpFinishedJob() {
		ProgressManager.getInstance().checkForStaleness(job);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		// Give the job info as the initial details
		getProgressMonitor().setTaskName(
				ProgressManager.getInstance().getJobInfo(this.job)
						.getDisplayString());
		return area;
	}

	@Override
	protected void createExtendedDialogArea(Composite parent) {

		showDialog = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.RUN_IN_BACKGROUND);
		final Button showUserDialogButton = new Button(parent, SWT.CHECK);
		showUserDialogButton
				.setText(WorkbenchMessages.WorkbenchPreference_RunInBackgroundButton);
		showUserDialogButton
				.setToolTipText(WorkbenchMessages.WorkbenchPreference_RunInBackgroundToolTip);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		showUserDialogButton.setLayoutData(gd);

		showUserDialogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDialog = showUserDialogButton.getSelection();
			}
		});

		super.createExtendedDialogArea(parent);
	}

	@Override
	public boolean close() {
		if (getReturnCode() != CANCEL)
			WorkbenchPlugin.getDefault().getPreferenceStore().setValue(
					IPreferenceConstants.RUN_IN_BACKGROUND, showDialog);

		return super.close();
	}
}
