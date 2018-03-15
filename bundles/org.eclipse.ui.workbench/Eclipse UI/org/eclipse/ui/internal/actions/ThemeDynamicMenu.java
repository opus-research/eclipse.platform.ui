package org.eclipse.ui.internal.actions;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbench;

/**
 * Implements the Theme Dynamic Menu.
 */
public class ThemeDynamicMenu extends ContributionItem {

	private IThemeEngine engine;
	private String defaultTheme;
	private boolean highContrastMode;

	/**
	 *
	 */
	@Inject
	public ThemeDynamicMenu(IWorkbench workbench) {
		MApplication application = workbench.getService(MApplication.class);
		IEclipseContext context = application.getContext();
		defaultTheme = (String) context.get(E4Application.THEME_ID);
		engine = context.get(IThemeEngine.class);
		defaultTheme = (String) context.get(E4Application.THEME_ID);
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
			menuItem.setEnabled(!defaultTheme.equals(theme.getId()));
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					engine.setTheme(theme, !highContrastMode);
				}
			});
		}
	}

}
