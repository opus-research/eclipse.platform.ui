/*******************************************************************************
 * Copyright (c) 2016 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Richard <axel.richard@obeo.fr> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import java.util.Date;

/**
 * Specialized {@link ISaveablePart} for editors who (do not) want to be part of
 * the auto-save of editors mechanism. An editor that is not an ISaveablePart3
 * doesn't belong to the auto-save mechanism. Remember that this policy is only
 * taken into account when the auto-save mechanism is enabled in Eclipse
 * preferences.
 *
 * @since 3.108
 *
 */
public interface ISaveablePart3 extends ISaveablePart {

	/**
	 * The property id for <code>getLastModified</code>.
	 */
	public static final int PROP_LAST_MODIFIED = IWorkbenchPartConstants.PROP_LAST_MODIFIED;

	/**
	 * Get the auto-save policy for this type of editor part. The default policy
	 * is to allow auto-save. This method must return <code>true</code> or
	 * <code>false</code>.
	 *
	 * @return <code>true</code> if auto-save is allowed for this type of part,
	 *         <code>false</code> otherwise
	 *
	 * @since 3.108
	 */
	default public boolean getAutoSavePolicy() {
		return true;
	}

	/**
	 * Returns whether the contents of this part have changed. If this value
	 * changes the part must fire a property listener event with
	 * <code>PROP_LAST_MODIFIED</code>.
	 * <p>
	 *
	 * @return the last modification date of this part.
	 *
	 * @since 3.108
	 */
	default public Date getLastModified() {
		return null;
	}
}
