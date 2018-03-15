/*******************************************************************************
 * Copyright (c) 2016 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Richard <axel.richard@obeo.fr>
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.AutoSavePreferencePage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @since 3.11
 *
 */
public class AutoSavePreferencePageTest {

	static PreferenceDialog dialog;
	@BeforeClass
	public static void setUpClass() {
		setupPreferenceManager();
		dialog = setupDialog();
		dialog.open();
	}

	@AfterClass
	public static void tearDownClass() {
		dialog.close();
		tearDownPreferenceManager();
	}

	@After
	public void restoreAndCheckDefaults() {
		MyAutoSavePreferencePage page = (MyAutoSavePreferencePage) dialog.getSelectedPage();
		page.performDefaults();
		page.performOk();
		boolean autoSave = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
		Assert.assertFalse(autoSave);
		Assert.assertFalse(page.isAutoSaveButtonSelected());
		int autoSaveInterval = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		Assert.assertEquals(60, autoSaveInterval);
		Assert.assertEquals(60, page.getAutoSaveIntervalTextValue());
	}

	@Test
	public void testEnableAutoSave() {
		MyAutoSavePreferencePage page = (MyAutoSavePreferencePage) dialog.getSelectedPage();
		page.selectAutoSaveButton(true);
		boolean autoSave = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
		Assert.assertTrue(autoSave);
		Assert.assertTrue(page.isAutoSaveButtonSelected());
		page.performOk();
	}

	@Test
	public void testDisableAutoSave() {
		MyAutoSavePreferencePage page = (MyAutoSavePreferencePage) dialog.getSelectedPage();
		page.selectAutoSaveButton(false);
		boolean autoSave = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
		Assert.assertFalse(autoSave);
		Assert.assertFalse(page.isAutoSaveButtonSelected());
		page.performOk();
	}

	@Test
	public void testUpdateAutoSaveInterval() {
		MyAutoSavePreferencePage page = (MyAutoSavePreferencePage) dialog.getSelectedPage();
		page.selectAutoSaveButton(true);
		page.setAutoSaveIntervalTextValue(30);
		int autoSaveInterval = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		// the interval preference in preference store is only updated after
		// performOk, so the value must be 60 before the perform and 30 after
		// the perform.
		Assert.assertEquals(60, autoSaveInterval);
		Assert.assertEquals(30, page.getAutoSaveIntervalTextValue());
		page.performOk();
		autoSaveInterval = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		Assert.assertEquals(30, autoSaveInterval);
	}

	@Test
	public void testUpdateAutoSaveIntervalWithOutOfScopeValue() {
		MyAutoSavePreferencePage page = (MyAutoSavePreferencePage) dialog.getSelectedPage();
		page.selectAutoSaveButton(true);
		page.setAutoSaveIntervalTextValue(0);
		int autoSaveInterval = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		// the value must be 60 before the perform and 60 after
		// the perform, because the value to set is out of scope
		Assert.assertEquals(60, autoSaveInterval);
		Assert.assertEquals(0, page.getAutoSaveIntervalTextValue());
		Assert.assertFalse(page.isValid());
		page.performOk();
		autoSaveInterval = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		Assert.assertEquals(60, autoSaveInterval);
	}

	@Test
	public void testCheckDefaultAutoSaveConfiguration() {
		boolean autoSave = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
		Assert.assertFalse(autoSave);
		int autoSaveInterval = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getInt(IDEInternalPreferences.SAVE_AUTOMATICALLY_INTERVAL);
		Assert.assertEquals(60, autoSaveInterval);
	}

	private static void setupPreferenceManager() {
		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();

		IPreferencePage page = new MyAutoSavePreferencePage();
		PreferenceNode node = new PreferenceNode("org.eclipse.ui.preferencePages.AutoSaveTest", "Auto-save", null,
				MyAutoSavePreferencePage.class.getName());
		node.setPage(page);
		manager.addToRoot(node);
	}

	private static void tearDownPreferenceManager() {
		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();

		manager.remove("org.eclipse.ui.preferencePages.AutoSaveTest");
	}

	private static PreferenceDialog setupDialog() {
		PreferenceDialog dial = new PreferenceDialog(Display.getCurrent().getActiveShell(),
				PlatformUI.getWorkbench().getPreferenceManager());
		dial.setSelectedNode("org.eclipse.ui.preferencePages.AutoSaveTest");
		dial.setBlockOnOpen(false);

		return dial;
	}

	private static class MyAutoSavePreferencePage extends AutoSavePreferencePage {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.ui.internal.ide.dialogs.AutoSavePreferencePage#
		 * isAutoSaveButtonSelected()
		 */
		@Override
		public boolean isAutoSaveButtonSelected() {
			return super.isAutoSaveButtonSelected();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.ui.internal.ide.dialogs.AutoSavePreferencePage#
		 * selectAutoSaveButton(boolean)
		 */
		@Override
		public void selectAutoSaveButton(boolean enable) {
			super.selectAutoSaveButton(enable);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.ui.internal.ide.dialogs.AutoSavePreferencePage#
		 * getAutoSaveIntervalTextValue()
		 */
		@Override
		public int getAutoSaveIntervalTextValue() {
			return super.getAutoSaveIntervalTextValue();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.ui.internal.ide.dialogs.AutoSavePreferencePage#
		 * setAutoSaveIntervalTextValue(int)
		 */
		@Override
		public void setAutoSaveIntervalTextValue(int interval) {
			super.setAutoSaveIntervalTextValue(interval);
		}

	}
}
