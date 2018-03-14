/*******************************************************************************
 * Copyright (c) 2006, 2007 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.window;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A simple TreeViewer to demonstrate how custom tooltips could be created
 * easily. This is an extended version from
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet125.java
 *
 * This code is for users pre 3.3 others could use newly added tooltip support
 * in {@link CellLabelProvider}
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet023TreeViewerCustomTooltips {
	private class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((MyModel) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			return ((MyModel) element).child.size() > 0;
		}

	}

	public class MyModel {

		public ArrayList<MyModel> child = new ArrayList<MyModel>();
		public MyModel parent;
		public int counter;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		@Override
		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}
			return rv += counter;
		}
	}

	public Snippet023TreeViewerCustomTooltips(Shell shell) {
		final TreeViewer viewer = new TreeViewer(shell);
		setViewerFields(viewer);

		final Listener labelListener = createLabelListenerFor(viewer);
		Listener treeListener = createTreeListenerFor(viewer, labelListener);

		int[] eventsToListen = { SWT.Dispose, SWT.KeyDown, SWT.MouseMove,
				SWT.MouseHover };
		for (int event : eventsToListen) {
			viewer.getTree().addListener(event, treeListener);
		}
	}

	private Listener createTreeListenerFor(final TreeViewer viewer,
			final Listener labelListener) {

		return new Listener() {
			private Shell tip = null;
			private Label label = null;

			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseMove: {
					onMouseMove(event);
					break;
				}
				case SWT.MouseHover: {
					onMouseHover(event);
					break;
				}
				}
			}

			private void onMouseHover(Event event) {
				Point coords = new Point(event.x, event.y);
				TreeItem item = viewer.getTree().getItem(coords);
				if (item != null) {
					int columns = viewer.getTree().getColumnCount();
					for (int i = 0; i < columns || i == 0; i++) {
						if (item.getBounds(i).contains(coords)) {
							if (tip != null && !tip.isDisposed())
								tip.dispose();

							int style = SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL;
							Color backgroundColor = viewer.getTree()
									.getDisplay()
									.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
							Color foregroundColor = viewer.getTree()
									.getDisplay()
									.getSystemColor(SWT.COLOR_INFO_FOREGROUND);

							tip = new Shell(viewer.getTree().getShell(), style);
							tip.setBackground(backgroundColor);

							tip.setLayout(createFillLayout());
							label = new Label(tip, SWT.NONE);
							label.setForeground(foregroundColor);
							label.setBackground(backgroundColor);
							label.setData("_TABLEITEM", item);
							label.setText("Tooltip: " + item.getData()
									+ " => Column: " + i);

							label.addListener(SWT.MouseExit, labelListener);
							label.addListener(SWT.MouseDown, labelListener);

							Point size = tip.computeSize(SWT.DEFAULT,
									SWT.DEFAULT);
							Rectangle rect = item.getBounds(i);
							Point pt = viewer.getTree().toDisplay(rect.x,
									rect.y);

							tip.setBounds(pt.x, pt.y, size.x, size.y);
							tip.setVisible(true);
							break;
						}
					}
				}
			}

			private FillLayout createFillLayout() {
				FillLayout layout = new FillLayout();
				layout.marginWidth = 2;
				return layout;
			}

			private void onMouseMove(Event event) {
				// The event itself is ignored here
				if (tip == null)
					return;
				tip.dispose();
				tip = null;
				label = null;
			}
		};
	}

	private void setViewerFields(final TreeViewer viewer) {
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new MyContentProvider());
		viewer.setInput(createModel());
		viewer.getTree().setToolTipText("");
	}

	private Listener createLabelListenerFor(final TreeViewer v) {
		return new Listener() {

			private Label label;
			private Shell shell;

			@Override
			public void handleEvent(Event event) {
				this.label = (Label) event.widget;
				this.shell = label.getShell();
				switch (event.type) {
				case SWT.MouseDown:
					onMouseDown();
					break;
				case SWT.MouseExit:
					shell.dispose();
					break;
				}
			}

			private void onMouseDown() {
				Event e = new Event();
				e.item = (TreeItem) label.getData("_TABLEITEM");
				// Assuming table is single select, set the selection as if
				// the mouse down event went through to the table
				v.getTree().setSelection(new TreeItem[] { (TreeItem) e.item });
				v.getTree().notifyListeners(SWT.Selection, e);
				shell.dispose();
				v.getTree().setFocus();
			}
		};
	}

	private MyModel createModel() {
		MyModel root = new MyModel(0, null);
		root.counter = 0;
		MyModel tmp;
		for (int i = 1; i < 10; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				tmp.child.add(new MyModel(j, tmp));
			}
		}
		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet023TreeViewerCustomTooltips(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
