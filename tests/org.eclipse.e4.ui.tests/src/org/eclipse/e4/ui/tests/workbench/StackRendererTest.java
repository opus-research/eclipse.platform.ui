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

import static org.eclipse.e4.ui.css.swt.CSSConstants.CSS_BUSY_CLASS;
import static org.eclipse.e4.ui.css.swt.CSSSWTConstants.CSS_CLASS_NAME_KEY;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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

		new StackRendererTestable() {
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

		new StackRendererTestable() {
			@Override
			public void updateBusyIndicator(CTabItem cti, MPart part,
					boolean busy) {
				executionMarker[0] = true;
			}
		}.updateTab(null, null, "notSupportedAttributeName", false);

		assertFalse(executionMarker[0]);
	}

	public void testUpdateBusyIndicatorWhenCurrentlyIdle() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "" /* state idle */);

		renderer.updateBusyIndicator(tabItem, part, true);

		assertNotNull(tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(CSS_BUSY_CLASS, tabItem.getData(CSS_CLASS_NAME_KEY));

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyBusy() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, " " + CSS_BUSY_CLASS /* state busy */);

		renderer.updateBusyIndicator(tabItem, part, false);

		assertNull(tabItem.getData(CSS_CLASS_NAME_KEY));

		tabItem.dispose();
	}

	private static class StackRendererTestable extends StackRenderer {
		@Override
		public void updateTab(CTabItem cti, MPart part, String attName,
				Object newValue) {
			super.updateTab(cti, part, attName, newValue);
		}

		@Override
		public void updateBusyIndicator(CTabItem cti, MPart part, boolean busy) {
			super.updateBusyIndicator(cti, part, busy);
		}
	}
}
