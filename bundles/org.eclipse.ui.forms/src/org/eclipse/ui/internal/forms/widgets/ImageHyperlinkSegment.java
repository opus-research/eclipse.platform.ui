/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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

import org.eclipse.swt.graphics.*;

public class ImageHyperlinkSegment extends ImageSegment implements
		IHyperlinkSegment {
	private String href;
	private String text;

	private String tooltipText;

	public ImageHyperlinkSegment() {
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public void paintFocus(GC gc, Color bg, Color fg, boolean selected,
			Rectangle repaintRegion) {
		Rectangle bounds = getBounds();
		if (bounds == null)
			return;
		if (selected) {
			gc.setBackground(bg);
			gc.setForeground(fg);
			gc.drawFocus(bounds.x, bounds.y, bounds.width, bounds.height);
		} else {
			gc.setForeground(bg);
			gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1,
					bounds.height - 1);
		}
	}

	public boolean isWordWrapAllowed() {
		return !isNowrap();
	}

	public void setWordWrapAllowed(boolean value) {
		setNowrap(!value);
	}

	public String getText() {
		return text!=null?text:""; //$NON-NLS-1$
	}
	
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return Returns the tooltipText.
	 */
	public String getTooltipText() {
		return tooltipText;
	}

	/**
	 * @param tooltipText
	 *            The tooltipText to set.
	 */
	public void setTooltipText(String tooltipText) {
		this.tooltipText = tooltipText;
	}
	
	public boolean isSelectable() {
		return true;
	}

	public boolean isFocusSelectable(Hashtable resourceTable) {
		return true;
	}

	public boolean setFocus(Hashtable resourceTable, boolean direction) {
		return true;
	}
}
