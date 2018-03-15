package org.eclipse.ui.internal.views.properties.tabbed.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.w3c.dom.css.CSSValue;

public class TabbedPropertyListCssPropertyHandler extends AbstractCSSPropertySWTHandler implements ICSSPropertyHandler {

	private static final String WIDGET_BACKGROUND_COLOR = "widgetBackground-color"; //$NON-NLS-1$
	private static final String WIDGET_FOREGROUND_COLOR = "widgetForeground-color"; //$NON-NLS-1$
	private static final String WIDGET_NORMAL_SHADOW_COLOR = "widgetNormalShadow-color"; //$NON-NLS-1$
	private static final String WIDGET_DARK_SHADOW_COLOR = "widgetDarkShadow-color"; //$NON-NLS-1$
	private static final String LIST_BACKGROUND_COLOR = "listBackground-color"; //$NON-NLS-1$

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (control instanceof TabbedPropertyList) {
			TabbedPropertyList list = (TabbedPropertyList) control;
			if (LIST_BACKGROUND_COLOR.equals(property)) {
				if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
					Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
					list.reInitListBackgroundColor(color);
				}
			} else if (WIDGET_BACKGROUND_COLOR.equals(property)) {
				if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
					Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
					list.reInitWidgetBackgroundColor(color);
				}
			} else if (WIDGET_FOREGROUND_COLOR.equals(property)) {
				if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
					Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
					list.reInitWidgetForegroundColor(color);
				}
			} else if (WIDGET_NORMAL_SHADOW_COLOR.equals(property)) {
				if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
					Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
					list.reInitWidgetNormalShadowColor(color);
				}
			} else if (WIDGET_DARK_SHADOW_COLOR.equals(property)) {
				if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
					Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
					list.reInitWidgetDarkShadowColor(color);
				}
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
