/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.eclipse.e4.ui.css.swt.CSSSWTConstants.CSS_CLASS_NAME_KEY;
import static org.eclipse.e4.ui.css.swt.CSSSWTConstants.CTABITEM_BUSY_CLASSNAME;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StackRendererTest extends TestCase {
	private StackRendererTestable renderer;
	private Shell shell;
	private MPart part;
	private IEclipseContext context;

	@Override
	protected void setUp() throws Exception {
		shell = Display.getDefault().getActiveShell();
		renderer = new StackRendererTestable();

		context = EclipseContextFactory.create();
		context.set(CSSRenderingUtils.class, new CSSRenderingUtils());

		part = (MPart) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { MPart.class }, new InvocationHandler() {
					public Object invoke(Object obj, Method method,
							Object[] args) throws Throwable {
						if ("getContext".equals(method.getName())) {
							return context;
						}
						return null;
					}
				});

	}

	public void testUpdateTabWhenStackBusyEvent() throws Exception {
		final boolean[] executionMarker = new boolean[] { false };

		new StackRenderer() {
			@Override
			public void updateTab(CTabItem cti, MPart part, String attName,
					Object newValue) {
				super.updateTab(cti, part, attName, newValue);
			}

			@Override
			public void updateBusyIndicator(CTabItem cti, MPart part,
					boolean busy) {
				executionMarker[0] = true;
			}
		}.updateTab(null, null, UIEvents.UILabel.BUSY, false);

		assertTrue(executionMarker[0]);
	}

	public void testUpdateTabWhenOtherStackEvent() throws Exception {
		final boolean[] executionMarker = new boolean[] { false };

		new StackRenderer() {
			@Override
			public void updateTab(CTabItem cti, MPart part, String attName,
					Object newValue) {
				super.updateTab(cti, part, attName, newValue);
			}

			@Override
			public void updateBusyIndicator(CTabItem cti, MPart part,
					boolean busy) {
				executionMarker[0] = true;
			}
		}.updateTab(null, null, "notSupportedAttributeName", false);

		assertFalse(executionMarker[0]);
	}

	public void testUpdateBusyIndicatorWhenCurrentlyIdleAndStylingByCSS()
			throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "" /* state idle */);

		renderer.updateBusyIndicator(tabItem, part, true);

		assertNotNull(tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(" " + CTABITEM_BUSY_CLASSNAME,
				tabItem.getData(CSS_CLASS_NAME_KEY));

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyBusyAndStylingByCSS()
			throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, " " + CTABITEM_BUSY_CLASSNAME /* busy */);

		renderer.updateBusyIndicator(tabItem, part, false);

		assertNotNull(tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals("", tabItem.getData(CSS_CLASS_NAME_KEY));

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyIdleAndDirectSwtStyling()
			throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setFont(new Font(shell.getDisplay(), "Arial", 19, SWT.NORMAL /* idle */));

		renderer.updateBusyIndicator(tabItem, part, true);
		FontData[] fontData = tabItem.getFont().getFontData();

		assertNull(tabItem.getData(CSSSWTConstants.CSS_CLASS_NAME_KEY));
		assertEquals(1, fontData.length);
		assertEquals(SWT.ITALIC, fontData[0].getStyle() & SWT.ITALIC);
		assertEquals("Arial", fontData[0].getName());
		assertEquals(19, fontData[0].getHeight());

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyBusyAndDirectSwtStyling()
			throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setFont(new Font(shell.getDisplay(), "Times", 21, SWT.ITALIC /* busy */));

		renderer.updateBusyIndicator(tabItem, part, false);
		FontData[] fontData = tabItem.getFont().getFontData();

		assertNull(tabItem.getData(CSSSWTConstants.CSS_CLASS_NAME_KEY));
		assertEquals(1, fontData.length);
		assertEquals(SWT.NORMAL, fontData[0].getStyle() & SWT.NORMAL);
		assertEquals("Times", fontData[0].getName());
		assertEquals(21, fontData[0].getHeight());

		tabItem.dispose();
	}

	public void testCreateFontWithItalicStyle() throws Exception {
		Font font = renderer.createFont(new Font(shell.getDisplay(), "Arial",
				12, SWT.NORMAL), SWT.ITALIC);

		assertNotNull(font);
		assertEquals(1, font.getFontData().length);
		assertEquals("Arial", font.getFontData()[0].getName());
		assertEquals(12, font.getFontData()[0].getHeight());
		assertEquals(SWT.ITALIC, font.getFontData()[0].getStyle() & SWT.ITALIC);

		font.dispose();
	}

	public void testCreateFontWithNormalStyle() throws Exception {
		Font font = renderer.createFont(new Font(shell.getDisplay(), "Arial",
				12, SWT.ITALIC), SWT.NORMAL);

		assertNotNull(font);
		assertEquals(1, font.getFontData().length);
		assertEquals("Arial", font.getFontData()[0].getName());
		assertEquals(12, font.getFontData()[0].getHeight());
		assertEquals(SWT.NORMAL, font.getFontData()[0].getStyle() & SWT.NORMAL);

		font.dispose();
	}

	private static class StackRendererTestable extends StackRenderer {
		@Override
		public Font createFont(Font font, int style) {
			return super.createFont(font, style);
		}

		@Override
		public void updateBusyIndicator(CTabItem cti, MPart part, boolean busy) {
			super.updateBusyIndicator(cti, part, busy);
		}
	}
}
