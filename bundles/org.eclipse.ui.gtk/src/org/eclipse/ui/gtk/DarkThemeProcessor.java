/*******************************************************************************
 * Copyright (c) 2015 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.gtk;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

@SuppressWarnings("restriction")
public class DarkThemeProcessor {

	@Inject
	IEclipseContext context;

	@PostConstruct
	public void pc(IEclipseContext context, final UISynchronize sync) {
		IEventBroker eventBroker = context.get(IEventBroker.class);

		eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED, new EventHandler() {

			@Override
			public void handleEvent(final Event event) {
				if (event == null)
					return;
				ITheme theme = (ITheme) event.getProperty("theme");
				boolean contains = theme.getId().contains("dark"); //$NON-NLS-1$
				OS.g_object_set(OS.gtk_settings_get_default(), "gtk-application-prefer-dark-theme".getBytes(), contains, //$NON-NLS-1$
						0);
				OS.g_object_notify(OS.gtk_settings_get_default(), "gtk-application-prefer-dark-theme".getBytes());
			}
		});
	}

}
