/*******************************************************************************
 * Copyright (c) 2015 Stefan Winkler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - initial contribution
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.junit.Test;

public class Bug459961Test extends CSSSWTTestCase {

	@Test
	public void testRegularColorConstantReference() {
		String cssString = "Label { background-color: COLOR-GREEN; }";

		Label label = createTestLabel(cssString);

		RGBA expected = Display.getDefault().getSystemColor(SWT.COLOR_GREEN).getRGBA();
		RGBA actual = label.getBackground().getRGBA();
		assertRGBAEquals(expected, actual);
	}

	@Test
	public void testTransparentColorConstantReference() {
		String cssString = "Label { background-color: COLOR-TRANSPARENT; }";

		Label label = createTestLabel(cssString);

		RGBA expected = Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT).getRGBA();

		if (Util.isGtk()) {
			// In GTK the background color set using the color specified by the
			// theme. Theme does not have the alpha/transparency specified so it
			// the default value set is 255. COLOR-TRANSPARENT sets the alpha
			// value to 0. So in GTK we need to get the expected color
			// correctly. COLOR_WIDGET_BACKGROUND fetches the background color
			// from the theme but since the alpha value is not defined in the
			// theme we need to set it.
			// more details see Bug 493640
			expected = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGBA();
			expected.alpha = 0;
		}

		RGBA actual = label.getBackground().getRGBA();
		assertRGBAEquals(expected, actual);
	}

	private void assertRGBAEquals(RGBA expected, RGBA actual) {
		assertEquals(expected.rgb.red, actual.rgb.red);
		assertEquals(expected.rgb.blue, actual.rgb.blue);
		assertEquals(expected.rgb.green, actual.rgb.green);
		assertEquals(expected.alpha, actual.alpha);
	}
}
