package org.eclipse.ui.internal.forms.css.dom;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.forms.widgets.ToggleHyperlink;
import org.w3c.dom.Element;

public class ToggleHyperlinkElementProvider implements IElementProvider {

	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof ToggleHyperlink) {
			return new ToggleHyperlinkElement((ToggleHyperlink) element, engine);
		}
		return null;
	}

}
