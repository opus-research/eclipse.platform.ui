/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.filters;

import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsShape;

/**
 * A section filter for the dynamic tests view.
 * 
 * @author Anthony Hunter
 */
public class DynamicTestsCircleSectionFilter extends
		DynamicTestsElementSectionFilter {

	protected boolean appliesToShape(DynamicTestsShape shape) {
		return DynamicTestsShape.CIRCLE.equals(shape);
	}
}
