/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenBrowserHandler extends AbstractHandler {

	private static final String PARAM_ID_URL = "url"; //$NON-NLS-1$

	private static final String PARAM_ID_BROWSER_ID = "browserId"; //$NON-NLS-1$

	private static final String PARAM_ID_NAME = "name"; //$NON-NLS-1$

	private static final String PARAM_ID_TOOLTIP = "tooltip"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String urlText = event.getParameter(PARAM_ID_URL);
		final URL url;
		if (urlText == null) {
			url = null;
		} else {
			try {
				url = new URL(urlText);
			} catch (MalformedURLException ex) {
				throw new ExecutionException("malformed URL:" + urlText, ex); //$NON-NLS-1$
			}
		}

		final String browserId = event.getParameter(PARAM_ID_BROWSER_ID);
		final String name = event.getParameter(PARAM_ID_NAME);
		final String tooltip = event.getParameter(PARAM_ID_TOOLTIP);
		final IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench()
					.getBrowserSupport();
		Shell shell = HandlerUtil.getActiveShell(event);
		Display display = shell == null ? Display.getCurrent() : shell.getDisplay();
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					IWebBrowser browser = browserSupport.createBrowser(
							IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, browserId,
							name, tooltip);
					browser.openURL(url);
				} catch (PartInitException ex) {
					throw new RuntimeException("error opening browser", ex); //$NON-NLS-1$
				}
			}
		});

		return null;
	}
}
