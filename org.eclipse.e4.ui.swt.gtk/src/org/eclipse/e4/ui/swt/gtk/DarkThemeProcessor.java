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
package org.eclipse.e4.ui.swt.gtk;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

@SuppressWarnings("restriction")
public class DarkThemeProcessor {

	@Inject
	IEclipseContext context;

	private EventHandler eventHandler;

	@PostConstruct
	public void intialize(IEclipseContext context) {
		IEventBroker eventBroker = context.get(IEventBroker.class);

		eventHandler = new EventHandler() {

			@Override
			public void handleEvent(final Event event) {
				if (event == null)
					return;
				ITheme theme = (ITheme) event.getProperty("theme");
				final boolean isDark = theme.getId().contains("dark"); //$NON-NLS-1$
				Display display = (Display) event.getProperty(IThemeEngine.Events.DEVICE);

				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						OS.gdk_flush();
						OS.g_object_set(OS.gtk_settings_get_default(), "gtk-application-prefer-dark-theme".getBytes(), //$NON-NLS-1$
								isDark, 0);
						OS.g_object_notify(OS.gtk_settings_get_default(),
								"gtk-application-prefer-dark-theme".getBytes());
					}
				});
			}
		};
		eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED, eventHandler);
	}

	@PreDestroy
	public void cleanUp(IEclipseContext context) {
		IEventBroker eventBroker = context.get(IEventBroker.class);
		eventBroker.unsubscribe(eventHandler);
	}

}
