/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

/**
 * The helper class for model tag operations
 */
public class TagsUtil {
	/**
	 * @param oldValue
	 * @param newValue
	 * @param tagName
	 * @return true when tag is added
	 */
	public static boolean isTagAdded(Object oldValue, Object newValue, String tagName) {
		return oldValue == null && tagName.equals(newValue);
	}

	/**
	 * @param oldValue
	 * @param newValue
	 * @param tagName
	 * @return true when tag is removed
	 */
	public static boolean isTagRemoved(Object oldValue, Object newValue, String tagName) {
		return newValue == null && tagName.equals(oldValue);
	}

	/**
	 * @param oldValue
	 * @param newValue
	 * @param tagName
	 * @return true when tag is modified
	 */
	public static boolean isTagModified(Object oldValue, Object newValue, String tagName) {
		return isTagAdded(oldValue, newValue, tagName) || isTagRemoved(oldValue, newValue, tagName);
	}
}
