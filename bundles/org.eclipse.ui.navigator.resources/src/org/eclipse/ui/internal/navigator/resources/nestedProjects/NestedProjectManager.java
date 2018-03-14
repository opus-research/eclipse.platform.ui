/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nestedProjects;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

/**
 * @since 3.3
 *
 */
public class NestedProjectManager {

	private static Set<IFolder> shownAsProject;

	public static void registerProjectShownInFolder(IFolder folder, IProject project) {
		if (shownAsProject == null) {
			shownAsProject = new HashSet<IFolder>();
		}
		shownAsProject.add(folder);
	}
	
	public static void unregisterProjectShownInFolder(IFolder targetFolder) {
		if (shownAsProject != null) {
			shownAsProject.remove(targetFolder);
		}
	}


	public static boolean isShownAsProject(IFolder folder) {
		return shownAsProject != null && shownAsProject.contains(folder);
	}


}
