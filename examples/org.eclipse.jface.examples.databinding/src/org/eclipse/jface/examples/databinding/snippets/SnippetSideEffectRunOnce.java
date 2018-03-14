/*******************************************************************************
 * Copyright (c) 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This snippet shows how to use the
 * {@link ISideEffect#runOnce(java.util.function.Supplier, java.util.function.Consumer)}
 * method to react properly, when an observable is changed inside an async
 * action, e.g., a Job.
 *
 * @since 3.2
 *
 */
public class SnippetSideEffectRunOnce {
	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			final Shell shell = new View().createShell();
			// The SWT event loop
			Display display1 = Display.getCurrent();
			while (!shell.isDisposed()) {
				if (!display1.readAndDispatch()) {
					display1.sleep();
				}
			}
		});
	}

	static class View {
		private Text text;

		public Shell createShell() {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			GridLayoutFactory.fillDefaults().applyTo(shell);
			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

			// create a Text widget, which will be bound to the Task summary
			text = new Text(shell, SWT.BORDER);
			gridDataFactory.applyTo(text);

			bindData();

			// Open and return the Shell
			shell.pack();
			shell.open();

			return shell;
		}

		private void bindData() {
			IObservableValue<String> asyncObservable = getAsyncObservable();
			ISideEffect.runOnce(asyncObservable::getValue, text::setText);
		}

		private IObservableValue<String> getAsyncObservable() {

			IObservableValue<String> asyncObservable = new WritableValue<>();
			Job job = new Job("Loading Data async") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					try {
						// sleep for 3 seconds to imitate an async call
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException e) {

					}

					return Status.OK_STATUS;
				}
			};
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					asyncObservable.getRealm().asyncExec(new Runnable() {

						@Override
						public void run() {
							asyncObservable.setValue("Results from EclipseCon.org");
						}
					});
				}
			});

			job.schedule();

			return asyncObservable;
		}
	}

}
