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
 * Relevance for the marker
 * <p>
 * This interface gives the relevance of the marker which can be used to sort
 * all the markers.
 * </p>
 *
 * @since 3.14
 */
public interface IMarkerRelevance {

	/**
	 * Returns the relevance for marker to be used in sorting. By default, the
	 * relevance is 0.
	 *
	 * @return the relevance for the marker
	 */
	default public int getRelevanceForMarker() {
		return 0;
	}

}
