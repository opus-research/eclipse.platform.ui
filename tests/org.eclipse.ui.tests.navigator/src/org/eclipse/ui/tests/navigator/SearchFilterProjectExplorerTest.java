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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.navigator.resources.actions.WorkingSetActionProvider;
import org.eclipse.ui.internal.navigator.workingsets.WorkingSetsContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SearchFilterProjectExplorerTest extends SearchFilterTestBase {

	public SearchFilterProjectExplorerTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
	}

	@Before
	@After
	public void resetProjectExplorer() throws Exception {
		setProjectExplorerTopLevelWorkingSets(false);
	}

	@Test
	public void testToolItemPresent() {
		ActionContributionItem item = findToolbarItem();
		assertNotNull(item);
	}

	@Test
	public void testFilterIncludesJavaFiles() {
		activateFilter("TestClass");
		DisplayHelper.sleep(2000l);

		TreeItem[] items;
		items = _viewer.getTree().getItems();
		String[] actual = getDeepItemTexts(items);
		System.out.println("testFilterIncludesJavaFiles -> " + Arrays.deepToString(actual));
		assertTrue(Arrays.asList(actual).contains("TestClass.java"));
	}

	@Test
	public void testFilterWorksWithWorkingSets() throws Exception {

		setProjectExplorerTopLevelWorkingSets(true);
		activateFilter("file");
		DisplayHelper.sleep(2000l);

		TreeItem[] items;
		items = _viewer.getTree().getItems();

		String[] actual = getDeepItemTexts(items);

		System.out.println("testFilterWorksWithWorkingSets -> " + Arrays.deepToString(actual));
		assertTrue(Arrays.asList(actual).containsAll(Arrays.asList("file1.txt", "file2.txt")));
	}

	private void setProjectExplorerTopLevelWorkingSets(boolean b) throws Exception {
		// this logic is derived from WorkingSetTest
		IExtensionStateModel extensionStateModel = _contentService
				.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		if (b) {
			// Force the content provider to be loaded so that it responds to
			// the working set events
			INavigatorContentExtension ce = _contentService
					.getContentExtensionById(WorkingSetsContentProvider.EXTENSION_ID);
			ce.getContentProvider();

			IWorkingSet workingSet = new WorkingSet("ws1", "ws1", new IAdaptable[] { _p1 });

			WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
					.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);
			IPropertyChangeListener l = provider.getFilterChangeListener();
			PropertyChangeEvent event = new PropertyChangeEvent(this, WorkingSetFilterActionGroup.CHANGE_WORKING_SET,
					null, workingSet);
			l.propertyChange(event);

			activePage.setWorkingSets(new IWorkingSet[] { workingSet });
		} else {
			activePage.setWorkingSets(new IWorkingSet[] {});
		}
		extensionStateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, b);
		refreshViewer();
		DisplayHelper.sleep(100l);
	}
}
