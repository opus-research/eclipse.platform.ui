/*******************************************************************************
 * Copyright (c) 2017 Stefan Winkler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Stefan Winkler - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;

public class SimpleTestLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getName();
		}
		if (element instanceof IResource) {
			return ((IResource) element).getName();
		}
		return null;
	}

}
