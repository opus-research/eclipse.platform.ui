/*******************************************************************************
 * Copyright (c) 2016 Remain Software and others,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongman - Wizard with cancelable progress monitor
 *******************************************************************************/
package org.eclipse.jface.snippets.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Example Wizard with Progress Monitor and Cancel
 *
 * @author Wim Jongman <wim.jongman@remainsoftware.com>
 */
public class Snippet071WizardWithProgressAndCancel {

	private static final class MyWizard extends Wizard {
		private int fTasks;

		private int fSubTasks;

		private final class MyPage extends WizardPage {
			private MyPage(String pageName, String title, ImageDescriptor titleImage) {
				super(pageName, title, titleImage);
			}

			@Override
			public void createControl(Composite arg0) {
				Text text = new Text(arg0, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
				text.setMessage("This is the wizard page");
				text.getParent().setFocus();
				setControl(text);
			}
		}

		public MyWizard(int pTasks, int pSubTasks) {
			this.fTasks = pTasks;
			this.fSubTasks = pSubTasks;
		}

		@Override
		public boolean performFinish() {
			try {
				getContainer().run(true, true, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException {
						int tasks = fTasks;
						for (int j = 0; j < tasks; j++) {
							int subTasks = fSubTasks;
							pMonitor.beginTask("Task " + j, subTasks);
							for (int i = 0; i < subTasks; i++) {
								if (!pMonitor.isCanceled())
									pMonitor.subTask("Performing subtask " + i);
								Thread.sleep(1000);
								if (pMonitor.isCanceled()) {
									pMonitor.subTask("Cancel pressed, finishing this step");
								}
								pMonitor.internalWorked(1);
							}
							if (pMonitor.isCanceled())
								break;
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;

		}

		@Override
		public void addPages() {
			addPage(new MyPage("Page1", "First and final page", null));
		}
	}

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		Wizard testWizard = new MyWizard(10, 5);
		testWizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(shell, testWizard);
		dialog.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
