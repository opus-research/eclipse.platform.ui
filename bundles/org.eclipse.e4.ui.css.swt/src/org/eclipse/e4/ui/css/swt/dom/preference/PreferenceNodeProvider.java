/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.preference;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.preference.IPreferenceNodeOverridable;
import org.w3c.dom.Element;

public class PreferenceNodeProvider implements IElementProvider {
	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof IPreferenceNodeOverridable) {
			return new PreferenceNodeElement(
					(IPreferenceNodeOverridable) element, engine);
		}
		return null;
	}

}
