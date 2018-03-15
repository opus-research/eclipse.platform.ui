package org.eclipse.ui.tests.performance;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Verifies the performance of progress reporting APIs in various contexts which
 * offer progress monitoring.
 */
public class ProgressReportingTest extends BasicPerformanceTest {
	public static final int ITERATIONS = 10000000;

	/**
	 * Maximum time to run each test. Increase to get better results during
	 * profiling.
	 */
	public static final int MAX_RUNTIME = 4000;

	/**
	 * Maximum number of iterations for each test. Increase to get better
	 * results during profiling.
	 */
	public static final int MAX_ITERATIONS = 100;
	private volatile boolean isDone;
	private Display display;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param testName
	 */
	public ProgressReportingTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		this.display = Display.getCurrent();
		super.doSetUp();
	}

	/**
	 * Starts an asynchronous performance test. The test ends whenever
	 * the runnable invokes endAsyncTest
	 */
	public void runAsyncTest(Runnable testContent) throws Exception {
		final Display display = Display.getCurrent();
		tagIfNecessary(getName(), Dimension.ELAPSED_PROCESS);
		exercise(new TestRunnable() {
			@Override
			public void run() throws Exception {
				startMeasuring();

				isDone = false;
				testContent.run();

				for (; !isDone;) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}

				stopMeasuring();
			}
		}, 3, MAX_ITERATIONS, MAX_RUNTIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Ends an asynchronous test
	 *
	 * @param ignored
	 *            Parameter that will be ignored. The test can pass the result
	 *            of a computation here to prevent the compiler from optimizing
	 *            it out.
	 */
	public void endAsyncTest(Object ignored) {
		isDone = true;
		// Trigger an empty asyncExec to ensure the event loop wakes up
		display.asyncExec(() -> {
		});
	}

	/**
	 * Test the overhead of the test framework itself
	 */
	public void testJobNoMonitorUsage() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of setTaskName
	 */
	public void testJobSetTaskName() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while(i < ITERATIONS) {
					monitor.setTaskName(Integer.toString(i));
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of subTask
	 */
	public void testJobSubTask() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					monitor.subTask(Integer.toString(i));
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of isCanceled
	 */
	public void testJobIsCanceled() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of monitor.worked in jobs
	 */
	public void testJobWorked() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					monitor.worked(1);
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of subMonitor.split()
	 */
	public void testJobSubMonitorSplit() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor, ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					subMonitor.split(1);
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of subMonitor.worked()
	 */
	public void testJobSubMonitorWorked() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job.create("Test Job", monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor, ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					subMonitor.worked(1);
					result += i;
					i++;
				}

				endAsyncTest(result);
			}).schedule();
		});
	}

	/**
	 * Test the cost of monitor.subTask in the progress service
	 */
	public void testProgressServiceNoMonitorUsage() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					result += i;
					i++;
				}

				endAsyncTest(result);
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.worked in the progress service
	 */
	public void testProgressServiceWorked() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					monitor.worked(1);
					result += i;
					i++;
				}

				endAsyncTest(result);
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.setTaskName in the progress service
	 */
	public void testProgressServiceSetTaskName() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					monitor.setTaskName(Integer.toString(i));
					result += i;
					i++;
				}

				endAsyncTest(result);
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.subTask in the progress service
	 */
	public void testProgressServiceSubTask() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					monitor.subTask(Integer.toString(i));
					result += i;
					i++;
				}

				endAsyncTest(result);
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of monitor.subTask in the progress service
	 */
	public void testProgressServiceIsCanceled() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			Job j = Job.create("Test Job", monitor -> {
				monitor.beginTask("Test Job", ITERATIONS);
				int i = 0;
				long result = 0;
				while (i < ITERATIONS) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					result += i;
					i++;
				}

				endAsyncTest(result);
			});
			j.schedule();
			PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), j);
		});
	}

	/**
	 * Test the cost of opening a progress monitor dialog without reporting any
	 * progress
	 */
	public void testProgressMonitorDialogNoMonitorUsage() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						monitor.beginTask("Test Job", ITERATIONS);
						int i = 0;
						long result = 0;
						while (i < ITERATIONS) {
							result += i;
							i++;
						}

						endAsyncTest(result);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling worked() in a progress monitor dialog
	 */
	public void testProgressMonitorDialogWorked() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						monitor.beginTask("Test Job", ITERATIONS);
						int i = 0;
						long result = 0;
						while (i < ITERATIONS) {
							monitor.worked(1);
							result += i;
							i++;
						}

						endAsyncTest(result);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling worked() in a progress monitor dialog
	 */
	public void testProgressMonitorDialogIsCanceled() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						monitor.beginTask("Test Job", ITERATIONS);
						int i = 0;
						long result = 0;
						while (i < ITERATIONS) {
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}
							result += i;
							i++;
						}

						endAsyncTest(result);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling setTaskName in a progress monitor dialog.
	 */
	public void testProgressMonitorDialogSetTaskName() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						monitor.beginTask("Test Job", ITERATIONS);
						int i = 0;
						long result = 0;
						while (i < ITERATIONS) {
							monitor.setTaskName(Integer.toString(i));
							result += i;
							i++;
						}

						endAsyncTest(result);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Test the cost of calling subTask in a progress monitor dialog.
	 */
	public void testProgressMonitorDialogSubTask() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		runAsyncTest(() -> {
			try {
				new ProgressMonitorDialog(window.getShell()).run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						monitor.beginTask("Test Job", ITERATIONS);
						int i = 0;
						long result = 0;
						while (i < ITERATIONS) {
							monitor.subTask(Integer.toString(i));
							result += i;
							i++;
						}

						endAsyncTest(result);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
