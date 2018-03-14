/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 226046 Add filter for user-spec'd patterns
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.navigator.AdaptabilityUtility;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.filters.UserFilter;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.4.700
 * This filter will read with {@link CommonViewer#getData(String)} the value of {@link NavigatorPlugin#RESOURCE_REGEXP_FILTER_DATA}
 * and evaluate whether one of the filters enabled on this current viewer hides resources. 
 */
public class ResourceNameRegexpFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource = (IResource) AdaptabilityUtility.getAdapter(element, IResource.class);
		if (resource != null && viewer.getData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA) != null) {
			List<UserFilter> filters = (List<UserFilter>)viewer.getData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA);
			for (UserFilter filter : filters) {
				if (filter.isEnabled()) {
					if (Pattern.matches(filter.getRegexp(), resource.getName())) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
