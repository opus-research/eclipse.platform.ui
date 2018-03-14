/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This segment serves as break within a paragraph. It has no data -
 * just starts a new line and resets the locator.
 */

public class BreakSegment extends ParagraphSegment {
	public boolean advanceLocator(GC gc, int wHint, Locator locator,
			Hashtable objectTable, boolean computeHeightOnly) {
		if (locator.rowHeight==0) {
			FontMetrics fm = gc.getFontMetrics();
			locator.rowHeight = fm.getHeight();
		}
		if (computeHeightOnly) locator.collectHeights();		
		locator.resetCaret();
		locator.width = locator.x;
		locator.y += locator.rowHeight;
		locator.rowHeight = 0;
		locator.leading = 0;
		return true;
	}

	public void paint(GC gc, boolean hover, Hashtable resourceTable, boolean selected, SelectionData selData, Rectangle repaintRegion) {
		//nothing to paint
	}
	public boolean contains(int x, int y) {
		return false;
	}
	public boolean intersects(Rectangle rect) {
		return false;
	}
	public void layout(GC gc, int width, Locator locator, Hashtable ResourceTable,
			boolean selected) {
		locator.resetCaret();
		if (locator.rowHeight==0) {
			FontMetrics fm = gc.getFontMetrics();
			locator.rowHeight = fm.getHeight();
		}
		locator.y += locator.rowHeight;
		locator.rowHeight = 0;
		locator.rowCounter++;		
	}

	public void computeSelection(GC gc, Hashtable resourceTable, SelectionData selData) {
		selData.markNewLine();
	}
}
