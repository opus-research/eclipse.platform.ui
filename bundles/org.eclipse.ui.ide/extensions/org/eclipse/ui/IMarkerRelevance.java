/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Relevance for the marker in Quick Fix Page
 * <p>
 * This interface gives the relevance of the marker which is used to sort all
 * markers in Quick Fix Page
 * </p>
 * 
 * @since 3.14
 *
 *
 */
public interface IMarkerRelevance {

	/**
	 * Returns the relevance for marker to be used in sorting By default, the
	 * relevance is 0 . If no relevance is provided or for resolutions with same
	 * relevance, string compare of label will be done for sorting.
	 *
	 * @return the relevance for the marker
	 */
	default public int getRelevanceForMarker() {
		return 0;
	}

}
