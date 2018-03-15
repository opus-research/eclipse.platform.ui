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
package org.eclipse.ui.internal.navigator.filters.incremental;

import java.util.Arrays;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Status Line contribution providing a text field to enter a filter search
 * expression which incrementally filters the CommonNavigator for which this
 * Contribution Item was created.
 *
 * The search expression may contain wildcards (*) to match arbitrary text in
 * the middle of a string. Wildcards are only needed in the middle of an
 * expression, as wildcards are implicitly assumed at the beginning and end of
 * the expression.
 *
 * <em>Example:</em> <code>f*r</code> matches
 * <code>fur, foobar, infer, inferring, fork</code>
 *
 * To activate the contribution, use {@link #addOrActivate(CommonNavigator)}. To
 * deactivate it, use {@link #remove(IStatusLineManager)}
 *
 * @noinstantiate This class is not intended to be instantiated by clients;
 *                clients should use {@link #addOrActivate(CommonNavigator)}
 *                instead.
 *
 * @author Stefan Winkler <stefan@winklerweb.net>
 * @since 3.3
 */
public class IncrementalFilterContribution extends ContributionItem implements KeyListener {

	/** The ID of this contribution */
	public static String ID = "org.eclipse.ui.internal.navigator.IncrementalFilterContribution"; //$NON-NLS-1$

	/**
	 * Convenience method to set the focus of the text field, creating the
	 * contribution if it does not yet exist.
	 *
	 * When this method is called for a given {@link CommonNavigator}, it checks
	 * if the status line for this {@link CommonNavigator} already contains an
	 * {@link IncrementalFilterContribution}. If this is not the case, a new
	 * instance of {@link IncrementalFilterContribution} is created and added to
	 * the status line.
	 *
	 * The text field (whether existing or newly created) is given the focus.
	 *
	 * @param navigator
	 *            the {@link CommonNavigator} for which to create the
	 *            {@link IncrementalFilterContribution}
	 */
	public static void addOrActivate(CommonNavigator navigator) {
		IViewSite viewSite = navigator.getViewSite();
		IStatusLineManager statusLineManager = viewSite.getActionBars().getStatusLineManager();
		IContributionItem filterContribution = statusLineManager.find(ID);

		if (filterContribution instanceof IncrementalFilterContribution) {
			IncrementalFilterContribution existingContribution = (IncrementalFilterContribution) filterContribution;
			existingContribution.grabFocus();
		} else {
			IncrementalFilterContribution contribution = new IncrementalFilterContribution(navigator);

			statusLineManager.appendToGroup(StatusLineManager.BEGIN_GROUP, contribution);

			statusLineManager.update(false);
			contribution.grabFocus();
		}

	}

	/**
	 * Remove the {@link IncrementalFilterContribution} from the given
	 * {@link IStatusLineManager}, if it exists. Otherwise do nothing.
	 *
	 * @param manager
	 *            The {@link IStatusLineManager} from which to remove the
	 *            contribution.
	 */
	public static void remove(IStatusLineManager manager) {
		IContributionItem item = manager.find(ID);
		if (item instanceof IncrementalFilterContribution) {
			((IncrementalFilterContribution) item).removeFromStatusBar();
		}
	}

	/**
	 * The {@link CommonNavigator} for which this
	 * {@link IncrementalFilterContribution} is responsible.
	 */
	private final CommonNavigator commonNavigator;

	/**
	 * The text field which is added to the status line.
	 */
	private Text searchStringText = null;

	/**
	 * The {@link PatternFilter} used to filter the {@link CommonNavigator}'s
	 * tree.
	 */
	private PatternFilter patternFilter;

	/**
	 * Constructor.
	 *
	 * Creates a new instance of this {@link IncrementalFilterContribution} and
	 * initializes the ID and patternFilter.
	 *
	 * <em>Note:</em> Usually, {@link #addOrActivate(CommonNavigator)} should be
	 * used instead of this method to directly add the contribution to the
	 * status line.
	 *
	 * @param commonNavigator
	 *            The {@link CommonNavigator} whose tree shall be filtered.
	 */
	public IncrementalFilterContribution(CommonNavigator commonNavigator) {
		this.commonNavigator = commonNavigator;
		setId(ID);

		this.patternFilter = new PatternFilter();
		this.patternFilter.setIncludeLeadingWildcard(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.
	 * Composite)
	 */
	@Override
	public void fill(Composite parent) {
		searchStringText = new Text(parent, SWT.BORDER);
		StatusLineLayoutData layoutData = new StatusLineLayoutData();
		layoutData.widthHint = 240;
		searchStringText.setLayoutData(layoutData);
		searchStringText.addModifyListener(e -> filterNavigatorTree(searchStringText.getText()));
		searchStringText.addKeyListener(this);
	}

	/**
	 * Updates the {@link PatternFilter} and the {@link CommonViewer} so that
	 * the filter reflects the given search string.
	 *
	 * @param searchString
	 *            the search string to be used for filtering.
	 */
	protected void filterNavigatorTree(String searchString) {
		if (searchString.isEmpty()) {
			removeViewerFilterIfExist();
		} else {
			patternFilter.setPattern(searchString);
			updateViewerFilter();
		}
	}

	/**
	 * Removes the {@link PatternFilter} from the {@link CommonNavigator} so
	 * that the tree is no longer filtered.
	 */
	protected void removeViewerFilterIfExist() {
		if (patternFilter == null) {
			return;
		}

		CommonViewer commonViewer = commonNavigator.getCommonViewer();
		ViewerFilter[] filters = commonViewer.getFilters();
		for (int i = 0; i < filters.length; ++i) {
			if (patternFilter == filters[i]) {
				ViewerFilter[] newFilters = new ViewerFilter[filters.length - 1];
				System.arraycopy(filters, 0, newFilters, 0, i);
				System.arraycopy(filters, i + 1, newFilters, i, filters.length - i - 1);
				commonViewer.setFilters(newFilters);
				return;
			}
		}
	}

	/**
	 * Refreshes the {@link CommonViewer} so that the {@link PatternFilter} is
	 * applied or updated. If the {@link PatternFilter} is currently not
	 * registered for the {@link CommonViewer}, it is added along the way.
	 */
	protected void updateViewerFilter() {
		CommonViewer commonViewer = commonNavigator.getCommonViewer();
		ViewerFilter[] filters = commonViewer.getFilters();
		if (Arrays.asList(filters).contains(patternFilter)) {
			// trigger refiltering
			commonViewer.refresh();
		} else {
			// adding the filter will automatically retrigger filtering
			addViewerFilter();
		}

		// expand all so we can see the search results
		commonViewer.expandAll();
	}

	/**
	 * Adds the {@link PatternFilter} to the {@link CommonViewer}
	 */
	protected void addViewerFilter() {
		CommonViewer commonViewer = commonNavigator.getCommonViewer();
		ViewerFilter[] filters = commonViewer.getFilters();
		ViewerFilter[] newFilters = new ViewerFilter[filters.length + 1];
		System.arraycopy(filters, 0, newFilters, 0, filters.length);
		newFilters[filters.length] = patternFilter;
		commonViewer.setFilters(newFilters);
	}

	/**
	 * Sets the focus to the contributed text field. If the contribution is not
	 * active (i.e., if the text field is null or disposed) the method does
	 * nothing.
	 */
	public void grabFocus() {
		if (searchStringText != null && !searchStringText.isDisposed()) {
			searchStringText.forceFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
	 * KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.ESC) {
			removeFromStatusBar();
		}
	}

	/**
	 * Remove this contribution from the parent {@link StatusLineManager} and
	 * remove the {@link PatternFilter} from the {@link CommonViewer}.
	 */
	private void removeFromStatusBar() {
		removeViewerFilterIfExist();

		IContributionManager manager = getParent();
		manager.remove(this);
		manager.update(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.
	 * KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {
	}
}
