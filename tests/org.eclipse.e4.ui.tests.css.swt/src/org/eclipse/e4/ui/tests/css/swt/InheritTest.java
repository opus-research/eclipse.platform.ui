/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Winkler - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class InheritTest extends CSSSWTTestCase {

	private CSSEngine engine;
	private Color originalBackgroundColor;
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	/**
	 * Test status quo: if a more general rule sets the background, it applies
	 * also more specific rules with no explicit backgound-color.
	 */
	public void testBackgroundNoInherit() throws Exception {
		Label labelToTest = createTestLabel("Label { background-color: #00FF00; }\n"
				+ "Composite Label { color: #0000FF; }");
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
		assertEquals(GREEN, labelToTest.getBackground().getRGB());
	}

	/**
	 * Test new 'inherit' handling: if a more general rule sets the background,
	 * it does not apply to the more specific rule when it specifies
	 * 'background-color: inherit;' Instead, the background-color is set to
	 * null.
	 */
	public void testBackgroundInherit() throws Exception {
		Label labelToTest = createTestLabel("Label { background-color: #00FF00; }\n"
				+ "Composite Label { background-color: inherit; color: #0000FF; }");
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
		assertEquals(originalBackgroundColor.getRGB(), labelToTest
				.getBackground().getRGB());
	}

	private Label createTestLabel(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout());

		Label labelToTest = new Label(composite, SWT.NONE);

		// remember default background color before CSS is applied
		originalBackgroundColor = labelToTest.getBackground();

		labelToTest.setText("Some label text");

		// Apply styles
		engine.applyStyles(labelToTest, true);
		return labelToTest;
	}
}
