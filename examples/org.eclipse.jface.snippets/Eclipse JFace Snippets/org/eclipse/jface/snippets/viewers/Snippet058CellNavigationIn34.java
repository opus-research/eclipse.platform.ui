/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Niels Lippke - initial API and implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * Example for full feature cell navigation until bug 230955 is fixed
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>, Niels Lippke
 *         <niels.lippke@airpas.com>
 *
 */
public class Snippet058CellNavigationIn34 {

	public class Person {
		public String givenname;
		public String surname;
		public String email;
		public String gender;

		public Person(String givenname, String surname, String email,
				String gender) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
			this.gender = gender;
		}
	}

	protected abstract class AbstractEditingSupport extends EditingSupport {
		private final CellEditor editor;

		public AbstractEditingSupport(TableViewer viewer) {
			super(viewer);
			this.editor = new TextCellEditor(viewer.getTable());
		}

		public AbstractEditingSupport(TableViewer viewer, CellEditor editor) {
			super(viewer);
			this.editor = editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected void setValue(Object element, Object value) {
			doSetValue(element, value);
			getViewer().update(element, null);
		}

		protected abstract void doSetValue(Object element, Object value);
	}

	public Snippet058CellNavigationIn34(Shell shell) {
		final TableViewer<Person, List<Person>> v = new TableViewer<Person, List<Person>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance(Person.class));

		TableViewerColumn<Person, List<Person>> column = null;
		column = createColumnFor(v, "Givenname");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.givenname;
			}
		});
		column.setEditingSupport(new AbstractEditingSupport(v) {

			@Override
			protected Object getValue(Object element) {
				return ((Person) element).givenname;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).givenname = value.toString();
			}

		});

		column = createColumnFor(v, "Surname");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.surname;
			}

		});
		column.setEditingSupport(new AbstractEditingSupport(v) {

			@Override
			protected Object getValue(Object element) {
				return ((Person) element).surname;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).surname = value.toString();
			}

		});

		column = createColumnFor(v, "E-Mail");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.email;
			}

		});
		column.setEditingSupport(new AbstractEditingSupport(v) {

			@Override
			protected Object getValue(Object element) {
				return ((Person) element).email;
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).email = value.toString();
			}

		});
		column = createColumnFor(v, "Gender");
    column.setLabelProvider(new ColumnLabelProvider<Person>() {

			@Override
			public String getText(Person element) {
				return element.gender;
			}

		});

		ComboBoxCellEditor editor = new ComboBoxCellEditor(v.getTable(),
				new String[] { "M", "F" });
		editor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_TRAVERSE_ACTIVATION
				| ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION
				| ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION
				| ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION);

		column.setEditingSupport(new AbstractEditingSupport(v, editor) {

			@Override
			protected Object getValue(Object element) {
				if (((Person) element).gender.equals("M"))
					return new Integer(0);
				return new Integer(1);
			}

			@Override
			protected void doSetValue(Object element, Object value) {
				if (((Integer) value).intValue() == 0) {
					((Person) element).gender = "M";
				} else {
					((Person) element).gender = "F";
				}
			}

		});

		CellNavigationStrategy naviStrat = new CellNavigationStrategy() {

			@Override
			public ViewerCell findSelectedCell(ColumnViewer viewer,
					ViewerCell currentSelectedCell, Event event) {
				ViewerCell cell = super.findSelectedCell(viewer,
						currentSelectedCell, event);

				if (cell != null) {
					v.getTable().showColumn(
							v.getTable().getColumn(cell.getColumnIndex()));
				}
				return cell;
			}

		};

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(
				v, new FocusCellOwnerDrawHighlighter(v), naviStrat);

		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				v) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		int feature = ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TableViewerEditor.create(v, focusCellManager, actSupport, feature);

		v.getColumnViewerEditor().addEditorActivationListener(
				new ColumnViewerEditorActivationListener() {

					@Override
					public void afterEditorActivated(
							ColumnViewerEditorActivationEvent event) {
					}

					@Override
					public void afterEditorDeactivated(
							ColumnViewerEditorDeactivationEvent event) {
					}

					@Override
					public void beforeEditorActivated(
							ColumnViewerEditorActivationEvent event) {
						ViewerCell cell = (ViewerCell) event.getSource();
						v.getTable().showColumn(
								v.getTable().getColumn(cell.getColumnIndex()));
					}

					@Override
					public void beforeEditorDeactivated(
							ColumnViewerEditorDeactivationEvent event) {
					}

				});

		List<Person> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	private TableViewerColumn<Person, List<Person>> createColumnFor(
			final TableViewer<Person, List<Person>> v, String label) {
		TableViewerColumn<Person, List<Person>> column = new TableViewerColumn<Person, List<Person>>(
				v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);
		return column;
	}

	private List<Person> createModel() {
		return Arrays
				.asList(new Person("Tom", "Schindl",
						"tom.schindl@bestsolution.at", "M"), new Person(
						"Boris", "Bokowski", "Boris_Bokowski@ca.ibm.com", "M"),
						new Person("Tod", "Creasey", "Tod_Creasey@ca.ibm.com",
								"M"), new Person("Wayne", "Beaton",
								"wayne@eclipse.org", "M"), new Person(
								"Jeanderson", "Candido",
								"jeandersonbc@gmail.com", "M"), new Person(
								"Lars", "Vogel", "lars.vogel@gmail.com", "M"),
						new Person("Hendrik", "Still",
								"hendrik.still@vogella.com", "M"));

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet058CellNavigationIn34(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
