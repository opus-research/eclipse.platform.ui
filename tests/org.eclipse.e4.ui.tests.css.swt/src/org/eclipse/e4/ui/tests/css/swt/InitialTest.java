/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Winkler - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InitialTest extends CSSSWTTestCase {

	private Color redColor;

	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	static final RGB RED = new RGB(255, 0, 0);
	static final RGB DEFAULT_BG = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB();

	@Before
	@Override
	public void setUp() {
		super.setUp();
		redColor = new Color(display, RED);
	}

	@After
	@Override
	public void tearDown() {
		redColor.dispose();
		super.tearDown();
	}

	/**
	 * Test 'initial' handling: if a more general rule sets the background, it
	 * does not apply to the more specific rule when it specifies
	 * 'background-color: initial;' The background-color is not set at all and
	 * should be the default color..
	 */
	@Test
	public void testBackgroundInitial() throws Exception {
		Label labelToTest = createTestLabel(
				"Label { background-color: #00FF00; }\n"
						+ "Composite { background-color: #FF0000; } \n"
						+ "Composite Label { background-color: initial; color: #0000FF; }", false);
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
		assertEquals(DEFAULT_BG, labelToTest.getBackground().getRGB());
	}

	/**
	 * Create a test label on a red canvas. Then apply given styles.
	 *
	 * @param styleSheet
	 *            styles to apply
	 * @param setCompositeBackgroundExplicitly
	 * @return the test label
	 */
	private Label createTestLabel(String styleSheet,
			boolean setCompositeBackgroundExplicitly) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout());
		if (setCompositeBackgroundExplicitly) {
			composite.setBackground(redColor);
		}

		Label labelToTest = new Label(composite, SWT.NONE);

		labelToTest.setText("Some label text");

		// Apply styles
		engine.applyStyles(shell, true);
		return labelToTest;
	}
}
