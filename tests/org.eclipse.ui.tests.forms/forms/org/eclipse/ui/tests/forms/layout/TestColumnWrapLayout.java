/*******************************************************************************
 * Copyright (c) 2011,2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/

package org.eclipse.ui.tests.forms.layout;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.internal.forms.widgets.ColumnLayoutUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestColumnWrapLayout {

	private final Point p20 = new Point(100, 20);
	private final Point p30 = new Point(100, 30);
	private final Point p50 = new Point(100, 50);
	private final Point p100 = new Point(100, 100);
	private final Point p200 = new Point(100, 200);

	private Display display;
	private Shell shell;
	private Composite inner;
	private ColumnLayout layout;

	@Before
	public void setUp() {
		display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		inner = new Composite(shell, SWT.NULL);
		inner.setSize(100, 300);
		layout = new ColumnLayout();
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		inner.setLayout(layout);
	}

	@After
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testEqualSizeColumns() {
		Point[] sizes = { p20, p30, p30, p20, p20, p30 };
		assertEquals(50, ColumnLayoutUtils.computeColumnHeight(3, sizes, 237, 0));
	}

	@Test
	public void testEqualSizeColumnsWithMargins() {
		Point[] sizes = { p20, p30, p30, p20, p20, p30 };
		assertEquals(60, ColumnLayoutUtils.computeColumnHeight(3, sizes, 200, 10));
	}

	@Test
	public void testVariedSizeColumns() {
		Point[] sizes = { p200, p200, p30 };
		assertEquals(230, ColumnLayoutUtils.computeColumnHeight(2, sizes, 100, 0));
	}

	@Test
	public void testLastEntryLargest() {
		Point[] sizes = { p50, p30, p30, p30, p50, p50, p100 };
		assertEquals(100, ColumnLayoutUtils.computeColumnHeight(4, sizes, 100, 0));
	}

	@Test
	public void testLargeMargins() {
		Point[] sizes = { p20, p20, p20, p20, p20, p50, p50};
		assertEquals(260, ColumnLayoutUtils.computeColumnHeight(3, sizes, 100, 100));
	}

	/**
	 * Test that labels with the WRAP property set do indeed wrap.
	 */
	@Test
	public void testColumnLayoutInShell() {
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 5;
		layout.minNumColumns = 2;
		layout.maxNumColumns = 2;
		layout.topMargin=2;
		layout.bottomMargin=3;
		layout.leftMargin = 5;
		layout.rightMargin = 5;
		ControlFactory.create(inner, 20, 20, 30);
		ControlFactory.create(inner, 20, 20, 40);
		ControlFactory.create(inner, 20, 20, 20);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(70, size.y);
		inner.setSize(size);
		inner.layout(true);
		assertEquals(new Rectangle(5, 2, 20, 30), inner.getChildren()[0].getBounds());
		assertEquals(new Rectangle(30, 2, 20, 40), inner.getChildren()[1].getBounds());
	}

	@Test
	public void testEffectOfHorizontalSpacing() {
		layout.horizontalSpacing = 10;
		ControlFactory.create(inner, 20, 20, 30);
	}

}
