package org.eclipse.ui.internal.views.properties.tabbed.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;
import org.w3c.dom.css.CSSValue;

public class TabbedPropertyElementCssPropertyHandler extends AbstractCSSPropertySWTHandler
		implements ICSSPropertyHandler {

	private static final String BACKGROUND_COLOR = "background-color"; //$NON-NLS-1$

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (control instanceof TabbedPropertyTitle) {
			TabbedPropertyTitle title = (TabbedPropertyTitle) control;
			if (BACKGROUND_COLOR.equalsIgnoreCase(property)
					&& (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				RGBA rgba = CSSSWTColorHelper.getRGBA(value);
				title.setBackgroundColorInFacatory(rgba);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
