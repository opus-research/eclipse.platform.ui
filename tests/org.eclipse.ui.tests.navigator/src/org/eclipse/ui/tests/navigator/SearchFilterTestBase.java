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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.navigator.filters.search.CommonNavigatorSearchFilterHelper;
import org.eclipse.ui.navigator.CommonViewer;
import org.junit.After;
import org.junit.Before;

public abstract class SearchFilterTestBase extends NavigatorTestBase {
	protected CommonNavigatorSearchFilterHelper helper = CommonNavigatorSearchFilterHelper.getInstance();

	@Before
	@After
	public void deactivateFilter() {
		helper.deactivateFilter(_viewer);
	}

	protected ActionContributionItem findToolbarItem() {
		IContributionItem item = _viewer.getCommonNavigator().getViewSite().getActionBars().getToolBarManager()
				.find(ActionFactory.FIND.getId());
		return (ActionContributionItem) item;
	}

	protected String[] getDeepItemTexts(TreeItem... items) {
		List<String> result = new ArrayList<>();

		for (TreeItem item : items) {
			result.add(item.getText());
			result.addAll(Arrays.asList(getDeepItemTexts(item.getItems())));
		}

		return result.toArray(new String[result.size()]);
	}

	protected void activateFilter(String filterPattern) {
		helper.activateFilter(_viewer);
		Text filterText = getFilterTextField(_viewer);
		filterText.setText(filterPattern);
	}

	protected Text getFilterTextField(CommonViewer viewer) {
		Composite contentComposite = viewer.getControl().getParent().getParent();
		Control child = contentComposite.getChildren()[0];
		Text result = (Text) ((Composite) child).getChildren()[0];
		return result;
	}
}
