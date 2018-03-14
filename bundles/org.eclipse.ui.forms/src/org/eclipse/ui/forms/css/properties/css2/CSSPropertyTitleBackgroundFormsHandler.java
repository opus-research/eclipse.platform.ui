/*******************************************************************************
 * Copyright (c) 2014 Lars Vogel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *******************************************************************************/
package org.eclipse.ui.forms.css.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.css.dom.SectionElement;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTitleBackgroundFormsHandler implements
		ICSSPropertyHandler {

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		SectionElement sectionElement = (SectionElement) element;
		Section section = (Section) sectionElement.getNativeWidget();
		section.setTitleBarBackground(CSSSWTColorHelper.getSWTColor(value,
				Display.getCurrent()));
		return false;
	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
