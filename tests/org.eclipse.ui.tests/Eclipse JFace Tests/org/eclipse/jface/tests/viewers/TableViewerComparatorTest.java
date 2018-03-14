/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 *
 */
public class TableViewerComparatorTest extends ViewerComparatorTest {

	/**
	 * @param name
	 */
	public TableViewerComparatorTest(String name) {
		super(name);
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent);
		viewer.setContentProvider(new TeamModelContentProvider());
		viewer.setLabelProvider(new TeamModelLabelProvider());
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

	public void testViewerSorter(){
		fViewer.setSorter(new ViewerSorter());
		assertSortedResult(TEAM1_SORTED);
	}

	public void testViewerSorterInsertElement(){
		fViewer.setSorter(new ViewerSorter());
		team1.addMember("Duong");
		assertSortedResult(TEAM1_SORTED_WITH_INSERT);
	}

	public void testViewerComparator(){
		fViewer.setComparator(new ViewerComparator());
		assertSortedResult(TEAM1_SORTED);
	}

	public void testViewerComparatorInsertElement(){
		fViewer.setComparator(new ViewerComparator());
		team1.addMember("Duong");
		assertSortedResult(TEAM1_SORTED_WITH_INSERT);
	}

	/**
	 * Use a comparator that returns random results.
	 */
	public void testViewerComparatorViolatesGeneralContract() throws Exception {
		Random rand = new Random(0);
		Comparator<String> violatingComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return rand.nextInt(3) - 1;
			}
		};
		fViewer.setComparator(new ViewerComparator(violatingComparator));

		String[] members = new String[30000];
		for (int i = 0; i < members.length; i++) {
			StringBuilder name = rand.ints(5, 'A', 'Z').collect(StringBuilder::new, StringBuilder::appendCodePoint,
					StringBuilder::append);
			members[i] = name.toString();
		}
		Team randTeam = new Team("RAND", members);

		IllegalArgumentException exception = null;
		ILogger logger = Policy.getLog();
		Policy.setLog(new ILogger() {
			@Override
			public void log(IStatus status) {
				// ignore
			}
		});
		try {
			fViewer.setInput(randTeam);
		} catch (IllegalArgumentException e) {
			exception = e;
			Policy.setLog(logger);
		}
		assertNotNull(exception);
	}

	/**
	 * Use a label provider that returns unstable labels for the same object.
	 */
	public void testViewerComparatorViolatesGeneralContract2() throws Exception {
		List<Integer> timSortTestList = new ArrayList<Integer>();
		{
			for (int i = 0; i < 64; ++i) {
				timSortTestList.add(i);
			}
			Collections.shuffle(timSortTestList, new Random(0));
		}
		Comparator<Integer> broken = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1 < 5) {
					return 1;
				}
				return Integer.compare(o1, o2);
			}
		};
		Collections.sort(timSortTestList, broken); // throws up

		Random rand = new Random(0);
		fViewer.setLabelProvider(new TeamModelLabelProvider() {
			@Override
			public String getText(Object element) {
				return Math.abs(rand.nextInt()) + ".";
			}
		});

		String[] members = new String[300];
		for (int i = 0; i < members.length; i++) {
			String name;
			if (i % 3 == 0) {
				name = Integer.toString(i);
			} else {
				name = Integer.toString(i - 3);
			}
			members[i] = name.toString();
		}
		Team randTeam = new Team("RAND", members);

		IllegalArgumentException exception = null;
		try {
			fViewer.setComparator(new ViewerComparator());
			fViewer.setInput(randTeam);
		} catch (IllegalArgumentException e) {
			exception = e;
		}
		assertNotNull(exception);
	}

	private void assertSortedResult(String[] expected){
		TableItem[] items = getTableViewer().getTable().getItems();
		for (int i = 0; i < items.length; i++){
			TableItem item = items[i];
			assertEquals("Item not expected.  actual=" + item.getText() + " expected=", expected[i], item.getText());
		}
	}

	@Override
	protected void setInput() {
		fViewer.setInput(team1);
	}

	protected TableViewer getTableViewer(){
		return (TableViewer)fViewer;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TableViewerComparatorTest.class);
	}

}
