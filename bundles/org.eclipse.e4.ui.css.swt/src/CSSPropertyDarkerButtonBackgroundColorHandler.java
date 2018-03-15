import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyDarkerButtonBackgroundColorHandler implements ICSSPropertyHandler {

	public CSSPropertyDarkerButtonBackgroundColorHandler() {
	}

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		final Widget widget = SWTElementHelpers.getWidget(element);
		final Display display = widget.getDisplay();
		final Color color = (Color) engine.convert(value, Color.class, display);
		if (widget instanceof Button) {
			final Button b = (Button) widget;
			if ((b.getStyle() & SWT.PUSH) != 0) {

				b.addPaintListener(new PaintListener() {
					@Override
					public void paintControl(PaintEvent e) {
						b.setBackground(color);
						e.gc.setBackground(color);
						e.gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
						e.gc.drawRoundRectangle(8, 8, b.getBounds().width - 8, b.getBounds().height - 8, 2, 2);
						// drawRoundedRectangle(int x, int y, int width, int
						// height, int arcWidth, int arcHeight);
						// e.gc.drawRoundRectangle(0, 0,
						// b.getBounds().width,
						// b.getBounds().height,
						// 2, 2);
						e.gc.drawString("Lars war hier", 5, 5);
					}
				});
				return true;
			}
		}
		return false;
	}

}
