/*******************************************************************************
 * Copyright (c) 2017 Ralf M Petter<ralf.petter@gmail.com and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ralf M Petter<ralf.petter@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Section;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Section
 */
public class SectionTest {
	private static final int TITLE_TWIST = Section.TWISTIE | Section.TITLE_BAR;
	private static final String TEXT1 = "Text";
	private static Display display;
	private static long delay = 100;
	private Shell shell;
	private Section sec;
	private Rectangle secbounds;
	// change this to true if you want to see test is slow motion
	private boolean humanWatching = false;

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}

	}

	private static Point getTextExtend(String str) {
		GC gc = new GC(display);
		try {
			return gc.stringExtent(str);
		} finally {
			gc.dispose();
		}
	}

	@Before
	public void setUp() throws Exception {
		shell = new Shell(display);
		shell.setSize(600, 400);
		shell.setLayout(new GridLayout());
		shell.open();
	}

	@After
	public void tearDown() throws Exception {
		if (humanWatching) {
			dispatch(1000);
		}
		shell.dispose();
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
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// ignore
				}
			} else
				break;
		} while (true);
	}

	private Composite rectangleComposite(final Composite parent, final int x, final int y) {
		return new Composite(parent, SWT.NONE) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return new Point(x, y);
			}
		};
	}

	private Composite createClient() {
		Composite client = rectangleComposite(sec, 100, 100);
		client.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		sec.setClient(client);
		return client;
	}

	private Composite createTextClient(int w, int h) {
		Composite textClient = rectangleComposite(sec, w, h);
		textClient.setBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
		sec.setTextClient(textClient);
		return textClient;
	}

	private Composite createSeparator(int w) {
		Composite sep = rectangleComposite(sec, w, 20);
		sep.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		sec.setSeparatorControl(sep);
		return sep;
	}

	private Composite createDescriptionControl(int w, int h) {
		Composite sep = rectangleComposite(sec, w, 20);
		sep.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		sec.setDescriptionControl(sep);
		return sep;
	}

	private void createSection(String text, int flags) {
		sec = new Section(shell, flags);
		sec.setText(text);
		sec.setBackground(display.getSystemColor(SWT.COLOR_RED));
		sec.addExpansionListener(new IExpansionListener() {

			@Override
			public void expansionStateChanging(ExpansionEvent e) {

			}

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				sec.getParent().layout(true);
			}
		});
	}

	private Rectangle update() {
		sec.redraw();
		shell.redraw();
		shell.layout(true, true);
		shell.update();
		if (humanWatching)
			dispatch(300);
		else
			dispatch();
		secbounds = sec.getBounds();

		return secbounds;
	}

	@Test
	public void testSectionNoClient() {
		createSection(TEXT1, TITLE_TWIST);
		Rectangle bounds1 = update();
		sec.setExpanded(true);
		Rectangle bounds2 = update();
		assertEquals(bounds1, bounds2);

		createSection(TEXT1, Section.TWISTIE);

		Rectangle bounds3 = update();
		assertTrue(bounds3.width < bounds2.width);
	}

	@Test
	public void testSectionWithClient() {
		createSection(TEXT1, TITLE_TWIST);
		createClient();
		Rectangle bounds1 = update();
		sec.setExpanded(true);
		Rectangle bounds2 = update();
		// our client is 100 tall + ver spacing
		bounds2.height -= 100 + sec.clientVerticalSpacing;

		assertEquals(bounds1, bounds2);

		createSection(TEXT1, Section.TWISTIE);
		createClient();

		Rectangle bounds3 = update();
		assertTrue(bounds3.width < bounds2.width);

	}

	@Test
	public void testSectionWithClientAndCompact() {
		createSection(TEXT1, TITLE_TWIST);
		// no client
		Rectangle bounds1 = update();

		createSection(TEXT1, TITLE_TWIST | Section.COMPACT);
		createClient(); // add client
		Rectangle bounds2 = update();

		assertTrue(bounds1.width == bounds2.width);

		sec.setExpanded(true); // now it should be bigger
		Rectangle bounds3 = update();
		assertTrue(bounds3.width > bounds2.width);
	}

	@Test
	public void testSectionWithAndWithoutClientCompact() {
		createSection(TEXT1, TITLE_TWIST | Section.COMPACT);
		// no client
		Rectangle bounds1 = update();

		createClient(); // client
		Rectangle bounds2 = update();

		assertEquals(bounds1, bounds2);
	}

	@Test
	public void testSectionWithTextClient() {
		int fontSize = getTextExtend(TEXT1).y;
		final int SMALL_BOX_H = 3;
		final int BIG_BOX_H = fontSize * 2;
		final int BIG_W = 80;
		createSection(TEXT1, TITLE_TWIST);
		Rectangle bounds1 = update();

		// text client height less then text height
		createSection(TEXT1, TITLE_TWIST);
		createTextClient(BIG_W, SMALL_BOX_H);
		Rectangle bounds2 = update();
		assertTrue(bounds2.width >= bounds1.width + BIG_W);
		assertTrue(bounds2.height == bounds1.height);

		// text client height more then text height
		createSection(TEXT1, TITLE_TWIST);
		createTextClient(BIG_W, BIG_BOX_H);
		Rectangle bounds3 = update();
		assertTrue(bounds2.width == bounds3.width);
		assertTrue(bounds3.height >= BIG_BOX_H);

		// text client height more then text height, left alignment
		createSection(TEXT1, TITLE_TWIST | Section.LEFT_TEXT_CLIENT_ALIGNMENT);
		createTextClient(BIG_W, BIG_BOX_H);
		Rectangle bounds3l = update();
		assertTrue(bounds2.width == bounds3l.width);
		assertTrue(bounds3l.height >= BIG_BOX_H);

		// no title
		createSection(TEXT1, TITLE_TWIST | Section.NO_TITLE);
		Rectangle bounds4 = update();
		assertTrue(bounds4.width < bounds1.width);
		assertTrue(bounds4.height < bounds1.height);

		// pure only toggle header
		createSection(TEXT1, Section.TWISTIE | Section.NO_TITLE);
		Rectangle boundsToggle = update();
		assertTrue(boundsToggle.width > 0);
		assertTrue(boundsToggle.height > 0);

		createSection(TEXT1, Section.TWISTIE | Section.NO_TITLE);
		createTextClient(BIG_W, SMALL_BOX_H); // text client is small
		Rectangle bounds5 = update();
		assertTrue(bounds5.width >= boundsToggle.width + BIG_W);
		assertTrue(bounds5.height == boundsToggle.height);

		createSection(TEXT1, Section.TWISTIE | Section.NO_TITLE | Section.LEFT_TEXT_CLIENT_ALIGNMENT);
		createTextClient(BIG_W, SMALL_BOX_H); // text client is small
		Rectangle bounds5l = update();
		assertTrue(bounds5l.width >= boundsToggle.width + BIG_W);
		assertTrue(bounds5l.height == boundsToggle.height);

		createSection(TEXT1, Section.TWISTIE | Section.NO_TITLE);
		createTextClient(BIG_W, BIG_BOX_H); // text client bigger then font size
											// and toggle
		Rectangle bounds6 = update();
		assertEquals(BIG_BOX_H, bounds6.height);

		sec.setExpanded(true);
		Rectangle bounds7 = update();
		assertEquals(bounds6, bounds7);

		// no toggle
		createSection(TEXT1, Section.NO_TITLE);
		createTextClient(BIG_W, BIG_BOX_H);

		Rectangle bounds8 = update();
		assertEquals(BIG_BOX_H, bounds8.height);
		assertEquals(BIG_W + 4, bounds8.width); // +4 maybe a bug
	}

	@Test
	public void testSectionWithTextSeparator() {
		createSection(TEXT1, Section.TWISTIE);
		createSeparator(10);
		checkSeparator();
		sec.setExpanded(true);
		checkSeparator();

		// with client
		createSection(TEXT1, Section.TWISTIE);
		createSeparator(10);
		createClient();
		checkSeparator();
		sec.setExpanded(true);
		checkSeparator();

		// with client and description
		createSection(TEXT1, Section.TWISTIE);
		createSeparator(10);
		createDescriptionControl(50, 20);
		createClient();
		checkSeparator();
		sec.setExpanded(true);
		update();
		Rectangle bounds = sec.getBounds();
		Rectangle sepBounds = sec.getSeparatorControl().getBounds();
		assertTrue(sepBounds.width == bounds.width);

		Rectangle cb = sec.getClient().getBounds();
		Rectangle db = sec.getDescriptionControl().getBounds();
		assertEquals(bounds.height - db.height - 3 - cb.height - sec.clientVerticalSpacing,
				sepBounds.y + sepBounds.height);

	}

	@Test
	public void testUpdateTitleOnPaint() {
		createSection(TEXT1, Section.TITLE_BAR);
		update();
		Image imageBackgroundBefore = sec.getBackgroundImage();
		sec.setTitleBarBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
		update();
		Image imageBackgroundAfter = sec.getBackgroundImage();
		assertNotEquals("Backgroundimage has not changed, although we have changed the TitleBarBackground",
				imageBackgroundBefore, imageBackgroundAfter);
	}

	@Test
	public void testBackgroundImageCaching() {
		createSection(TEXT1, Section.TITLE_BAR);
		update();
		sec.setTitleBarBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
		Image imageBackgroundBefore = sec.getBackgroundImage();
		sec.setTitleBarBorderColor(sec.getTitleBarBackground());
		update();
		Image imageBackgroundAfter = sec.getBackgroundImage();
		shell.setSize(600, 400);
		assertEquals("Backgroundimage has changed, although the TitleBar was not changed", imageBackgroundBefore,
				imageBackgroundAfter);
	}

	private void checkSeparator() {
		update();
		Rectangle bounds = sec.getBounds();
		Rectangle sepBounds = sec.getSeparatorControl().getBounds();
		assertTrue(sepBounds.width == bounds.width);
		if (sec.isExpanded() && sec.getClient() != null) {
			Rectangle cb = sec.getClient().getBounds();
			assertEquals(bounds.height - cb.height - sec.clientVerticalSpacing, sepBounds.y + sepBounds.height);
		} else
			assertEquals(bounds.height, sepBounds.y + sepBounds.height);
	}
}
