/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

/**
 * A handler which adds a custom paint listener which replaces the original tree
 * arrows and draws new ones (based on the state of the TreeItem).
 */
public class CSSPropertyTreeArrowsSWTHandler extends AbstractCSSPropertySWTHandler {

	/**
	 * We'll use the same instance to paint all the TreeItems.
	 */
	private static final Listener paintListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			Widget item = event.item;
			if (!(item instanceof TreeItem)) {
				return;
			}

			TreeItem treeItem = (TreeItem) item;
			if (treeItem.getItemCount() == 0) {
				return;
			}

			Tree parent = treeItem.getParent();
			Color foreground = parent.getForeground();
			Color background = parent.getBackground();
			if (foreground == null || background == null) {
				// If there was no change to the foreground/background, bail
				// out.
				return;
			}

			int w = 9;
			int h = 9;
			int x = event.x - 16;
			int y = event.y + 4;
			GC gc = event.gc;

			gc.setForeground(foreground);
			gc.setBackground(background);

			// Erase the arrow
			gc.fillRectangle(x, y, w + 1, h + 2);
			int halfH = h / 2;
			gc.drawRectangle(x + 1, y + 1, w - 1, h - 1);

			gc.drawLine(x + 3, y + halfH + 1, x + w - 2, y + halfH + 1);
			if (!treeItem.getExpanded()) {
				int halfW = w / 2;
				gc.drawLine(x + halfW + 1, y + 3, x + halfW + 1, y + h - 2);
			}
			event.detail &= ~SWT.BACKGROUND;

		}
	};

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		boolean apply = (Boolean) engine.convert(value, Boolean.class, null);
		if (control instanceof Tree) {
			Tree tree = (Tree) control;
			if (apply) {
				// Make sure we don't add the listener twice.
				tree.removeListener(SWT.PaintItem, paintListener);
				tree.addListener(SWT.PaintItem, paintListener);

			} else {
				tree.removeListener(SWT.PaintItem, paintListener);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
