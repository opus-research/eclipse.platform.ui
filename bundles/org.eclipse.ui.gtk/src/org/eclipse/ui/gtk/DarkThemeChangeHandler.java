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

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.internal.gtk.OS;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class DarkThemeChangeHandler implements ICSSPropertyHandler {

	private static final String IS_DARK_PROPERTY = "is-dark";

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		if (!IS_DARK_PROPERTY.equalsIgnoreCase(property)) {
			return true;
		}

		boolean isDark = false;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			isDark = (boolean) engine.convert(value, Boolean.class, null);
			OS.gdk_flush();
			OS.g_object_set(OS.gtk_settings_get_default(), "gtk-application-prefer-dark-theme".getBytes(), isDark, //$NON-NLS-1$
					0);
			OS.g_object_notify(OS.gtk_settings_get_default(), "gtk-application-prefer-dark-theme".getBytes());
		}
		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property, String pseudo, CSSEngine engine)
			throws Exception {

		return null;
	}

}
