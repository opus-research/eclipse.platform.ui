/*******************************************************************************
 * Copyright (c) 2007, 2015 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 413427, 475361
 *     Jeanderson Candido (http://jeandersonbc.github.io) - Bug 414565
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * Demonstrate alternating row colors using new Jace 3.3 API
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet041TableViewerAlternatingColors {

	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	private class OptimizedIndexSearcher {
		private int lastIndex = 0;

		public boolean isEven(TableItem item) {
			TableItem[] items = item.getParent().getItems();

			// 1. Search the next ten items
			for (int i = lastIndex; i < items.length && lastIndex + 10 > i; i++) {
				if (items[i] == item) {
					lastIndex = i;
					return lastIndex % 2 == 0;
				}
			}

			// 2. Search the previous ten items
			for (int i = lastIndex; i < items.length && lastIndex - 10 > i; i--) {
				if (items[i] == item) {
					lastIndex = i;
					return lastIndex % 2 == 0;
				}
			}

			// 3. Start from the beginning
			for (int i = 0; i < items.length; i++) {
				if (items[i] == item) {
					lastIndex = i;
					return lastIndex % 2 == 0;
				}
			}

			return false;
		}
	}

	final private OptimizedIndexSearcher searcher = new OptimizedIndexSearcher();

	public Snippet041TableViewerAlternatingColors(Shell shell) {
		final TableViewer<MyModel, List<MyModel>> viewer = new TableViewer<MyModel, List<MyModel>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);

		viewer.setContentProvider(ArrayContentProvider
				.getInstance(MyModel.class));
		String[] labels = { "Column 1", "Column 2" };
		for (String label : labels) {
			createColumnFor(viewer, label);
		}
		viewer.setInput(createModel());
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);

		Button b = new Button(shell, SWT.PUSH);
		b.addSelectionListener(createAdapterFor(viewer));
	}

	private SelectionAdapter createAdapterFor(final TableViewer viewer) {
		return new SelectionAdapter() {
			boolean b = true;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (b) {
					viewer.setFilters(new ViewerFilter[] { createFilterForViewer() });
					b = false;

				} else {
					viewer.setFilters(new ViewerFilter[0]);
					b = true;
				}
			}
		};
	}

	private ViewerFilter<MyModel, List<MyModel>> createFilterForViewer() {
		return new ViewerFilter<MyModel, List<MyModel>>() {

			@Override
			public boolean select(Viewer<List<MyModel>> viewer,
					Object parentElement, MyModel element) {
				return element.counter % 2 == 0;
			}
		};
	}

	private TableViewerColumn<MyModel, List<MyModel>> createColumnFor(
			TableViewer<MyModel, List<MyModel>> viewer, String label) {
		TableViewerColumn<MyModel, List<MyModel>> column = new TableViewerColumn<MyModel, List<MyModel>>(
				viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.setLabelProvider(createLabelProviderFor(viewer));
		return column;
	}

  private ColumnLabelProvider<MyModel> createLabelProviderFor(
			final TableViewer viewer) {
    return new ColumnLabelProvider<MyModel>() {
			boolean isEvenIdx = true;

			@Override
			public Color getBackground(MyModel element) {
				Color grayColor = viewer.getTable().getDisplay()
						.getSystemColor(SWT.COLOR_GRAY);

				return (isEvenIdx ? null : grayColor);
			}

			@Override
			public void update(ViewerCell<MyModel> cell) {
				isEvenIdx = searcher.isEven((TableItem) cell.getItem());
				super.update(cell);
			}
		};
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<>();
		for (int i = 0; i < 100000; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet041TableViewerAlternatingColors(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

}
