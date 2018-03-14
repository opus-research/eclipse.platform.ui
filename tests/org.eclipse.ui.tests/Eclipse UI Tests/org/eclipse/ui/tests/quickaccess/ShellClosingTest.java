/*******************************************************************************
 * Copyright (c) 2015 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.quickaccess;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;

/**
 * @since 3.5
 */
public class ShellClosingTest extends UITestCase {
	public ShellClosingTest() {
		super(ShellClosingTest.class.getName());
	}

	@Test
	public void testClosingShells() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell[] shells = Display.getDefault().getShells();
				Shell active = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				for (Shell shell : shells) {
					if (!(active == shell) && !shell.isDisposed()) {
						shell.close();
					}
				}
			}
		});
	}
}
