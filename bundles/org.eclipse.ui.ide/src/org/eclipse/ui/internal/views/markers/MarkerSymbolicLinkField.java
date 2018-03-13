/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lidia Gutu (Windriver) - Bug 415241 The new column for tasks view to display the difference between symbolic links and source files
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.io.File;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * @since 3.4
 *
 */
public class MarkerSymbolicLinkField extends MarkerField {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.MarkerField#getValue(org.eclipse.ui.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
	
		try {

			IMarker marker = item.getMarker();
			IPath path = marker.getResource().getLocation();
			File file = new File (path.toString());

			String absolutePath = file.getAbsolutePath();
			String canonicalPath = file.getCanonicalPath();
			
			if (absolutePath.equals(canonicalPath)){
				return "Source file"; //$NON-NLS-1$
			}
			return "Symbolic link"; //$NON-NLS-1$
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

}
