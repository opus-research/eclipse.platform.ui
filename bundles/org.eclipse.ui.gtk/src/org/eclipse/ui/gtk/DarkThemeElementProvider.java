/*******************************************************************************
 * Copyright (c) 2015 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela <scela@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.gtk;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class DarkThemeElementProvider implements IElementProvider {

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof Shell) {
			return new DarkThemeElement((Shell) element, engine);
		}
		return null;
	}

}
