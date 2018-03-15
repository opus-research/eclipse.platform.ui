/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *     Axel Richard <axel.richard@obeo.fr> - Bug 486644
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import org.eclipse.ui.tests.propertyPages.PropertyPageEnablementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for preferences.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	FontPreferenceTestCase.class,
	DeprecatedFontPreferenceTestCase.class,
	ScopedPreferenceStoreTestCase.class,
	WorkingCopyPreferencesTestCase.class,
	PropertyPageEnablementTest.class,
	ListenerRemovalTestCase.class,
	PreferencesDialogTest.class,
	ZoomAndPreferencesFontTest.class,
	AutoSavePreferencePageTest.class})
public class PreferencesTestSuite {
}
