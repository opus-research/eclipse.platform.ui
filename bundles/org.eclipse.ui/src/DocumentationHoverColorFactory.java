import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

public class DocumentationHoverColorFactory implements IColorFactory {

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

}
