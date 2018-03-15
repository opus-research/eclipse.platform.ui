package org.eclipse.ui.internal.themes;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

/**
 * Used in org.eclipse.ui.themes extension point of the org.eclipse.ui plug-in
 * for the color definition org.eclipse.ui.workbench.DOCUMENTATION_HOVER
 *
 */
public class DocumentationHoverColorFactory implements IColorFactory {

	@Override
	public RGB createColor() {
		RGB rgb = null;
		/**
		 * Starting with ~Gnome 3.06, COLOR_INFO_BACKGROUND and
		 * COLOR_INFO_FOREGROUND are inverted, often producing windows with
		 * black background with white text on an otherwise white background and
		 * black text. However, on Windows/Cocoa COLOR_INFO_* looks ok. Solution
		 * is to generate a different color based on platform.
		 *
		 */
		if (Util.isGtk()) {
			rgb = ColorUtil.getColorValue("COLOR_INFO_FOREGROUND"); //$NON-NLS-1$
		} else
				rgb = ColorUtil.getColorValue("COLOR_LIST_BACKGROUND"); //$NON-NLS-1$

		if (rgb == null) {
			// IColorFactory must return a valid color.
			return new RGB(0, 0, 0); // Black
		}
		return rgb;
	}


}
