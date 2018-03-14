/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.css.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.Section;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Section}.
 *
 */
public class SectionElement extends CompositeElement {


	public SectionElement(Section section, CSSEngine engine) {
		super(section, engine);
	}

	public void reset() {
		super.reset();
		Section widget = (Section) getWidget();
		widget.setTitleBarBackground(null);
	}

}
