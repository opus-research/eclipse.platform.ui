/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.util.HashSet;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.junit.Assert;

/**
 * The DuplicateMenuItemTest is a class for testing the popups
 * and window menus for the navigator to check for duplicate menu
 * entries.
 */
public class DuplicateMenuItemTest extends AbstractNavigatorTest {

    /**
     * Constructor for DuplicateMenuItemTest.
     * @param testName
     */
    public DuplicateMenuItemTest(String testName) {
        super(testName);
    }

    public void testSelection() {

        IStructuredSelection selection = new StructuredSelection(testProject);
        checkSelection(selection);
        selection = new StructuredSelection(testFolder);
        checkSelection(selection);
        selection = new StructuredSelection(testFile);
        checkSelection(selection);
    }

    private void checkMenu(Menu menu, String menuName) {

        MenuItem[] items = menu.getItems();
        HashSet labels = new HashSet();
        for (int i = 0; i < items.length; i++) {
            String label = items[i].getText();
            System.out.println(label);
            Assert.assertTrue("Duplicate menu entry in: " + menuName + " "
                    + label, !labels.contains(label));
            if (items[i].getMenu() != null)
                checkMenu(items[i].getMenu(), label);
        }

    }

    private void checkWorkbenchMenu() {

        MenuManager workbenchManager = ((WorkbenchWindow) navigator
                .getViewSite().getWorkbenchWindow()).getMenuManager();

        checkMenu(workbenchManager.getMenu(), "Workbench");
    }

    private void checkSelection(IStructuredSelection selection) {
        navigator.selectReveal(selection);
        checkWorkbenchMenu();
    }

    /**
     * Sets up the hierarchy.
     */
    @Override
	protected void doSetUp() throws Exception {
        super.doSetUp();
        createTestFile();
        showNav();
    }

}
