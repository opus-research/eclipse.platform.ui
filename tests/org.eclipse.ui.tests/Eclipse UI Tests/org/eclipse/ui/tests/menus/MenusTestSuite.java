/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Axel Richard <axel.richard@obeo.fr> - Bug 392457
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for all code related to menus. This includes the
 * <code>popupMenus</code> extension point, and others.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		DynamicToolbarTest.class })
		/*
		 * TODO: MenusTestSuite was previously disabled in UiTestSuite and these
		 * commented-out tests must be validated and fixed up
		 */
		// ObjectContributionTest.class,
		// MenuVisibilityTest.class,
		// MenuBaseTests.class,
		// MenuPopulationTest.class,
		// DynamicMenuTest.class,
		// Bug231304Test.class,
		// ShowViewMenuTest.class,
		// Bug264804Test.class,
		// MenuHelperTest.class,
		// Bug410426Test.class,
public class MenusTestSuite {
}
