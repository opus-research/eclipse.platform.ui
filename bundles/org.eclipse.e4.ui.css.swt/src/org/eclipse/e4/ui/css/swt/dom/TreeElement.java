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
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public class TreeElement extends ControlElement {

	private final static String TREE_ARROWS_FOREGROUND_COLOR = "org.eclipse.e4.ui.css.swt.treeArrowsForegroundColor"; //$NON-NLS-1$

	/**
	 * We'll use the same instance to paint all the TreeItems.
	 */
	private static final Listener treeItemPaintListener = new Listener() {

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
			Object data = parent.getData(TREE_ARROWS_FOREGROUND_COLOR);
			if (!(data instanceof Color)) {
				return;
			}
			Color foreground = (Color) data;
			Color background = parent.getBackground();

			// Many windows-only magic numbers here to erase the previous
			// arrow drawing and create our own... future work could be
			// adding more styles (the code below creates a square with
			// + or - depending on whether the item is collapsed or expanded).
			int w = 9;
			int h = 9;
			int x = event.x - 16;
			int y = event.y + 4;
			GC gc = event.gc;

			gc.setForeground(foreground);
			if (background != null) {
				gc.setBackground(background);
			}

			// Erase the arrow we had previously.
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

	public TreeElement(Tree composite, CSSEngine engine) {
		super(composite, engine);
	}

	public Tree getTree() {
		return (Tree) getNativeWidget();
	}

	@Override
	public void reset() {
		setTreeArrowsForegroundColor(null);
		super.reset();
	}

	/**
	 * Adds a custom paint listener which replaces the original tree arrows and
	 * draws new ones (based on the state of the TreeItem) if a color is passed
	 * (if null is passed, returns to the standard behavior).
	 *
	 * Note: the current implementation is windows-only (done for Bug 434201)
	 *
	 * @param color
	 *            The foreground color to be used to paint the items. May be
	 *            null (in which case we stop our custom painter from painting
	 *            the tree items).
	 */
	public void setTreeArrowsForegroundColor(Color color) {
		Tree tree = getTree();
		tree.setData(TREE_ARROWS_FOREGROUND_COLOR, color);

		if (Constants.OS_WIN32.equals(Platform.getOS())) {
			// This code is currently windows-only (the custom painter would
			// need to be tweaked for other platforms).

			// Make sure we don't add the listener twice.
			tree.removeListener(SWT.PaintItem, treeItemPaintListener);
			if (color != null) {
				tree.addListener(SWT.PaintItem, treeItemPaintListener);
			}
		}
	}

	/**
	 * @return the color to be used to draw the tree arrows foreground.
	 */
	public Color getTreeArrowsForegroundColor() {
		Tree tree = getTree();
		Object data = tree.getData(TREE_ARROWS_FOREGROUND_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}
}