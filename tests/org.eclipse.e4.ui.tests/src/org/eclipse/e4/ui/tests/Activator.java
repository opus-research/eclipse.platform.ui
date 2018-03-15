/*******************************************************************************
 * Copyright (c) 2013, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests;

import org.osgi.framework.FrameworkUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator {

	/**
	 * Generate a platform URI referencing the provided class.
	 *
	 * @param clazz
	 *            the class to be referenced
	 * @return the platform-based URI: bundleclass://X/X.Y
	 */
	public static String asURI(Class<?> clazz) {
		return "bundleclass://" + FrameworkUtil.getBundle(clazz).getSymbolicName()
				+ '/' + clazz.getName();
	}

}
