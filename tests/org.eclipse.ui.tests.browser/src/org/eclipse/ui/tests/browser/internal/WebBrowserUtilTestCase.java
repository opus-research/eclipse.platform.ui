/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import static org.eclipse.ui.internal.browser.IBrowserDescriptor.URL_PARAMETER;
import static org.eclipse.ui.internal.browser.WebBrowserUtil.createParameterArray;
import junit.framework.TestCase;

import org.eclipse.ui.internal.browser.WebBrowserUtil;

@SuppressWarnings({ "restriction", "deprecation" })
public class WebBrowserUtilTestCase extends TestCase {

	private static final String URL = "http://127.0.0.1:3873/help/index.jsp";

	public void testNullParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString(null, URL));
	}

	public void testEmptyParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString("", URL));
	}

	public void testNullURL() {
		assertEquals("", WebBrowserUtil.createParameterString("", null));
	}

	public void testNoSubstitution() {
		assertEquals("-console " + URL, WebBrowserUtil.createParameterString("-console", URL));
	}

	public void testSubstitution() {
		assertEquals("-url " + URL + " -console", WebBrowserUtil.createParameterString("-url %URL% -console", URL));
	}

	// Remove when we move to JUnit 4
	private void assertArrayEquals(String[] a1, String[] a2) {
		assertEquals("Arrays have different lengths", a1.length, a2.length);
		for (int i = 0; i < a1.length; i++) {
			assertEquals("Elements differ at index " + i, a1[i], a2[i]);
		}
	}

	public void testArrayNullParameters() {
		assertArrayEquals(new String[] { URL }, WebBrowserUtil.createParameterArray(null, URL));
	}

	public void testArrayEmptyParameters() {
		assertArrayEquals(new String[] { URL }, WebBrowserUtil.createParameterArray("", URL));
	}

	public void testArrayNullURL() {
		assertArrayEquals(new String[0], WebBrowserUtil.createParameterArray("", null));
	}

	public void testArrayNoSubstitution() {
		assertArrayEquals(new String[] { "-console", URL }, WebBrowserUtil.createParameterArray("-console", URL));
	}

	public void testArraySubstitution() {
		assertArrayEquals(new String[] { "-url", URL, "-console" },
				WebBrowserUtil.createParameterArray("-url %URL% -console", URL));
	}

	public void testCreateParameterArray() {
		assertArrayEquals(new String[0], createParameterArray(null, null));
		assertArrayEquals(new String[] { "parameters" }, createParameterArray("parameters", null));
		assertArrayEquals(new String[] { "url" }, createParameterArray(null, "url"));
		assertArrayEquals(new String[] { "parameters", "url" }, createParameterArray("parameters ", "url"));
		assertArrayEquals(new String[] { "parameters", "url" }, createParameterArray("parameters", "url"));
		assertArrayEquals(new String[] { "param1", "param2" },
				createParameterArray("param1 " + URL_PARAMETER + " param2", null));
		assertArrayEquals(new String[] { "param1", "url", "param2" },
				createParameterArray("param1 " + URL_PARAMETER + " param2", "url"));
		assertArrayEquals(new String[] { "param1", "url", "param2", URL_PARAMETER },
				createParameterArray("param1 " + URL_PARAMETER + " param2 " + URL_PARAMETER, "url"));
	}

	public void testCreateParameterArrayForMozilla() {
		assertArrayEquals(new String[] { "-remote", "openURL(url)" },
				createParameterArray(" -remote openURL(" + URL_PARAMETER + ")", "url"));
		assertArrayEquals(new String[] { "parameters", "-remote", "openURL(url)" },
				createParameterArray("parameters -remote openURL(" + URL_PARAMETER + ")", "url"));
	}
}
