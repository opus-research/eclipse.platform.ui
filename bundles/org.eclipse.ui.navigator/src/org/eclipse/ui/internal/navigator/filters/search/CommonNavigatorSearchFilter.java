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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;

/**
 * Implementation of a {@link ViewerFilter} to search for a string or regular
 * expression in a {@link CommonNavigator} tree. As {@link CommonNavigator}s can
 * have very large and deep trees, this filter implementation tries to optimize
 * performance using two strategies:
 *
 * <ol>
 * <li>Limitation of visited elements by implementation class name.</li>
 * <li>Asynchronous calculation of the elements matching the filter in a
 * {@link Job}</li>
 * </ol>
 *
 * Regarding 1.), a developer using a {@link CommonNavigator} has the
 * possibility to configure the class names of elements which are considered for
 * the filtered result via the viewer descriptor property
 * "org.eclipse.ui.navigator.searchFilterAllowedClasses". The value of this
 * property can either be "*" (no class-based filtering is performed) or a
 * comma-separated list of (implementation!) class names. The default, if the
 * property is not set, is to consider only Files, Folders, Projects, and the
 * WorkspaceRoot from the org.eclipse.core.resources bundle. Note that the class
 * names need to contain the classes of all elements to consider for name
 * matching <em>and</em> their ancestors up to the root. In other words, if the
 * algorithm encounters a node which does not satisfy the check for allowed
 * classes, this node including all of its children are pruned from the search.
 *
 * Regarding 2.), in contrast to regular {@link ViewerFilter} implementations
 * which calculate the filter on demand, this implementation considers the
 * {@link CommonViewer}'s input in the moment a filter string is set. The
 * processing is done in a separate {@link Job} and populates a cache of
 * elements and their visibility state. Only after the job has finished
 * processing the tree, it asynchronously refreshes the {@link CommonViewer}. Of
 * course, the {@link Job} is cancelled and restarted if the search pattern is
 * changed or the search is aborted.
 *
 * @since 3.7
 */
public class CommonNavigatorSearchFilter extends ViewerFilter {

	/**
	 * The special string denoting that all classes are allowed to be considered
	 * for this filter
	 */
	private final static String ALL_CLASSES_ALLOWED = "*"; //$NON-NLS-1$

	/**
	 * The default allowed classes of elements considered for the filter.
	 * (Default is all classes are allowed: <code>"*"</code>.
	 */
	private final static String DEFAULT_ALLOWED_CLASSES = ALL_CLASSES_ALLOWED;

	/**
	 * The set of class names configured to be allowed for the filter to be
	 * considered. An empty set means that all classes are allowed.
	 */
	private final Set<String> allowedClasses;

	/**
	 * A cache which remembers if a concrete class is allowed. This cache
	 * increases performance, because we need to do the "is any
	 * superclass/interface of this class allowed"-check only once for each
	 * class.
	 */
	private final ConcurrentMap<Class<?>, Boolean> allowedClassesCache = new ConcurrentHashMap<>();

	/**
	 * The cache of elements to filter (precalculated by the calculation
	 * {@link Job}). The key refers to the element in the {@link CommonViewer}'s
	 * tree. The value indicates whether the element should be visible
	 * (<code>true</code>) or not (<code>false</code>).
	 */
	private final Map<Object, Boolean> elementCache = new HashMap<>();

	/**
	 * The {@link CommonViewer} to which this filter is attached.
	 */
	private final CommonViewer commonViewer;

	/**
	 * The filter precalculation {@link Job}.
	 */
	private final Job elementCachePopulationJob;

	/**
	 * The regex {@link Pattern} to use to match labels.
	 */
	private Pattern searchPattern;

	/**
	 * Create a new {@link CommonNavigatorSearchFilter} for the given
	 * {@link CommonViewer}. Note the other than regular {@link ViewerFilter}s,
	 * this instance can only be used with the {@link CommonViewer} it was
	 * initialized with.
	 *
	 * @param viewer
	 *            the {@link CommonViewer} to which this {@link ViewerFilter} is
	 *            attached
	 */
	public CommonNavigatorSearchFilter(CommonViewer viewer) {
		this.commonViewer = viewer;
		this.elementCachePopulationJob = createJob();
		this.allowedClasses = getAllowedClassesForViewer(viewer);
	}

	/**
	 * Create the {@link Job} which precalculates the filtered elements. The
	 * {@link Job} can be scheduled and reused every time the search pattern
	 * changes.
	 *
	 * The returned {@link Job} is configured, but not yet scheduled to run.
	 * This is done when the search string is set in
	 * {@link #setSearchString(String)}
	 *
	 * @return the precalculateion {@link Job}
	 */
	private Job createJob() {
		Job job = Job.create("Calculate Element Filter", monitor -> { //$NON-NLS-1$
			CommonNavigatorSearchFilterHelper helper = CommonNavigatorSearchFilterHelper.getInstance();
			try {
				// the SubMonitor's primary function here is to make the Job
				// responsive to cancel requests
				SubMonitor subMonitor = SubMonitor.convert(monitor);

				// show the indicator icon so the user knows that the job is
				// calculating the filter
				Display.getDefault().asyncExec(() -> helper.setBusyImageVisible(commonViewer, true));

				// traverse the tree starting with the input
				Object input = commonViewer.getInput();
				ITreeContentProvider contentProvider = (ITreeContentProvider) commonViewer.getContentProvider();
				ILabelProvider labelProvider = (ILabelProvider) commonViewer.getLabelProvider();
				Object[] rootElements = contentProvider.getElements(input);
				calculateElementFilter(rootElements, contentProvider, labelProvider, subMonitor);

				// finally sync to UI in order to apply the precalculated filter
				Display.getDefault().asyncExec(() -> {
					// trigger refiltering and expand all so we can see the
					// search results
					commonViewer.refresh();
					commonViewer.expandAll();
				});
			} finally {
				// hide the indicator icon again to signal that the job has
				// finished.
				Display.getDefault().asyncExec(() -> helper.setBusyImageVisible(commonViewer, false));
			}
		});

		job.setPriority(Job.INTERACTIVE);
		job.setUser(true);
		return job;
	}

	/**
	 * Read the configuration from the {@link CommonNavigator}'s descriptor and
	 * create a {@link Predicate} which can be used to check an element's class
	 * for consideration by the filter algorithm.
	 *
	 * @param viewer
	 *            the CommonViewer from which to retrieve the descriptor.
	 * @return the {@link Predicate}
	 */
	private Set<String> getAllowedClassesForViewer(CommonViewer viewer) {
		INavigatorViewerDescriptor descriptor = viewer.getNavigatorContentService().getViewerDescriptor();

		// TODO We are already past API Freeze for Oxygen. So the property keys
		// are hard-coded here, but should be moved to
		// INavigatorViewerDescriptor in the future.
		String allowedClassesString = descriptor
				.getStringConfigProperty("org.eclipse.ui.navigator.searchFilterAllowedClasses"); //$NON-NLS-1$
		if (allowedClassesString == null) {
			allowedClassesString = DEFAULT_ALLOWED_CLASSES;
		}
		if (allowedClassesString.equals(ALL_CLASSES_ALLOWED)) {
			// empty set has the special meaning of all classes allowed.
			return Collections.emptySet();
		}

		return new HashSet<String>(Arrays.asList(allowedClassesString.split(","))); //$NON-NLS-1$
	}

	/**
	 * Process the given elements and evaluate whether they should be filtered
	 * or not. The result is put in the {@link #elementCache}, but for
	 * simplicity it is also returned as boolean value so the parent item can
	 * directly use the result from its children to determine its own
	 * visibility.
	 *
	 * @param elements
	 *            an array of elements to process
	 * @param contentProvider
	 *            the content provider to retrieve children of an element
	 * @param labelProvider
	 *            the label provider to retrieve an element's label
	 * @param monitor
	 *            the {@link SubMonitor} which tracks progress and indicates
	 *            cancel requests
	 * @return <code>true</code> if any of the given elements was evaluated to
	 *         be visible, <code>false</code> else.
	 */
	private boolean calculateElementFilter(Object[] elements, ITreeContentProvider contentProvider,
			ILabelProvider labelProvider, SubMonitor monitor) {

		List<Boolean> result = Arrays.asList(elements).parallelStream()
				.map(element -> calculateElementFilter(element, contentProvider, labelProvider, monitor))
				.collect(Collectors.toList());
		return result.contains(Boolean.TRUE);
	}

	/**
	 * Process the given element and evaluate whether it should be filtered or
	 * not. An element is visible if either one of its children is visible or if
	 * its label matches the given search string. To increase performance,
	 * elements whose classes do not satisfy the {@link #allowedClasses}
	 * predicate are left out and are not processed themselves, and neither are
	 * their children.
	 *
	 * The result is put in the {@link #elementCache}, but for simplicity it is
	 * also returned as boolean value so the parent item can directly use the
	 * result from its children to determine its own visibility.
	 *
	 * @param element
	 *            the element to process
	 * @param contentProvider
	 *            the content provider to retrieve children of an element
	 * @param labelProvider
	 *            the label provider to retrieve an element's label
	 * @param monitor
	 *            the {@link SubMonitor} which tracks progress and indicates
	 *            cancel requests
	 * @return <code>true</code> if the given element was evaluated to be
	 *         visible, <code>false</code> else.
	 */
	private boolean calculateElementFilter(Object element, ITreeContentProvider contentProvider,
			ILabelProvider labelProvider, SubMonitor monitor) {

		monitor.checkCanceled();

		if (!isElementAllowed(element)) {
			elementCache.put(element, Boolean.FALSE);
			return false;
		}

		// first process children
		Object[] children = contentProvider.getChildren(element);
		boolean anyChildVisible = calculateElementFilter(children, contentProvider, labelProvider, monitor);

		// after all children are processed, we can rely on the cache to
		// calculate visibility on the parent
		if (anyChildVisible || isLeafVisible(element, labelProvider)) {
			elementCache.put(element, Boolean.TRUE);
			return true;
		}

		elementCache.put(element, Boolean.FALSE);
		return false;
	}

	/**
	 * Checks if the element is visible as leaf, that is, if the element's label
	 * matches the given search string.
	 *
	 * @param element
	 *            the element to check
	 * @param labelProvider
	 *            the {@link ILabelProvider} to use to retrieve the element's
	 *            label
	 * @return <code>true</code> if the element shall be visible,
	 *         <code>false</code> else.
	 */
	private boolean isLeafVisible(Object element, ILabelProvider labelProvider) {
		String label = labelProvider.getText(element);
		return searchPattern.matcher(label).find();
	}

	/**
	 * Set the search string against which the elements shall be matched.
	 * Partial matches are considered as matches, i.e., the check for the match
	 * is performed as
	 * <code>Pattern.compile(searchString).matcher(elementLabel).find()</code>
	 *
	 * The search string is regarded case-insensitive, and it is not escaped.
	 * This also means that it is also possible to specify (valid) regular
	 * expressions as search string.
	 *
	 * If the searchString is <code>null</code>, empty, or an invalid regular
	 * expression, the filter is removed; so in these cases the tree is not
	 * filtered in any way by this filter.
	 *
	 * @param searchString
	 *            the search string or regular expression to use for filtering.
	 */
	public void setSearchString(String searchString) {
		// if there is a precalculation job scheduled/running/sleeping, it shall
		// be cancelled
		if (elementCachePopulationJob.getState() != Job.NONE) {
			if (!elementCachePopulationJob.cancel()) {
				try {
					elementCachePopulationJob.join();
				} catch (InterruptedException e) {
					// if something goes wrong and we are interrupted, we return
					// here to avoid further conflicts that could come up if we
					// reschedule the job.
					return;
				}
			}
		}

		// clear the cache (we need to repopulate it for the new search string)
		elementCache.clear();

		// evaluate the search string
		if (searchString == null || searchString.isEmpty()) {
			searchPattern = null;
		} else {
			try {
				searchPattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException e) {
				searchPattern = null;
			}
		}

		// reschedule the job to populate the filter cache
		if (searchPattern != null) {
			elementCachePopulationJob.schedule();
		} else {
			// If the search pattern is set to emtpy by the user, we do not
			// need to schedule the job.
			// But we still need to refresh the viewer so that all elements
			// become visible.
			commonViewer.refresh();
		}
	}

	/**
	 * Check an element's class to know if the element and its children shall be
	 * considered for filter calculation.
	 *
	 * @param element
	 *            the element to check
	 * @return <code>true</code> if the element shall be considered for
	 *         filtering, <code>false</code> if not.
	 */
	private boolean isElementAllowed(Object element) {
		// shortcut: if all classes are allowed (== allowedClasses is empty),
		// return immediately
		if (allowedClasses.isEmpty()) {
			return true;
		}

		Class<? extends Object> elementClass = element.getClass();
		boolean allowed = allowedClassesCache.computeIfAbsent(elementClass, cls -> computeClassAllowed(cls));
		return allowed;
	}

	/**
	 * (Recursively) compute if a class is allowed or not. First the class
	 * itself is checked. If this fails, the implemented interfaces and
	 * superinterfaces are checked. If this fails also, the superclass is
	 * checked (again including interfaces, superinterfaces, and superclasses
	 * until the inheritance root is reached.)
	 *
	 * @param cls
	 *            the class to check
	 * @return <code>true</code> if the class is allowed, <code>false</code>
	 *         else.
	 */
	private boolean computeClassAllowed(Class<?> cls) {
		// check if this concrete class is allowed
		String name = cls.getCanonicalName();
		if (name != null && allowedClasses.contains(name)) {
			return true;
		}

		// check if any implemented interface is allowed
		Class<?>[] ifcs = cls.getInterfaces();
		if (ifcs != null && Arrays.asList(ifcs).stream().anyMatch(ifc -> computeClassAllowed(ifc))) {
			return true;
		}

		// check if the superclass is allowed
		// (including transitively checking its interfaces and superclass)
		Class<?> parent = cls.getSuperclass();
		if (parent != null) {
			return computeClassAllowed(parent);
		}

		// if none of the above checks succeed, the class is not allowed
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.
	 * Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		// if the viewer is not the viewer this filter instance was created for,
		// this is not supported
		Assert.isTrue(viewer == commonViewer);

		// If the search pattern is empty, do not filter at all
		if (searchPattern == null) {
			return true;
		}

		if (!elementCache.containsKey(element)) {
			// If an element is not in the cache, it might have been added to
			// the viewer. In this case, we process the element synchronously.
			// (This should not take too much time compared to evaluating the
			// whole tree).
			ITreeContentProvider contentProvider = (ITreeContentProvider) commonViewer.getContentProvider();
			ILabelProvider labelProvider = (ILabelProvider) commonViewer.getLabelProvider();
			calculateElementFilter(element, contentProvider, labelProvider, SubMonitor.convert(null));
		}

		return elementCache.get(element);
	}

	/**
	 * Dispose this filter, i.e., cancel a potentially running Job.
	 */
	public void dispose() {
		elementCachePopulationJob.cancel();
	}
}
