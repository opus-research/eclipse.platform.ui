/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.propertysheet;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test areas of the Property Sheet API.
 */
@RunWith(org.junit.runners.AllTests.class)
public class PropertySheetTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new PropertySheetTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public PropertySheetTestSuite() {
        addTest(new TestSuite(PropertyShowInContextTest.class));
        addTest(new TestSuite(MultiInstancePropertySheetTest.class));
        addTest(new TestSuite(ShowInPropertySheetTest.class));
        addTest(new TestSuite(NewPropertySheetHandlerTest.class));
        addTest(new TestSuite(PropertySheetAuto.class));
        addTest(new TestSuite(ComboBoxPropertyDescriptorTest.class));
    }
}
