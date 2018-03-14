/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.model.fragment.impl;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;


public class EclipseExtensionFunctions {

	public static boolean instanceOf(ExpressionContext context, String name) {
		Pointer pointer = context.getContextNodePointer();
		if (pointer == null) {
			return false;
		}
		try {
			Class<?> interfaceToTest = Class.forName(name);
			if (interfaceToTest != null) {
				Object object = pointer.getValue();
				if (object != null) {
					Class<? extends Object> objectClass = object.getClass();
					return interfaceToTest.isAssignableFrom(objectClass);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
