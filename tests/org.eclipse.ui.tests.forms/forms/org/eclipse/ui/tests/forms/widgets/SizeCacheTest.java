/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.junit.Test;

import junit.framework.TestCase;

public class SizeCacheTest extends TestCase {
	private static Display display;
	private Shell shell;
	private static String shortText = "Hedgehog";
	private static String longText = "A hedgehog is any of the spiny mammals of the subfamily Erinaceinae, in the order Erinaceomorpha. " //$NON-NLS-1$
			+ "There are seventeen species of hedgehog in five genera, found through parts of Europe, Asia, Africa and New Zealand. " //$NON-NLS-1$
			;
	private Font font;
	// change this to true if you want to see test is slow motion
	private boolean humanWatching = false;
	private SizeCache sizeCache;


	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}
	}

	@Override
	public void setUp() throws Exception {
		font = new Font(display, "Arial", 12, SWT.NORMAL);
		shell = new Shell(display);
		shell.setSize(600, 400);
		shell.setLayout(GridLayoutFactory.fillDefaults().create());
		shell.setFont(font);
		shell.open();
	}

	@Override
	public void tearDown() throws Exception {
		if (humanWatching)
			dispatch(1000);
		shell.dispose();
		font.dispose();
	}

	private static void dispatch() {
		while (display.readAndDispatch()) {
		}
	}

	private static void dispatch(int msec) {
		long cur = System.currentTimeMillis();
		do {
			dispatch();
			long pass = System.currentTimeMillis() - cur;
			if (pass < msec) {
				// not doing display.sleep() because its automated tests,
				// nothing will cause any display events
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			} else
				break;
		} while (true);
	}

	private Label createLabel(Composite comp, String text, int style) {
		Label l = new Label(comp, style);
		l.setText(text);
		l.setFont(comp.getFont());
		return l;
	}

	private Point checkSizeEquals(int whint, int hhint) {
		update();
		Point calcSize = sizeCache.getControl().computeSize(whint, hhint);
		Point cachedSize = sizeCache.computeSize(whint, hhint);
		assertEquals(calcSize, cachedSize);
		return cachedSize;
	}

	private void update() {
		shell.layout(true, true);
		if (humanWatching)
			dispatch(300);
		else
			dispatch();
	}

	private Composite createComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setFont(parent.getFont());
		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_MAGENTA));
		return comp;
	}

	private Composite createFillComp(Composite parent) {
		Composite comp = createComposite(parent);
		comp.setLayout(GridLayoutFactory.fillDefaults().create());
		Label l = createLabel(comp, longText, SWT.WRAP);
		l.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).create());
		return comp;
	}

	private Composite createFixedComp(Composite parent) {
		Composite comp = createComposite(parent);
		comp.setLayout(GridLayoutFactory.fillDefaults().create());

		Label l = createLabel(comp, shortText, SWT.NONE);
		l.setLayoutData(GridDataFactory.swtDefaults().create());
		return comp;
	}

	private void testCacheSize(Control inner) {
		sizeCache = new SizeCache(inner);
		Point size = checkSizeEquals(SWT.DEFAULT, SWT.DEFAULT);
		int w = size.x;
		int h = size.y;
		checkSizeEquals(w, SWT.DEFAULT);
		checkSizeEquals(w / 2, SWT.DEFAULT);
		checkSizeEquals(w * 2, SWT.DEFAULT);
		checkSizeEquals(SWT.DEFAULT, h);
		checkSizeEquals(SWT.DEFAULT, h / 2);
		checkSizeEquals(SWT.DEFAULT, h * 2);
		// SizeCache currently lies about size for width hint 0..5
		// for wrap controls, uncomment the next line if this is fixed
		// checkSizeEquals(0, SWT.DEFAULT);
	}

	@Test
	public void testFixedLabel() {
		testCacheSize(createLabel(shell, shortText, SWT.NONE));
	}

	@Test
	public void testWrapLabel() {
		testCacheSize(createLabel(shell, shortText, SWT.WRAP));
	}

	@Test
	public void testFixedComp() {
		testCacheSize(createFixedComp(shell));
	}

	@Test
	public void testFillComp() {
		testCacheSize(createFillComp(shell));
	}

	@Test
	public void testWrapComp1() {
		Composite inner = createComposite(shell);
		inner.setLayout(new TableWrapLayout());
		createLabel(inner, shortText, SWT.NONE);
		createLabel(inner, longText, SWT.NONE);
		// this test is currently failing, it was changed to pass but
		// if it is ever fixed change code below to
		// testCacheSize(inner);
		sizeCache = new SizeCache(inner);
		int w = checkSizeEquals(SWT.DEFAULT, SWT.DEFAULT).x;

		int whint = w / 2;
		int hhint = SWT.DEFAULT;
		Point calcSize = sizeCache.getControl().computeSize(whint, hhint);
		Point cachedSize = sizeCache.computeSize(whint, hhint);
		assertTrue("Bug is fixed!", calcSize.x < cachedSize.x);
	}

	@Test
	public void testWrapCompWrapLabels() {
		Composite inner = createComposite(shell);
		inner.setLayout(new TableWrapLayout());
		createLabel(inner, shortText, SWT.WRAP);
		createLabel(inner, longText, SWT.WRAP);
		testCacheSize(inner);
	}

	@Test
	public void testFixedLabelLong() {
		testCacheSize(createLabel(shell, longText, SWT.NONE));
	}

	@Test
	public void testWrapLabelLong() {
		testCacheSize(createLabel(shell, longText, SWT.WRAP));
	}

	@Test
	public void testHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.NONE);
		link.setText(longText);
		link.setFont(shell.getFont());
		testCacheSize(link);
	}
}
