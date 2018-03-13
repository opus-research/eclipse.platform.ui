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

package org.eclipse.e4.ui.workbench.modeling;

import org.eclipse.emf.ecore.EClass;

/**
 * A service which is able to map normal java {@link Class} objects to {@link EClass}.
 * 
 * @since 1.1
 */
public interface EClassProvider {
	/**
	 * Maps the given java {@link Class} to the corresponding {@link EClass}.
	 * 
	 * @param clazz the java {@link Class} which should be an EMF {@link EClass}
	 * @return the {@link EClass} object associated with the given {@link Class} or null if none can be found
	 */
	public EClass getEClass(Class<?> clazz);
}
