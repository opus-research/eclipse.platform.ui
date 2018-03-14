/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableViewerTest extends StructuredItemViewerTest {
	private TestElement testElement1;
	private TestElement testElement2;
	private TestElement testElement3;
	private Object expectedSelection;
	private Object expectedPostSelection;

	public static class TableTestLabelProvider extends TestLabelProvider implements ITableLabelProvider {
		public boolean fExtended = false;

		@Override
		public String getText(Object element) {
			if (fExtended) {
				return providedString((String) element);
			}
			return element.toString();
		}

		@Override
		public String getColumnText(Object element, int index) {
			if (fExtended) {
				return providedString((TestElement) element);
			}
			return element.toString();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	public TableViewerTest(String name) {
		super(name);
	}

	/**
	 * Creates the viewer used by this test, under the given parent widget.
	 */
	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TableViewer viewer = createTableViewer(parent);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(new TableTestLabelProvider());
		viewer.getTable().setLinesVisible(true);

		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		String headers[] = { "column 1 header", "column 2 header" };

		ColumnLayoutData layouts[] = { new ColumnWeightData(100),
				new ColumnWeightData(100) };

		final TableColumn columns[] = new TableColumn[headers.length];

		for (int i = 0; i < headers.length; i++) {
			layout.addColumnData(layouts[i]);
			TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
			tc.setResizable(layouts[i].resizable);
			tc.setText(headers[i]);
			columns[i] = tc;
		}

		return viewer;
	}

	ViewerColumn getViewerColumn(ColumnViewer viewer, int index) {
		Method method;
		try {
			method = ColumnViewer.class.getDeclaredMethod("getViewerColumn", new Class[]{int.class});
			method.setAccessible(true);
			return (ViewerColumn) method.invoke(viewer, new Object[]{new Integer(index)});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testViewerColumn() {
    	assertNull(getViewerColumn((TableViewer) fViewer, -1));
		assertNotNull(getViewerColumn((TableViewer) fViewer, 0));
		assertNotNull(getViewerColumn((TableViewer) fViewer, 1));
		assertNull(getViewerColumn((TableViewer) fViewer, 2));
    }

	/**
	 * Get the content provider for the viewer.
	 *
	 * @return IContentProvider
	 */
	protected TestModelContentProvider getContentProvider() {
		return new TestModelContentProvider();
	}

	/**
	 * Create the table viewer for the test
	 *
	 * @param parent
	 * @return
	 */
	protected TableViewer createTableViewer(Composite parent) {
		return new TableViewer(parent);
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		TableItem ti = (TableItem) fViewer.testFindItem(first);
		Table table = ti.getParent();
		return table.getItemCount();
	}

	@Override
	protected String getItemText(int at) {
		Table table = (Table) fViewer.getControl();
		return table.getItem(at).getText();
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(TableViewerTest.class);
	}

	@Override
	public void testLabelProvider() {

		TableViewer viewer = (TableViewer) fViewer;
		TableTestLabelProvider provider = (TableTestLabelProvider) viewer
				.getLabelProvider();

		provider.fExtended = true;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
		fViewer.refresh();
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
		provider.fExtended = false;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
	}

	@Override
	public void testLabelProviderStateChange() {
		TableViewer tableviewer = (TableViewer) fViewer;
		TableTestLabelProvider provider = (TableTestLabelProvider) tableviewer
				.getLabelProvider();

		provider.fExtended = true;
		provider.setSuffix("added suffix");
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
		tableviewer.refresh();
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
		provider.fExtended = false;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
		fViewer.refresh();
	}

	public void testRemove() {
		TableViewer tableviewer = (TableViewer) fViewer;
		TestElement first = fRootElement.getFirstChild();
		((TestElement) fViewer.getInput()).deleteChild(first);
		tableviewer.remove(first);
		assertTrue("Removed item still exists",
				fViewer.testFindItem(first) == null);
	}

	public void testRemoveAtPosition_selectedElement0() {
		prepRemoveAtPositionTest();

		TableViewer viewer = (TableViewer) fViewer;
		List<TestElement> selectedElementsAfterRemove = Arrays.asList(testElement1, testElement2, testElement3);
		expectedSelection = expectedPostSelection = new StructuredSelection(selectedElementsAfterRemove);

		viewer.removeAtPosition(testElement1, 0);
		printTable("After remove index 0", viewer.getTable());

		assertThat(viewer.getTable().getSelectionIndices(), is(new int[] { 0, 1, 3 }));
		IStructuredSelection structuredSelection = viewer.getStructuredSelection();
		List<TestElement> selectedElements = structuredSelection.toList();
		assertThat(selectedElements, is(selectedElementsAfterRemove));
	}

	public void testRemoveAtPosition_selectedElement1() {
		prepRemoveAtPositionTest();

		TableViewer viewer = (TableViewer) fViewer;
		List<TestElement> selectedElementsAfterRemove = Arrays.asList(testElement1, testElement2, testElement3);
		expectedSelection = expectedPostSelection = new StructuredSelection(selectedElementsAfterRemove);

		viewer.removeAtPosition(testElement1, 1);
		printTable("After remove index 1", viewer.getTable());

		assertThat(viewer.getTable().getSelectionIndices(), is(new int[] { 0, 1, 3 }));
		IStructuredSelection structuredSelection = viewer.getStructuredSelection();
		List<TestElement> list = structuredSelection.toList();
		assertThat(list, is(selectedElementsAfterRemove));

	}

	public void testRemoveAtPosition_selectedElement2() {
		prepRemoveAtPositionTest();

		TableViewer viewer = (TableViewer) fViewer;
		List<TestElement> selectedElementsAfterRemove = Arrays.asList(testElement1, testElement1, testElement3);
		expectedSelection = expectedPostSelection = new StructuredSelection(selectedElementsAfterRemove);

		viewer.removeAtPosition(testElement2, 2);
		printTable("After remove index 2", viewer.getTable());

		assertThat(viewer.getTable().getSelectionIndices(), is(new int[] { 0, 1, 3 }));
		IStructuredSelection structuredSelection = viewer.getStructuredSelection();
		List<TestElement> list = structuredSelection.toList();
		assertThat(list, is(selectedElementsAfterRemove));
	}

	public void testRemoveAtPosition_notSelectedElement() {
		prepRemoveAtPositionTest();

		TableViewer viewer = (TableViewer) fViewer;
		List<TestElement> selectedElementsAfterRemove = Arrays.asList(testElement1, testElement1, testElement2,
				testElement3);

		viewer.removeAtPosition(testElement1, 3);
		printTable("After remove index 3", viewer.getTable());

		assertThat(viewer.getTable().getSelectionIndices(), is(new int[] { 0, 1, 2, 3 }));
		IStructuredSelection structuredSelection = viewer.getStructuredSelection();
		List<TestElement> selectedElements = structuredSelection.toList();
		assertThat(selectedElements, is(selectedElementsAfterRemove));
	}

	public void testRemoveAtPosition_selectedElement3() {
		prepRemoveAtPositionTest();

		TableViewer viewer = (TableViewer) fViewer;
		List<TestElement> selectedElementsAfterRemove = Arrays.asList(testElement1, testElement1, testElement2);
		expectedSelection = expectedPostSelection = new StructuredSelection(selectedElementsAfterRemove);

		viewer.removeAtPosition(testElement3, 4);
		printTable("After remove index 4", viewer.getTable());

		assertThat(viewer.getTable().getSelectionIndices(), is(new int[] { 0, 1, 2 }));
		IStructuredSelection structuredSelection = viewer.getStructuredSelection();
		List<TestElement> list = structuredSelection.toList();
		assertThat(list, is(selectedElementsAfterRemove));
	}

	private void printTable(String string, Table table) {
		boolean debug = false;
		if (!debug) {
			return;
		}
		System.out.println(string);
		int[] selectionIndices = table.getSelectionIndices();
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			boolean printed = false;
			for (int selectedIndex : selectionIndices) {
				if (selectedIndex == i) {
					System.out.println(i + ": [" + items[i].getText() + "]");
					printed = true;
				}
			}
			if (!printed) {
				System.out.println(i + ":  " + items[i].getText());
			}
		}
	}

	private void prepRemoveAtPositionTest() {
		// [0],[1],3
		testElement1 = new TestElement(fModel, fRootElement);
		// [2]
		testElement2 = new TestElement(fModel, fRootElement);
		// [4]
		testElement3 = new TestElement(fModel, fRootElement);

		testElement1.fId = "1";
		testElement1.fSomeName = "Egg";
		testElement2.fId = "2";
		testElement2.fSomeName = "Tee";
		testElement3.fId = "3";
		testElement3.fSomeName = "Flower";

		TestElement[] children = new TestElement[] { testElement1, testElement1, testElement2, testElement1,
				testElement3 };
		TestModelChange testModelChange = new TestModelChange(TestModelChange.INSERT, fRootElement, children);
		fRootElement.deleteChildren();
		fRootElement.addChildren(children, testModelChange);

		Table table = (Table) fViewer.getControl();
		table.select(new int[] { 0, 1, 2, 4 });
		IStructuredSelection structuredSelection = fViewer.getStructuredSelection();
		List<TestElement> list = structuredSelection.toList();
		assertThat(list, is(Arrays.asList(testElement1, testElement1, testElement2, testElement3)));

		printTable("Before remove", ((TableViewer) fViewer).getTable());
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (expectedSelection != null && event.getSelection().equals(expectedSelection)) {
					return;
				}
				fail();
			}
		});
		fViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (expectedPostSelection != null && event.getSelection().equals(expectedPostSelection)) {
					return;
				}
				fail();
			}
		});
	}

}
