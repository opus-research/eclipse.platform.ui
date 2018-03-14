/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 402445
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The ColumnLabelProvider is the label provider for viewers
 * that have column support such as {@link TreeViewer} and
 * {@link TableViewer}
 *
 * <p><b>This classes is intended to be subclassed</b></p>
 * @param <E> Type of an element of the model
 *
 * @since 3.3
 *
 */
public class ColumnLabelProvider<E> extends CellLabelProvider<E>
		implements IFontProvider<E>, IColorProvider<E>, ILabelProvider<E> {

	@Override
	public void update(ViewerCell<E> cell) {
		E element = cell.getElement();
		cell.setText(getText(element));
		Image image = getImage(element);
		cell.setImage(image);
		cell.setBackground(getBackground(element));
		cell.setForeground(getForeground(element));
		cell.setFont(getFont(element));

	}

	@Override
	public Font getFont(E element) {
		return null;
	}

	@Override
	public Color getBackground(E element) {
		return null;
	}

	@Override
	public Color getForeground(E element) {
		return null;
	}


	@Override
	public Image getImage(E element) {
		return null;
	}

	@Override
	public String getText(E element) {
		return element == null ? "" : element.toString();//$NON-NLS-1$
	}

}
