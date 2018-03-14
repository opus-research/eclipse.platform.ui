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

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * Customization for selection/hot color for Trees/Tables.
 */
public abstract class AbstractControlSelectionBackgroundCustomizationElement extends ControlElement {

	private final static String SELECTION_BACKGROUND_COLOR = "org.eclipse.e4.ui.css.swt.selectionBackgroundColor"; //$NON-NLS-1$
	private final static String SELECTION_BORDER_COLOR = "org.eclipse.e4.ui.css.swt.selectionBorderColor"; //$NON-NLS-1$
	private final static String HOT_BACKGROUND_COLOR = "org.eclipse.e4.ui.css.swt.hotBackgroundColor"; //$NON-NLS-1$
	private final static String HOT_BORDER_COLOR = "org.eclipse.e4.ui.css.swt.hotBorderColor"; //$NON-NLS-1$

	private static final Listener selectionListener = new Listener() {

		@Override
		public void handleEvent(Event event) {

			Scrollable control = (Scrollable) event.widget;
			int columnCount = 1;
			if (control instanceof Tree) {
				columnCount = ((Tree) control).getColumnCount();

			} else if (control instanceof Table) {
				columnCount = ((Table) control).getColumnCount();
			} else {
				// We only support Tree and Table.
				return;
			}

			boolean selected = (event.detail & SWT.SELECTED) != 0;
			boolean hot = (event.detail & SWT.HOT) != 0;

			// If we're painting the selection we also deal with hotness to be
			// consistent (i.e.: hotness is a 'selection preview on hover').
			event.detail &= ~SWT.HOT;

			if (selected || hot) {

				Color foreground = control.getForeground();
				if (foreground == null) {
					return;
				}

				GC gc = event.gc;
				Rectangle area = control.getClientArea();

				// Handling hotness with more than one column doesn't
				// work well because we'd have to change the clipping
				// for all of that (which isn't really possible)...
				boolean handlingOnlyHot = !selected && hot;
				if (handlingOnlyHot) {
					if (columnCount > 1) {
						return;
					}
				}


				String dataBackgroundKey;
				String dataBorderKey;
				if (handlingOnlyHot) {
					dataBackgroundKey = HOT_BACKGROUND_COLOR;
					dataBorderKey = HOT_BORDER_COLOR;
				} else {
					dataBackgroundKey = SELECTION_BACKGROUND_COLOR;
					dataBorderKey = SELECTION_BORDER_COLOR;
				}

				Object dataBackground = control.getData(dataBackgroundKey);
				Object dataBorder = control.getData(dataBorderKey);

				Color background = null;
				if ((dataBackground instanceof Color)) {
					background = (Color) dataBackground;
				}

				Color border = null;
				if ((dataBorder instanceof Color)) {
					border = (Color) dataBorder;
				}

				if (background == null && border == null) {
					// Nothing to draw
					return;
				}

				// Update clip to cover the whole column.
				int width = area.width;
				if (event.index == columnCount - 1 || columnCount == 0) {
					// i.e.: we only need to fix this for the last column
					// or if the tree/table reports having no columns (which
					// means single column... really weird hum?)
					if (width > 0) {
						Region region = new Region();
						gc.getClipping(region);
						region.add(event.x, event.y, width, event.height);
						gc.setClipping(region);
						region.dispose();
					}
				}

				if (background != null) {
					Color oldbackground = gc.getBackground();
					gc.setBackground(background);
					try {
						gc.fillRectangle(0, area.y, area.width + 2, area.height);
					} finally {
						gc.setBackground(oldbackground);
					}
				}
				if (border != null) {
					gc.setForeground(border);
					gc.drawRectangle(0, event.y, width - 1, event.height - 1);
				}
				// Restore the foreground for proper drawing later on.
				gc.setForeground(foreground);


				// we just painted the background...
				event.detail &= ~SWT.BACKGROUND;

				if (control instanceof Table) {
					if ((event.detail & SWT.FOCUSED) != 0) {
						// it has focus: remove the selected state as we
						// just painted it and it no longer needs to be
						// painted.
						event.detail &= ~SWT.SELECTED;
					} else {
						// it doesn't have focus: don't change the drawing
						// as the tree selection won't appear properly if we
						// remove the selected state.
					}
				} else if (control instanceof Tree) {
					event.detail &= ~SWT.SELECTED;
				}

			}
		}
	};

	public AbstractControlSelectionBackgroundCustomizationElement(Control control, CSSEngine engine) {
		super(control, engine);
	}

	private void setSelectionListener(Control control) {
		control.removeListener(SWT.EraseItem, selectionListener);
		control.addListener(SWT.EraseItem, selectionListener);
	}

	@Override
	public Control getControl() {
		return super.getControl();
	}

	// Selection background
	public void setSelectionBackgroundColor(Color color) {
		Control control = getControl();
		control.setData(SELECTION_BACKGROUND_COLOR, color);
		setSelectionListener(control);
	}


	public Color getSelectionBackgroundColor() {
		Control control = getControl();
		return getSelectionBackgroundColor(control);
	}

	public static Color getSelectionBackgroundColor(Control control) {
		Object data = control.getData(SELECTION_BACKGROUND_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// Selection border
	public void setSelectionBorderColor(Color color) {
		Control control = getControl();
		control.setData(SELECTION_BORDER_COLOR, color);
		setSelectionListener(control);
	}

	public Color getSelectionBorderColor() {
		Control control = getControl();
		return getSelectionBorderColor(control);
	}

	public static Color getSelectionBorderColor(Control control) {
		Object data = control.getData(SELECTION_BORDER_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// --- Hot background
	public void setHotBackgroundColor(Color color) {
		Control control = getControl();
		control.setData(HOT_BACKGROUND_COLOR, color);
		setSelectionListener(control);
	}

	public Color getHotBackgroundColor() {
		Control control = getControl();
		return getHotBackgroundColor(control);
	}

	public static Color getHotBackgroundColor(Control control) {
		Object data = control.getData(HOT_BACKGROUND_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

	// --- Hot border
	public void setHotBorderColor(Color color) {
		Control control = getControl();
		control.setData(HOT_BORDER_COLOR, color);
		setSelectionListener(control);
	}

	public Color getHotBorderColor() {
		Control control = getControl();
		return getHotBorderColor(control);
	}

	public static Color getHotBorderColor(Control control) {
		Object data = control.getData(HOT_BORDER_COLOR);
		if (data instanceof Color) {
			return (Color) data;
		}
		return null;
	}

}
