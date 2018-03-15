/*******************************************************************************
 * Copyright (c) 2007, 2017 Patrik Suzzi.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrik Suzzi <psuzzi@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Implements the Dynamic Menu to choose the Theme.
 */
public class ThemeDynamicMenu extends ContributionItem {

	private static String THEME_ID = "THEME_ID"; //$NON-NLS-1$

	private IThemeEngine engine;
	private boolean highContrastMode;

	public ThemeDynamicMenu() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		MApplication application = workbench.getService(MApplication.class);
		IEclipseContext context = application.getContext();
		engine = context.get(IThemeEngine.class);
		highContrastMode = workbench.getDisplay().getHighContrast();
	}

	@Override
	public void fill(Menu menu, int index) {
		for (ITheme theme : engine.getThemes()) {
			if (!highContrastMode && !Util.isGtk() && theme.getId().equals(E4Application.HIGH_CONTRAST_THEME_ID)) {
				continue;
			}
			MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
			menuItem.setText(theme.getLabel());
			menuItem.setData(THEME_ID, theme.getId());
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					engine.setTheme(theme, !highContrastMode);
				}
			});
		}

		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				for (MenuItem item : menu.getItems()) {
					boolean isActive = item.getData(THEME_ID).equals(engine.getActiveTheme().getId());
					item.setEnabled(!isActive);
					item.setSelection(isActive);
				}
			}
		});

	}

}
