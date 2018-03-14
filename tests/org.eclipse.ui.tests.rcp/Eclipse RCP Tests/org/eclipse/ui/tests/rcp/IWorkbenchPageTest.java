/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.rcp.util.EmptyView;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

/**
 * Tests the behaviour of various IWorkbenchPage methods under different
 * workbench configurations.
 */
public class IWorkbenchPageTest extends TestCase {

    public IWorkbenchPageTest(String name) {
        super(name);
    }

    private Display display = null;

    protected void setUp() throws Exception {
        super.setUp();

        assertNull(display);
        display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

    protected void tearDown() throws Exception {
        assertNotNull(display);
        display.dispose();
        assertTrue(display.isDisposed());

        super.tearDown();
    }

    /**
     * Regression test for Bug 70080 [RCP] Reset Perspective does not work if no
     * perspective toolbar shown (RCP).
     */
    public void test70080() {
        WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

            public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
                super.preWindowOpen(configurer);
                configurer.setShowPerspectiveBar(false);
            }

            public void postStartup() {
                try {
                    IWorkbenchWindow window = getWorkbenchConfigurer()
                            .getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage();
                    page.showView(EmptyView.ID);
                    assertNotNull(page.findView(EmptyView.ID));
                    page.resetPerspective();
                    assertNull(page.findView(EmptyView.ID));
                } catch (PartInitException e) {
                    fail(e.toString());
                }
            }
        };
        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);
    }

	/**
	 * Regression test for Bug 382625. The perspectives stored in the
	 * preferences as
	 * {@link IWorkbenchPreferenceConstants#PERSPECTIVE_BAR_EXTRAS} are put into
	 * the perspective stack
	 */
	public void testPerspectiveBarExtrasGetOpened() {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver#preStartup
			 * ()
			 */
			public void preStartup() {
				super.preStartup();
				PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS,
						"org.eclipse.debug.ui.DebugPerspective,org.eclipse.jdt.ui.JavaBrowsingPerspective");
			}

			public void postStartup() {
				super.postStartup();
				IWorkbenchPage activePage = getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow()
						.getActivePage();
				IPerspectiveDescriptor[] openPerspectives = activePage.getOpenPerspectives();
				assertEquals(3, openPerspectives.length);
				assertEquals(openPerspectives[1].getId(), "org.eclipse.debug.ui.DebugPerspective");
				assertEquals(openPerspectives[2].getId(), "org.eclipse.jdt.ui.JavaBrowsingPerspective");
			}

		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}

}
