/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 402445
 *******************************************************************************/

package org.eclipse.jface.viewers;


/**
 * TableColumnViewerLabelProvider is the mapping from the table based providers
 * to the ViewerLabelProvider.
 *
 * @param <E>
 *            Type of an element of the model
 * @param <I>
 *            Type of the input
 *
 * @since 3.3
 * @see ITableLabelProvider
 * @see ITableColorProvider
 * @see ITableFontProvider
 *
 */
class TableColumnViewerLabelProvider<E> extends WrappedViewerLabelProvider<E> {

	private ITableLabelProvider<E> tableLabelProvider;

	private ITableColorProvider<E> tableColorProvider;

	private ITableFontProvider<E> tableFontProvider;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param labelProvider
	 *            instance of a table based label provider
	 * @see ITableLabelProvider
	 * @see ITableColorProvider
	 * @see ITableFontProvider
	 */

	@SuppressWarnings("unchecked")
	public TableColumnViewerLabelProvider(IBaseLabelProvider<E> labelProvider) {
		super(labelProvider);

		if (labelProvider instanceof ITableLabelProvider)
			tableLabelProvider = (ITableLabelProvider<E>) labelProvider;

		if (labelProvider instanceof ITableColorProvider)
			tableColorProvider = (ITableColorProvider<E>) labelProvider;

		if (labelProvider instanceof ITableFontProvider)
			tableFontProvider = (ITableFontProvider<E>) labelProvider;
	}

	@Override
	public void update(ViewerCell<E> cell) {

		E element = cell.getElement();
		int index = cell.getColumnIndex();

		if (tableLabelProvider == null) {
			cell.setText(getLabelProvider().getText(element));
			cell.setImage(getLabelProvider().getImage(element));
		} else {
			cell.setText(tableLabelProvider.getColumnText(element, index));
			cell.setImage(tableLabelProvider.getColumnImage(element, index));
		}

		if (tableColorProvider == null) {
			if (getColorProvider() != null) {
				cell.setBackground(getColorProvider().getBackground(element));
				cell.setForeground(getColorProvider().getForeground(element));
			}

		} else {
			cell.setBackground(tableColorProvider
					.getBackground(element, index));
			cell.setForeground(tableColorProvider
					.getForeground(element, index));

		}

		if (tableFontProvider == null) {
			if (getFontProvider() != null)
				cell.setFont(getFontProvider().getFont(element));
		} else
			cell.setFont(tableFontProvider.getFont(element, index));

	}


}
