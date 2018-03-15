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

/**
 * Specialized {@link IEditorPart} for editors who do not want to be part of the
 * auto-save of editors mechanism. An {@link IEditorPart} that is not an
 * IAutoSaveableEditorPart is part of the auto-save mechanism. Remember that
 * this policy is only taken into account when the auto-save mechanism is
 * enabled in Eclipse preferences.
 *
 * @since 3.108
 *
 */
public interface IAutoSaveableEditorPart extends IEditorPart {

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
	public boolean getAutoSavePolicy();

}
