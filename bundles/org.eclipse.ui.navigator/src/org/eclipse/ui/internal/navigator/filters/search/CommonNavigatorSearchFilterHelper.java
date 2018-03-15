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
package org.eclipse.ui.internal.navigator.filters.search;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * This singleton class contains helpers to create and handle UI relating to the
 * search filter text field.
 *
 * Since we are past API freeze for 4.7 (Oxygen), accessing the filterComposite
 * in the commonNavigator is not possible directly. After 4.7 a respective
 * accessor could be added to commonNavigator to expose the filterComposite and
 * this class could be refatored accordingly.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 * @since 3.7
 */
public final class CommonNavigatorSearchFilterHelper {
	/**
	 * A marker string to mark the filterComposite in the
	 * {@link CommonNavigator}.
	 */
	private static String FILTER_COMPOSITE = "FILTER_COMPOSITE"; //$NON-NLS-1$

	/**
	 * The path to the "busy" image which is used to indicate that a filter
	 * calculation is in progress.
	 */
	private static String BUSY_IMAGE_PATH = "/icons/full/elcl16/busy.png"; //$NON-NLS-1$

	/**
	 * The singleton instance.
	 */
	private static CommonNavigatorSearchFilterHelper instance = new CommonNavigatorSearchFilterHelper();

	/**
	 * Singleton constructor.
	 */
	private CommonNavigatorSearchFilterHelper() {
	}

	/**
	 * Retrieve the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static CommonNavigatorSearchFilterHelper getInstance() {
		return instance;
	}

	/**
	 * Create the filterComposite for the {@link CommonNavigator}.
	 *
	 * This creates the composite, marks it as <code>FILTER_COMPOSITE</code>,
	 * and adds the filter text field and "busy" image. The composite is hidden
	 * by default.
	 *
	 * @param parent
	 *            the {@link CommonNavigator}'s content composite. (It must have
	 *            a GridLayout configured)
	 * @param commonNavigator
	 *            the {@link CommonNavigator} for which to create the filter
	 *            composite
	 * @return the new filterComposite
	 */
	public Composite createFilterTextField(final Composite parent, final CommonNavigator commonNavigator) {
		Composite filterComposite = new Composite(parent, SWT.NONE);
		filterComposite.setLayout(new GridLayout(2, false));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.heightHint = 0;
		filterComposite.setLayoutData(layoutData);
		filterComposite.setVisible(false);
		filterComposite.setData(FILTER_COMPOSITE);

		Text filterText = new Text(filterComposite, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		filterText.addListener(SWT.DefaultSelection, e -> handleFilterSelectionEvent(commonNavigator, e));
		filterText.addListener(SWT.KeyDown, e -> handleKeyDownEvent(commonNavigator, e));
		filterText.addListener(SWT.Modify, e -> filterNavigatorTree(commonNavigator, filterText.getText()));

		Image busyImage = NavigatorPlugin.getDefault().getImage(BUSY_IMAGE_PATH);
		Label busyImageLabel = new Label(filterComposite, SWT.NONE);
		busyImageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		busyImageLabel.setImage(busyImage);
		busyImageLabel.setVisible(false);

		return filterComposite;
	}

	/**
	 * Called when the search or cancel icons are clicked in the filter text
	 * field. A click on the search icon is ignored. A click on the cancel icon
	 * leads to the filter composite to be hidden and the filter to be
	 * deactivated.
	 *
	 * @param commonNavigator
	 *            the {@link CommonNavigator} to which the filter belongs
	 * @param e
	 *            the SWT event
	 */
	private void handleFilterSelectionEvent(CommonNavigator commonNavigator, Event e) {
		if (e.detail == SWT.ICON_CANCEL) {
			deactivateFilter(commonNavigator.getCommonViewer());
		}
	}

	/**
	 * Called when the user presses a key. If the user presses the ESC key, the
	 * search shall be cancelled and hidden.
	 *
	 * @param commonNavigator
	 *            the {@link CommonNavigator} to which the filter belongs
	 * @param e
	 *            the SWT event
	 */
	private void handleKeyDownEvent(CommonNavigator commonNavigator, Event e) {
		if (e.keyCode == SWT.ESC) {
			deactivateFilter(commonNavigator.getCommonViewer());
		}
	}

	/**
	 * Find and return the filterComposite for a given {@link CommonViewer}.
	 * This method should be refactored after 4.7 (Oxygen) is released and a
	 * getter in {@link CommonNavigator} should be used instead of navigating
	 * the SWT containment hierarchy.
	 *
	 * @param viewer
	 *            the {@link CommonNavigator}
	 * @return the filterComposite for the {@link CommonNavigator}, or
	 *         <code>null</code> if no filterComposite could be found.
	 */
	private Composite getFilterComposite(CommonViewer viewer) {
		// retrieve the CommonNavigator.contentComposite
		Composite contentComposite = viewer.getControl().getParent().getParent();
		// the first child should be the filterComposite
		Control child = contentComposite.getChildren()[0];
		if (child instanceof Composite && child.getData() == FILTER_COMPOSITE) {
			return (Composite) child;
		}
		return null;
	}

	/**
	 * Show the filterComposite for the given {@link CommonViewer} if it is not
	 * shown. Otherwise hide and deactivate the filterComposite.
	 *
	 * @param commonViewer
	 *            the {@link CommonViewer} to which the filter belongs
	 */
	public void toggleFilter(CommonViewer commonViewer) {
		Composite filterComposite = getFilterComposite(commonViewer);
		if (filterComposite != null) {
			if (!filterComposite.isVisible()) {
				activateFilter(commonViewer);
			} else {
				deactivateFilter(commonViewer);
			}
		}
	}

	/**
	 * Show the filterComposite for the given {@link CommonViewer} and focus the
	 * search filter text field.
	 *
	 * @param viewer
	 *            the {@link CommonViewer} to which the filter belongs
	 */
	public void activateFilter(CommonViewer viewer) {
		Composite filterComposite = getFilterComposite(viewer);
		if (filterComposite != null) {
			filterComposite.setVisible(true);
			GridData layoutData = (GridData) filterComposite.getLayoutData();
			layoutData.heightHint = SWT.DEFAULT;
			filterComposite.getParent().layout(true);

			Text filterTextField = (Text) filterComposite.getChildren()[0];
			filterTextField.setFocus();
		}
		setToolbarActionChecked(viewer, true);
	}

	/**
	 * Hide the filterComposite for the given {@link CommonViewer} and remove an
	 * active {@link CommonNavigatorSearchFilter} if one is attached to the
	 * {@link CommonViewer}.
	 *
	 * @param viewer
	 *            the {@link CommonViewer} to which the filter belongs
	 */
	public void deactivateFilter(CommonViewer viewer) {
		Composite filterComposite = getFilterComposite(viewer);
		if (filterComposite != null) {
			filterComposite.setVisible(false);
			GridData layoutData = (GridData) filterComposite.getLayoutData();
			layoutData.heightHint = 0;
			filterComposite.getParent().layout(true);
		}

		// remove the search filter from the viewer and trigger a refresh so the
		// viewer applies the new filter set without our search filter.
		removeSearchFilterIfExist(viewer);
		viewer.refresh();
		setToolbarActionChecked(viewer, false);
	}

	protected void setToolbarActionChecked(CommonViewer viewer, boolean checked) {
		IToolBarManager toolbarManager = viewer.getCommonNavigator().getViewSite().getActionBars().getToolBarManager();
		IContributionItem item = toolbarManager.find(ActionFactory.FIND.getId());
		if (item instanceof ActionContributionItem) {
			((ActionContributionItem) item).getAction().setChecked(checked);
		}
	}

	/**
	 * Updates the {@link CommonNavigatorSearchFilter} the filter reflects the
	 * given search string.
	 *
	 * Also lazily adds the filter to the {@link CommonViewer} if it is not yet
	 * present.
	 *
	 * @param viewer
	 *            the {@link CommonViewer} which shall be filtered
	 * @param searchString
	 *            the search string to be used for filtering.
	 */
	private void filterNavigatorTree(CommonNavigator commonNavigator, String searchString) {
		CommonNavigatorSearchFilter searchFilter = getOrAddSearchFilter(commonNavigator.getCommonViewer());
		searchFilter.setSearchString(searchString);
	}

	/**
	 * Retrieve a previously added {@link CommonNavigatorSearchFilter} from the
	 * {@link CommonViewer}. If no {@link CommonNavigatorSearchFilter} was added
	 * previously, it is created and added now and returned.
	 *
	 * @param viewer
	 *            the {@link CommonViewer} from which to retrieve and possibly
	 *            add the search filter
	 * @return the (previously added or just created) search filter
	 */
	private CommonNavigatorSearchFilter getOrAddSearchFilter(CommonViewer viewer) {
		ViewerFilter[] filters = viewer.getFilters();
		for (ViewerFilter filter : filters) {
			if (filter instanceof CommonNavigatorSearchFilter) {
				return (CommonNavigatorSearchFilter) filter;
			}
		}

		// if there is no filter, we create one and add it to the viewer
		CommonNavigatorSearchFilter newFilter = new CommonNavigatorSearchFilter(viewer);
		ViewerFilter[] newFilters = new ViewerFilter[filters.length + 1];
		System.arraycopy(filters, 0, newFilters, 0, filters.length);
		newFilters[filters.length] = newFilter;
		viewer.setFilters(newFilters);
		return newFilter;
	}

	/**
	 * Removes the {@link CommonNavigatorSearchFilter} from the
	 * {@link CommonViewer} so that the tree is no longer filtered.
	 *
	 * It is allowed to call this method with a {@link CommonViewer} without an
	 * associated {@link CommonNavigatorSearchFilter}. In this case, this method
	 * does nothing.
	 *
	 * @param commonViewer
	 *            the {@link CommonViewer} from which to remove the
	 *            {@link CommonNavigatorSearchFilter}.
	 */
	private void removeSearchFilterIfExist(CommonViewer commonViewer) {
		ViewerFilter[] filters = commonViewer.getFilters();
		for (int i = 0; i < filters.length; ++i) {
			if (filters[i] instanceof CommonNavigatorSearchFilter) {
				CommonNavigatorSearchFilter filterToRemove = (CommonNavigatorSearchFilter) filters[i];
				filterToRemove.dispose();
				ViewerFilter[] newFilters = new ViewerFilter[filters.length - 1];
				System.arraycopy(filters, 0, newFilters, 0, i);
				System.arraycopy(filters, i + 1, newFilters, i, filters.length - i - 1);
				commonViewer.setFilters(newFilters);
				return;
			}
		}
	}

	/**
	 * Show or hide the "busy" image in the filterComposite.
	 *
	 * @param commonViewer
	 *            the {@link CommonViewer} for which to show/hide the busy
	 *            image.
	 * @param visible
	 *            <code>true</code> if the image shall be shown,
	 *            <code>false</code> if it shall be hidden.
	 */
	public void setBusyImageVisible(CommonViewer commonViewer, boolean visible) {
		Composite filterComposite = getFilterComposite(commonViewer);
		if (filterComposite != null) {
			Control label = filterComposite.getChildren()[1];
			label.setVisible(visible);
		}
	}
}
