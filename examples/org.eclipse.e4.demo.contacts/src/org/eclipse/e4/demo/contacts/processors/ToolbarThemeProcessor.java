/*******************************************************************************
 * Copyright (c) 2010, 2012 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 *     Lars Vogel <lars.vogel@gmail.com> - Bug 413431, 416166
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.processors;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.demo.contacts.util.Util;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;

public class ToolbarThemeProcessor extends Util {

	@Inject
	@Named("toolbar:org.eclipse.ui.main.toolbar")
	private MToolBar toolbar;

	private final static String PROCESSOR_ID = "org.eclipse.e4.demo.contacts.processor.toolbar";

	@SuppressWarnings("restriction")
	@Execute
	public void execute(MApplication app, EModelService service,
			IExtensionRegistry registery, IThemeManager mgr) {
		if (toolbar == null) {
			return;
		}

		List<String> tags = app.getTags();
		for (String tag : tags) {
			if (PROCESSOR_ID.equals(tag)) {
				return; // already processed
			}
		}
		tags.add(PROCESSOR_ID);

		IThemeEngine engine = mgr.getEngineForDisplay(Display.getCurrent());
		List<ITheme> themes = engine.getThemes();
		if (themes.size() > 0) {

			MCommand switchThemeCommand = null;
			for (MCommand cmd : app.getCommands()) {
				if ("contacts.switchTheme".equals(cmd.getElementId())) { //$NON-NLS-1$
					switchThemeCommand = cmd;
					break;
				}
			}

			if (switchThemeCommand != null) {

				toolbar.getChildren().add(
						MMenuFactory.INSTANCE.createToolBarSeparator());

				for (ITheme theme : themes) {
					MParameter parameter = MCommandsFactory.INSTANCE
							.createParameter();
					parameter.setName("contacts.commands.switchtheme.themeid"); //$NON-NLS-1$
					parameter.setValue(theme.getId());
					String iconURI = getCSSUri(theme.getId(), registery);
					if (iconURI != null) {
						iconURI = iconURI.replace(".css", ".png");
					}
					processTheme(theme.getLabel(), switchThemeCommand,
							parameter, iconURI, service);
				}

			}
		}
	}

	protected void processTheme(String name, MCommand switchCommand,
			MParameter themeId, String iconURI, EModelService service) {
		MHandledToolItem toolItem = service
				.createModelElement(MHandledToolItem.class);
		toolItem.setTooltip(name);
		toolItem.setCommand(switchCommand);
		toolItem.getParameters().add(themeId);
		if (iconURI != null) {
			toolItem.setIconURI(iconURI);
		}
		toolbar.getChildren().add(toolItem);
	}

}
