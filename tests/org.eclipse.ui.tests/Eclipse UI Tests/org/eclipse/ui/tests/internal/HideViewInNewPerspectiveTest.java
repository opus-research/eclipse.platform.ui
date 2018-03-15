/*******************************************************************************
 * Copyright (c) 2017 Simeon Andreev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class HideViewInNewPerspectiveTest extends UITestCase {

	public static String TEST_PERSPECTIVE_ID = "org.eclipse.ui.tests.internal.HideViewInNewPerspectiveTest.TestPerspective";
	public static String ACTIVE_VIEW_ID = "org.eclipse.ui.tests.internal.HideViewInNewPerspectiveTest.TestViewActive";
	public static String INACTIVE_VIEW_ID = "org.eclipse.ui.tests.internal.HideViewInNewPerspectiveTest.TestViewInactive";

	public static class TestPerspective implements IPerspectiveFactory {

		@Override
		public void createInitialLayout(IPageLayout layout) {
			String editorArea = layout.getEditorArea();
			IFolderLayout folder = layout.createFolder("tabs", IPageLayout.LEFT, .75f, editorArea);
			folder.addView(ACTIVE_VIEW_ID);
			folder.addView(INACTIVE_VIEW_ID);
		}
	}

	public static class TestView extends ViewPart {

		@Override
		public void createPartControl(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(getSite().getId());
		}

		@Override
		public void setFocus() {
			// nothing to do
		}
	}

	public HideViewInNewPerspectiveTest(String testName) {
		super(testName);
	}


	public void testInactiveViewWithViewReference() throws Exception {
		IWorkbenchWindow activeWorkbenchWindow = openTestWindow();
		IWorkbench workbench = activeWorkbenchWindow.getWorkbench();
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

		workbench.showPerspective(TEST_PERSPECTIVE_ID, activeWorkbenchWindow);
		processUiEvents();

		IViewReference inactiveViewReference = activePage.findViewReference(INACTIVE_VIEW_ID);
		assertNotNull(inactiveViewReference);

		activePage.hideView(inactiveViewReference);
		processUiEvents();

		inactiveViewReference = activePage.findViewReference(INACTIVE_VIEW_ID);
		assertNull(inactiveViewReference);
	}

	private void processUiEvents() {
		while (fWorkbench.getDisplay().readAndDispatch()) {
		}
	}
}
