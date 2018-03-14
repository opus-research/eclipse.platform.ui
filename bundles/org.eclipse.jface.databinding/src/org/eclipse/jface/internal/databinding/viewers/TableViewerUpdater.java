/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bugs 226765, 230296
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.swt.widgets.Table;

/**
 * NON-API - A {@link ViewerUpdater} that updates {@link AbstractTableViewer}
 * instances.
 *
 * @since 1.2
 */
class TableViewerUpdater extends ViewerUpdater {
	private AbstractTableViewer viewer;

	TableViewerUpdater(AbstractTableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	public void insert(Object element, int position) {
		viewer.insert(element, position);
	}

	@Override
	public void remove(Object element, int position) {
		viewer.removeAtPosition(element, position);
	}

	@Override
	public void replace(Object oldElement, Object newElement, int position) {
		if (isElementOrderPreserved())
			viewer.replace(newElement, position);
		else {
			super.replace(oldElement, newElement, position);
		}
	}

	@Override
	public void add(Object[] elements) {
		viewer.add(elements);
	}

	@Override
	public void remove(Object[] elements) {
		viewer.remove(elements);
	}

	@Override
	public void move(Object element, int oldPosition, int newPosition) {
		// preserve the selectioin while calling super.move()
		Table table = (Table) viewer.getControl();
		int[] selectionIndices = table.getSelectionIndices();
		super.move(element, oldPosition, newPosition);
		replaceIndex(selectionIndices, oldPosition, newPosition);
		table.setSelection(selectionIndices);
	}

	/**
	 * Replaces the first found integer {@code oldPosition} in
	 * {@code selectionIndices} whit {@code newPosition}.
	 *
	 * @param selectionIndices
	 *            an array of unique integers
	 * @param oldPosition
	 *            the integer to be removed
	 * @param newPosition
	 *            the integer to be inserted at the index of {@code oldPosition}
	 */
	private void replaceIndex(int[] selectionIndices, int oldPosition, int newPosition) {
		for (int i = 0; i < selectionIndices.length; i++) {
			int j = selectionIndices[i];
			if (j == oldPosition) {
				selectionIndices[i] = newPosition;
				return;
			}
		}
	}
}
