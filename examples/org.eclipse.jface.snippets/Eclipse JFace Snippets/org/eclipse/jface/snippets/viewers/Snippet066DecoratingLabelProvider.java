/*******************************************************************************
 * Copyright (c) 2017 Fabian Pfaff and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Pfaff - Bug 517461
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet066DecoratingLabelProvider {

	public class MyModel {
		public final int counter;
		private final Image image;

		public MyModel(int counter) {
			this.counter = counter;
			this.image = Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION);
		}

		public int getCounter() {
			return counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}

		public Image getImage() {
			return image;
		}
	}

	public class MyLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			if (!(element instanceof MyModel)) {
				return null;
			}
			return ((MyModel) element).getImage();
		}
	}

	public class MyLabelDecorator implements ILabelDecorator {
		private List<ILabelProviderListener> listeners;

		@Override
		public void addListener(ILabelProviderListener listener) {
			if (listeners == null) {
				listeners = new ArrayList<>();
			}
			listeners.add(listener);
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			if (listeners != null) {
				listeners.remove(listener);
			}
		}

		@Override
		public Image decorateImage(Image image, Object element) {
			if (!(element instanceof MyModel)) {
				return null;
			}
			MyModel modelElement = (MyModel) element;
			// change icon to warning for even elements
			return isEven(modelElement.getCounter()) ? Display.getCurrent().getSystemImage(SWT.ICON_WARNING) : image;
		}

		@Override
		public String decorateText(String text, Object element) {
			if (!(element instanceof MyModel)) {
				return null;
			}
			MyModel modelElement = (MyModel) element;
			// decorate odd elements with prefix
			String decoration = isEven(modelElement.getCounter()) ? "" : "[decorated] ";
			return decoration + text;
		}

		private boolean isEven(int number) {
			return number % 2 == 0;
		}
	}

	public Snippet066DecoratingLabelProvider(Shell shell) {
		final TableViewer v = new TableViewer(shell);

		ILabelProvider lp = new MyLabelProvider();
		ILabelDecorator decorator = new MyLabelDecorator();
		v.setLabelProvider(new DecoratingLabelProvider(lp, decorator));
		v.setContentProvider(ArrayContentProvider.getInstance());
		MyModel[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10];

		for (int i = 0; i < 10; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet066DecoratingLabelProvider(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

}
