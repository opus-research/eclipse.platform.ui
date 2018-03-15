/*******************************************************************************
 * Copyright (c) 2017 Conrad Groth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Conrad Groth - initial implementation for Bug 519771
 *******************************************************************************/
package org.eclipse.ui.navigator.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;

/**
 * This filter selects only working sets that contain open projects.
 *
 * @since 3.6.100
 */
public class OpenProjectsWorkingSetFilter extends ViewerFilter {

    @Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IWorkingSet resource = Adapters.adapt(element, IWorkingSet.class);
		if (resource == null) {
			return true;
		}
		for (IAdaptable subElement : resource.getElements()) {
			IProject project = Adapters.adapt(subElement, IProject.class);
			if (project != null && project.isOpen()) {
				return true;
			}
		}
		return false;
    }
}
