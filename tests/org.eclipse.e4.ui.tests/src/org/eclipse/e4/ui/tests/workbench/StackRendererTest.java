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

import junit.framework.TestCase;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class StackRendererTest extends TestCase {
	private StackRendererTestable renderer;
	private Shell shell;
	private MPart part;

	@Override
	protected void setUp() throws Exception {
		shell = Display.getDefault().getActiveShell();
		renderer = new StackRendererTestable();
		part = MBasicFactory.INSTANCE.createPart();
	}

	public void testUpdateTabWhenStackBusyEvent() throws Exception {
		final boolean[] executionMarker = new boolean[] { false };

		new StackRendererTestable() {
			@Override
			public void updateBusyIndicator(CTabItem cti, MPart part,
					boolean busy) {
				executionMarker[0] = true;
			}
		}.updateTab(null, null, UIEvents.UILifeCycle.BUSY, false);

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
		part.getTags().remove(UIEvents.UILifeCycle.BUSY); /* state idle */

		renderer.updateBusyIndicator(tabItem, part, true);

		assertTrue(part.getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertTrue(renderer.setCSSInfoCalled);
		assertTrue(renderer.reapplyStylesCalled);

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyIdleAndNextIdleEvent()
			throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		part.getTags().remove(UIEvents.UILifeCycle.BUSY); /* state idle */

		renderer.updateBusyIndicator(tabItem, part, false);

		assertFalse(part.getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertFalse(renderer.setCSSInfoCalled);
		assertFalse(renderer.reapplyStylesCalled);

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyBusy() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		part.getTags().add(UIEvents.UILifeCycle.BUSY); /* state busy */

		renderer.updateBusyIndicator(tabItem, part, false);

		assertFalse(part.getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertTrue(renderer.setCSSInfoCalled);
		assertTrue(renderer.reapplyStylesCalled);

		tabItem.dispose();
	}

	public void testUpdateBusyIndicatorWhenCurrentlyBusyAndNextBusyEvent()
			throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		part.getTags().add(UIEvents.UILifeCycle.BUSY); /* state busy */

		renderer.updateBusyIndicator(tabItem, part, true);

		assertTrue(part.getTags().contains(UIEvents.UILifeCycle.BUSY));
		assertFalse(renderer.setCSSInfoCalled);
		assertFalse(renderer.reapplyStylesCalled);

		tabItem.dispose();
	}

	private static class StackRendererTestable extends StackRenderer {
		public boolean setCSSInfoCalled;
		public boolean reapplyStylesCalled;

		@Override
		public void updateTab(CTabItem cti, MPart part, String attName,
				Object newValue) {
			super.updateTab(cti, part, attName, newValue);
		}

		@Override
		public void updateBusyIndicator(CTabItem cti, MPart part, boolean busy) {
			super.updateBusyIndicator(cti, part, busy);
		}

		@Override
		public void setCSSInfo(MUIElement me, Object widget) {
			setCSSInfoCalled = true;
		}

		@Override
		protected void reapplyStyles(Widget widget) {
			reapplyStylesCalled = true;
		}
	}
}
