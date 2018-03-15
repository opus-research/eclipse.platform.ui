/*******************************************************************************
 * Copyright (c) 2017 Stefan Winkler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Winkler - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SearchFilterHiddenTest extends SearchFilterTestBase {

	public SearchFilterHiddenTest() {
		_navigatorInstanceId = TEST_VIEWER_SEARCHFILTER_HIDDEN;
	}

	@Test
	public void testToolItemHiddden() {
		assertNull(findToolbarItem());
	}
}
