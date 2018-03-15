package org.eclipse.ui.internal.forms.css.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.ui.forms.widgets.ToggleHyperlink;

public class ToggleHyperlinkElement extends CompositeElement {

	public ToggleHyperlinkElement(ToggleHyperlink composite, CSSEngine engine) {
		super(composite, engine);
	}

	@Override
	public void reset() {
		super.reset();
		getToggleHyperlinkElement().setHoverDecorationColor(null);
	}

	private ToggleHyperlink getToggleHyperlinkElement() {
		return (ToggleHyperlink) getNativeWidget();
	}

}
