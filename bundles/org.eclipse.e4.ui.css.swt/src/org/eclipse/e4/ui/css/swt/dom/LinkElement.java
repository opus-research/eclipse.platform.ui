/*******************************************************************************
 * Copyright (c) 2016 Conrad Groth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Conrad Groth <info@conrad-groth.de> - initial implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.widgets.Link;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Link}.
 *
 */
public class LinkElement extends ControlElement {

	public LinkElement(Link link, CSSEngine engine) {
		super(link, engine);
	}

	@Override
	public boolean isPseudoInstanceOf(String s) {
		if ("link".equals(s)) {
			return true;
		}
		return super.isPseudoInstanceOf(s);
	}

	@Override
	protected void computeStaticPseudoInstances() {
		super.computeStaticPseudoInstances();
		super.addStaticPseudoInstance("link");
	}
}
