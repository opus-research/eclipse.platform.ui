/*******************************************************************************
 * Copyright (c) 2010, 2014 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     Bachmann electronic GmbH - Bug 447530 - adding a test for active non visible filters
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;

public class FilterTest extends NavigatorTestBase {

	private static final String TEST_FILTER_ACTIVE_NOT_VISIBLE = "org.eclipse.ui.tests.navigator.filters.p2";

	public FilterTest() {
		_navigatorInstanceId = TEST_VIEWER_FILTER;
	}

	// bug 292813 Add API for high level activation of filters
	public void testFilterActivation() throws Exception {

		_contentService.bindExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);

		_viewer.expandAll();

		TreeItem[] items;
		items = _viewer.getTree().getItems();
		assertEquals(3, items.length);
		
		NavigatorContentService ncs = (NavigatorContentService) _contentService;
		
		// Bug 305703 Make sure that contribution memory does not leak on filters
		assertEquals(0, ncs.getContributionMemorySize());

		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(
				new String[] { TEST_FILTER_P1, TEST_FILTER_P2 });

		items = _viewer.getTree().getItems();
		assertEquals(1, items.length);
		assertEquals("Test", items[0].getText());
		assertEquals(0, ncs.getContributionMemorySize());
		
		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(
				new String[] { TEST_FILTER_P1 });

		items = _viewer.getTree().getItems();
		assertEquals(2, items.length);
		assertEquals("p2", items[0].getText());
		assertEquals(0, ncs.getContributionMemorySize());

		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(new String[] {});

		items = _viewer.getTree().getItems();
		assertEquals(3, items.length);
		assertEquals("p1", items[0].getText());
		assertEquals("p2", items[1].getText());
		assertEquals(0, ncs.getContributionMemorySize());

	}

	// bug 447530, when a filter is active by default but not visible in the ui,
	// it must still be active after
	// restoring the active filters from the preferences.
	public void testNonVisibleFilters() {
		_contentService.getFilterService().persistFilterActivationState();
		// new instance will load the persistent filter ids, that must be active
		NavigatorFilterService filterService = new NavigatorFilterService((NavigatorContentService) _contentService);
		assertTrue(filterService.isActive(TEST_FILTER_ACTIVE_NOT_VISIBLE));
	}

}
