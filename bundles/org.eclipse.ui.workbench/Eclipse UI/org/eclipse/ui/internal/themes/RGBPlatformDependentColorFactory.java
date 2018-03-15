/*******************************************************************************
 * Copyright (c) 2016 Red Hat inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat inc - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.themes;

import java.util.Hashtable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

/**
 * Starting with ~Gnome 3.06, COLOR_INFO_BACKGROUND and COLOR_INFO_FOREGROUND
 * are inverted, often producing windows with black background with white text
 * on an otherwise white background and black text. However, on Windows/Cocoa
 * COLOR_INFO_* looks ok. Solution is to generate a different color based on
 * platform. Note, colors for dark themes are overridden in
 * /org.eclipse.ui.themes/css/dark/e4-dark_preferencestyle.css. See Bug 501742
 *
 * @since 3.110
 */
public class RGBPlatformDependentColorFactory implements IColorFactory, IExecutableExtension {
	String color;

	@Override
	public RGB createColor() {
		RGB rgb = null;
		if (Util.isGtk()) {
			if ("COLOR_INFO_FOREGROUND".equals(color)) { //$NON-NLS-1$
				rgb = ColorUtil.getColorValue("COLOR_LIST_FOREGROUND"); //$NON-NLS-1$
			} else if ("COLOR_INFO_BACKGROUND".equals(color)) { //$NON-NLS-1$
				rgb = ColorUtil.getColorValue("COLOR_LIST_BACKGROUND"); //$NON-NLS-1$
			}
		} else {
			rgb = ColorUtil.getColorValue(color);
		}

		if (rgb == null) {
			// IColorFactory must return a valid color.
			return new RGB(0, 0, 0); // Black
		}
		return rgb;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof Hashtable<?, ?>) {
			Hashtable<?, ?> map = (Hashtable<?, ?>) data;
			color = (String) map.get("color"); //$NON-NLS-1$
		}
	}
}
