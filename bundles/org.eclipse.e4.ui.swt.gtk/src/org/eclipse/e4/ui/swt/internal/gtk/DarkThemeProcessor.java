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
package org.eclipse.e4.ui.swt.internal.gtk;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.Converter;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class DarkThemeProcessor {

	@Inject
	IEventBroker eventBroker;

	private EventHandler eventHandler;

	@PostConstruct
	public void intialize() {

		eventHandler = new EventHandler() {

			@Override
			public void handleEvent(final Event event) {
				if (event == null)
					return;
				ITheme theme = (ITheme) event.getProperty("theme");
				final boolean isDark = theme.getId().contains("dark"); //$NON-NLS-1$
				final Display display = (Display) event.getProperty(IThemeEngine.Events.DEVICE);

				// not using UISynchronize as this is specific to SWT/GTK
				// scenarios
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						OS.setDarkThemePreferred(isDark);
						long /*int*/ screen = OS.gdk_screen_get_default();
						long /*int*/ provider = OS.gtk_css_provider_new();
						if (screen != 0 && provider != 0) {
							Color color;
							if (isDark) {
								color = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
							} else {
								color = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
							}
							String css = "GtkMenuItem {color: rgb(" + color.getRed() + ", " + 
									color.getGreen() + ", " + color.getBlue() + ");}";
							OS.gtk_style_context_add_provider_for_screen (screen, provider, OS.GTK_STYLE_PROVIDER_PRIORITY_APPLICATION);
							OS.gtk_css_provider_load_from_data (provider, Converter.wcsToMbcs (null, css, true), -1, null);
						}
					}
				});
			}
		};
		// using the IEventBroker explicitly because the @EventTopic annotation
		// is unpredictable with processors within the debugger
		eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED, eventHandler);
	}

	@PreDestroy
	public void cleanUp() {
		eventBroker.unsubscribe(eventHandler);
	}

}
