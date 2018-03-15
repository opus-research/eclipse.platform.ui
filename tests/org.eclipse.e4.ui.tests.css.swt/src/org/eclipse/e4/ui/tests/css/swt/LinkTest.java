
/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Sadau <lars@sadau-online.de>
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.Link;
import org.junit.Ignore;
import org.junit.Test;

public class LinkTest extends CSSSWTTestCase {
	@Test
	public void testColor() {
		Link widgetToTest = createTestLink("Link { color: #0000FF; } Link:link {  color: #FF0000; }");
		assertEquals(BLUE, widgetToTest.getForeground().getRGB());
		assertEquals(RED, widgetToTest.getLinkForeground().getRGB());
	}

	@Test
	public void testBackgroundColor() {
		Link widgetToTest = createTestLink(
				"Link { background-color: #FF0000; color: #00FF00; }");
		assertEquals(RED, widgetToTest.getBackground().getRGB());
		assertEquals(GREEN, widgetToTest.getForeground().getRGB());
	}

	@Test
	@Ignore
	// TODO is that a bug?
	public void testWeird() {
		Link widgetToTest = createTestLink(
				"Link { background-color: #00FF00; color: #0000FF; } "
						+ "Link:link {  background-color: #0000FF; color: #FF0000; }");
		assertEquals(BLUE, widgetToTest.getForeground().getRGB());
		assertEquals(BLUE, widgetToTest.getBackground().getRGB());
		assertEquals(RED, widgetToTest.getLinkForeground().getRGB());
	}
}
