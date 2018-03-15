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

import java.util.ArrayList;
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
import org.eclipse.ui.internal.WorkbenchMessages;

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
	public void fill(Menu parent, int index) {
		if (engine == null) {
			return;
		}
		// theme menu visible when engine available
		MenuItem themeMenuItem = new MenuItem(parent, SWT.CASCADE, index);
		themeMenuItem.setText(WorkbenchMessages.MenuAppearanceTheme_label);
		themeMenuItem.setToolTipText(WorkbenchMessages.MenuAppearanceTheme_tooltip);
		Menu themeMenu = new Menu(themeMenuItem);
		themeMenuItem.setMenu(themeMenu);
		// add a menu item for each theme in the sorted list
		ArrayList<ITheme> themes = new ArrayList<>(engine.getThemes());
		themes.sort((ITheme t1, ITheme t2) -> t1.getLabel().compareTo(t2.getLabel()));
		for (ITheme theme : themes) {
			if (!highContrastMode && !Util.isGtk() && theme.getId().equals(E4Application.HIGH_CONTRAST_THEME_ID)) {
				continue;
			}
			MenuItem menuItem = new MenuItem(themeMenu, SWT.CHECK);
			menuItem.setText(theme.getLabel());
			menuItem.setData(THEME_ID, theme.getId());
			// select and activate theme
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					engine.setTheme(theme, !highContrastMode);
				}
			});
		}
		// select and enable are consistent with selection
		themeMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				for (MenuItem item : themeMenu.getItems()) {
					boolean isActive = item.getData(THEME_ID).equals(engine.getActiveTheme().getId());
					item.setEnabled(!isActive);
					item.setSelection(isActive);
				}
			}
		});
	}

}
