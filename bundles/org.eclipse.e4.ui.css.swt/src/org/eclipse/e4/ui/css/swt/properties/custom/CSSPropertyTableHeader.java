/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.w3c.dom.css.CSSValue;

/**
 * A handler which will can control the table header visibility.
 */
public class CSSPropertyTableHeader extends AbstractCSSPropertySWTHandler {

	private static final String SWT_TABLE_HEADER_VISIBLE = "swt-table-header-visible"; //$NON-NLS-1$


	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		boolean isTableHeaderVisible = (Boolean) engine.convert(value, Boolean.class, null);
		if (control instanceof Table) {
			Table folder = (Table) control;
			folder.setHeaderVisible(isTableHeaderVisible);
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
