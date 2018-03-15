package org.eclipse.ui.internal.views.properties.tabbed.css;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;
import org.w3c.dom.Element;

public class TabbedPropertyElementProvider implements IElementProvider {

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof TabbedPropertyTitle) {
			return new TabbedPropertyTitleAdapter((TabbedPropertyTitle) element, engine);
		}
		return null;
	}

}
