/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.dom.scrollbar.StyledTextThemedScrollBarAdapter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

public class StyledTextElement extends CompositeElement {

	public StyledTextElement(Composite composite, CSSEngine engine) {
		super(composite, engine);
	}

	public StyledText getStyledText() {
		return (StyledText) getControl();
	}

	private StyledTextThemedScrollBarAdapter getScrollbarAdapter() {
		return StyledTextThemedScrollBarAdapter.getScrollbarAdapter(getStyledText());
	}

	public void setScrollBarBackgroundColor(Color newColor) {
		getScrollbarAdapter().setScrollBarBackgroundColor(newColor);
	}

	public void setScrollBarForegroundColor(Color newColor) {
		getScrollbarAdapter().setScrollBarForegroundColor(newColor);
	}

	public void setScrollBarWidth(int width) {
		getScrollbarAdapter().setScrollBarWidth(width);
	}

	public void setMouseNearScrollScrollBarWidth(int width) {
		getScrollbarAdapter().setMouseNearScrollScrollBarWidth(width);
	}

	public void setVerticalScrollBarVisible(boolean visible) {
		getScrollbarAdapter().setVerticalScrollBarVisible(visible);
	}

	public void setHorizontalScrollBarVisible(boolean visible) {
		getScrollbarAdapter().setHorizontalScrollBarVisible(visible);
	}

	public void setScrollBarBorderRadius(int radius) {
		getScrollbarAdapter().setScrollBarBorderRadius(radius);
	}

	public void setScrollBarThemed(boolean themed) {
		getScrollbarAdapter().setScrollBarThemed(themed);
	}

}
