/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											- bug fixes for 182443
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * The CellLabelProvider is an abstract implementation of a label provider for
 * structured viewers.
 * 
 * <p><b>This class is intended to be subclassed</b></p>
 * 
 * @since 3.3
 * @see ColumnLabelProvider as a concrete implementation
 */
public abstract class CellLabelProvider extends BaseLabelProvider implements IToolTipProvider {

	/**
	 * Create a new instance of the receiver.
	 */
	public CellLabelProvider() {
		super();
	}

	/**
	 * Create a ViewerLabelProvider for the column at index
	 * 
	 * @param labelProvider
	 *            The labelProvider to convert
	 * @return ViewerLabelProvider
	 */
	/* package */static CellLabelProvider createViewerLabelProvider(
			ColumnViewer viewer, IBaseLabelProvider labelProvider) {

		boolean noColumnTreeViewer = viewer instanceof AbstractTreeViewer && viewer
				.doGetColumnCount() == 0;

		if (!noColumnTreeViewer
				&& (labelProvider instanceof ITableLabelProvider
						|| labelProvider instanceof ITableColorProvider || labelProvider instanceof ITableFontProvider))
			return new TableColumnViewerLabelProvider(labelProvider);
		if (labelProvider instanceof CellLabelProvider)
			return (CellLabelProvider) labelProvider;
		return new WrappedViewerLabelProvider(labelProvider);

	}

	public Image getToolTipImage(Object object) {
		return null;
	}

	public String getToolTipText(Object element) {
		return null;
	}

	public Color getToolTipBackgroundColor(Object object) {
		return null;
	}

	public Color getToolTipForegroundColor(Object object) {
		return null;
	}

	public Font getToolTipFont(Object object) {
		return null;
	}

	public Point getToolTipShift(Object object) {
		return null;
	}

	public boolean useNativeToolTip(Object object) {
		return false;
	}

	public int getToolTipTimeDisplayed(Object object) {
		return 0;
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 0;
	}

	/**
	 * This default implementation uses {@link SWT#SHADOW_NONE}.
	 */
	public int getToolTipStyle(Object object) {
		return SWT.SHADOW_NONE;
	}

	/**
	 * Update the label for cell.
	 * 
	 * @param cell
	 *            {@link ViewerCell}
	 */
	public abstract void update(ViewerCell cell);
	
	/**
	 * Initialize this label provider for use with the given column viewer for
	 * the given column. Subclasses may extend but should call the super
	 * implementation (which at this time is empty but may be changed in the
	 * future).
	 * 
	 * @param viewer
	 *            the viewer
	 * @param column
	 *            the column, or <code>null</code> if a column is not
	 *            available.
	 * 
	 * @since 3.4
	 */
	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
	}

	/**
	 * Dispose of this label provider which was used with the given column
	 * viewer and column. Subclasses may extend but should call the super
	 * implementation (which calls {@link #dispose()}).
	 * 
	 * @param viewer
	 *            the viewer
	 * @param column
	 *            the column, or <code>null</code> if a column is not
	 *            available.
	 * 
	 * @since 3.4
	 */
	public void dispose(ColumnViewer viewer, ViewerColumn column) {
		dispose();
	}
	
}
