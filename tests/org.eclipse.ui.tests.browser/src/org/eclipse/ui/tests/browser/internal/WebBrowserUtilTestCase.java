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
import static org.eclipse.ui.internal.browser.WebBrowserUtil.createParameterString;
import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class WebBrowserUtilTestCase extends TestCase {

	public void testCreateParameterString() {
		assertEquals("", createParameterString(null, null));
		assertEquals("parameters ", createParameterString("parameters", null));
		assertEquals("url", createParameterString(null, "url"));
		assertEquals("parameters url", createParameterString("parameters ", "url"));
		assertEquals("parameters url", createParameterString("parameters", "url"));
		assertEquals("param1  param2", createParameterString("param1 " + URL_PARAMETER + " param2", null));
		assertEquals("param1 url param2", createParameterString("param1 " + URL_PARAMETER + " param2", "url"));
		assertEquals("param1 url param2 url",
				createParameterString("param1 " + URL_PARAMETER + " param2 " + URL_PARAMETER, "url"));
	}

}
