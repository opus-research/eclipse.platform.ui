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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.Test;

public class SearchFilterTest extends SearchFilterTestBase {

	public SearchFilterTest() {
		_navigatorInstanceId = TEST_VIEWER_SEARCHFILTER;
	}

	@Test
	public void testToolItemPresent() {
		ActionContributionItem item = findToolbarItem();
		assertNotNull(item);
	}

	@Test
	public void testFilterIncludesFiles() {

		assertFalse(findToolbarItem().getAction().isChecked());
		activateFilter("file1");
		DisplayHelper.sleep(500l);
		assertTrue(findToolbarItem().getAction().isChecked());

		TreeItem[] items;
		items = _viewer.getTree().getItems();

		String[] actual = getDeepItemTexts(items);
		System.out.println("testFilterIncludesFiles -> " + Arrays.deepToString(actual));
		assertTrue(Arrays.asList(actual).containsAll(Arrays.asList("file1.txt")));
	}

	@Test
	public void testFilterIncludesOtherObjects() {
		activateFilter("child");

		DisplayHelper.sleep(500l);

		TreeItem[] items;
		items = _viewer.getTree().getItems();

		String[] expectedItemTexts = new String[] { "Test", "Parent", "Child1", "Grandchild1", "Grandchild2", "Child2",
				"x Grandchild3 x", "x Child3 x" };
		String[] actual = getDeepItemTexts(items);

		System.out.println("testFilterIncludesOtherObjects -> " + Arrays.deepToString(actual));

		assertArrayEquals(expectedItemTexts, actual);
	}
}
