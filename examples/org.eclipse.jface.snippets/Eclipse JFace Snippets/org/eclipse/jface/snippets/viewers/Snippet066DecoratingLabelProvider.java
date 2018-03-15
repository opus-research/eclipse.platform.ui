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
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

public class Snippet066DecoratingLabelProvider {

	public class Person {
		private final int id;
		private String firstName;
		private String lastName;

		public Person(int id, String firstName, String lastName) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public int getId() {
			return id;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}
	}

	public class FirstNameLabelDecorator implements ILabelDecorator {
		private List<ILabelProviderListener> listeners;
		private String pattern = "(\\w)+";

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
			if (!(element instanceof Person)) {
				return null;
			}
			Person modelElement = (Person) element;
			System.out
					.println(modelElement.getFirstName() + " matches: " + modelElement.getFirstName().matches(pattern));
			// change icon to warning for even elements
			return modelElement.getFirstName().matches(pattern) ? image
					: Display.getCurrent().getSystemImage(SWT.ICON_ERROR);
		}

		@Override
		public String decorateText(String text, Object element) {
			if (!(element instanceof Person)) {
				return null;
			}
			Person modelElement = (Person) element;
			// decorate odd elements with prefix
			String decoration = isEven(modelElement.getId()) ? "" : "[decorated] ";
			return decoration + text;
		}

		private boolean isEven(int number) {
			return number % 2 == 0;
		}
	}

	public class FirstNameLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (!(element instanceof Person)) {
				return null;
			}
			Person modelElement = (Person) element;
			return modelElement.getFirstName();
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

	}

	public class FirstNameStyledLabelProvider implements DelegatingStyledCellLabelProvider.IStyledLabelProvider {

		@Override
		public StyledString getStyledText(Object element) {
			if (!(element instanceof Person)) {
				return null;
			}
			Person person = (Person) element;
			return new StyledString(person.getFirstName());
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
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
		}

	}

	public Snippet066DecoratingLabelProvider(Shell shell) {
		final TableViewer v = new TableViewer(shell);

		createColumns(v);
		v.setContentProvider(ArrayContentProvider.getInstance());
		Person[] model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
	}

	private void createColumns(TableViewer v) {
		final String[] titles = { "ID", "First Name", "Last Name" };
		final int[] bounds = { 100, 100, 100, 100 };

		TableViewerColumn column = this.createTableViewerColumn(v, titles[0], bounds[0], 0);
		// column.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(final Object element) {
		// final Person p = (Person) element;
		// return String.valueOf(p.getId());
		// }
		// });
		column.setLabelProvider(new StyledCellLabelProvider() {
            @Override
            public void update(final ViewerCell cell) {
                final Person person = (Person) cell.getElement();
				final String cellText = String.valueOf(person.getId());
				System.out.println("Setting id to: " + cellText);
                cell.setText(cellText);
            }});

		column = this.createTableViewerColumn(v, titles[1], bounds[1], 1);
		// column.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(final Object element) {
		// final Person p = (Person) element;
		// return p.getFirstName();
		// }
		// });
		// column.setLabelProvider(
		// new DecoratingLabelProvider(new FirstNameLabelProvider(), new
		// FirstNameLabelDecorator()));
		column.setLabelProvider(new DecoratingStyledCellLabelProvider(new FirstNameStyledLabelProvider(),
				new FirstNameLabelDecorator(), null));
		column.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Person person = (Person) cell.getElement();
				final String cellText = person.getFirstName();
				cell.setText(cellText);
			}
		});

		column = this.createTableViewerColumn(v, titles[2], bounds[2], 2);
		// column.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(final Object element) {
		// final Person p = (Person) element;
		// return p.getLastName();
		// }
		// });
		column.setLabelProvider(new StyledCellLabelProvider() {
            @Override
            public void update(final ViewerCell cell) {
                final Person person = (Person) cell.getElement();
				final String cellText = person.getLastName();
                cell.setText(cellText);
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		return viewerColumn;
	}

	private Person[] createModel() {
		Person[] elements = new Person[4];

		elements[0] = new Person(0, "Anna", "Schmidt");
		elements[1] = new Person(1, "Bernd", "Schmidt");
		elements[2] = new Person(2, "Cecilia", "Schmidt");
		elements[3] = new Person(3, "Dietrich", "Schmidt");

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
