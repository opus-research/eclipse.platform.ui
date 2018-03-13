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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Widget;

public class StackRendererTest extends TestCase {
	private StackRendererTestable renderer;

	@Override
	public void setUp() throws Exception {
		renderer = new StackRendererTestable();
	}

	public void testUpdateTabWhenStackBusyEvent() throws Exception {
		renderer.updateTab(null, null, UIEvents.UILifeCycle.BUSY, false);

		assertTrue(renderer.executedMethods.contains("setCSSInfo"));
		assertTrue(renderer.executedMethods.contains("reapplyStyles"));
	}

	public void testUpdateTabWhenOtherStackEvent() throws Exception {
		renderer.updateTab(null, null, "notSupportedAttributeName", false);

		assertFalse(renderer.executedMethods.contains("setCSSInfo"));
		assertFalse(renderer.executedMethods.contains("reapplyStyles"));
	}

	// helper functions
	private static class StackRendererTestable extends StackRenderer {
		List<String> executedMethods = new ArrayList<String>();

		@Override
		public void setCSSInfo(MUIElement me, Object widget) {
			registerExecutedMethod();
		}

		@Override
		protected void reapplyStyles(Widget widget) {
			registerExecutedMethod();
		}

		@Override
		public void updateTab(CTabItem cti, MPart part, String attName,
				Object newValue) {
			super.updateTab(cti, part, attName, newValue);
		}

		private void registerExecutedMethod() {
			StackTraceElement[] elements = Thread.currentThread()
					.getStackTrace();
			for (int i = 0; i < elements.length; i++) {
				if ("registerExecutedMethod"
						.equals(elements[i].getMethodName())) {
					executedMethods.add(elements[i + 1].getMethodName());
					break;
				}
			}
		}
	}
}
