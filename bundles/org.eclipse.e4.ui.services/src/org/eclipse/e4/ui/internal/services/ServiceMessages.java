/**********************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.e4.ui.internal.services;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ServiceMessages {

	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.internal.services.serviceMessages"; //$NON-NLS-1$
	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	// Event broker
	public static final String NO_EVENT_ADMIN = "NO_EVENT_ADMIN";
	public static final String NO_BUNDLE_CONTEXT = "NO_BUNDLE_CONTEXT";

	/**
	 * Reload the ResourceBundle for the current set default locale.
	 */
	public static void reloadMessages() {
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	}

	/**
	 * Return the localized message for the given key and the given placeholder
	 * replacements.
	 *
	 * @param key
	 *            The key of the message to return.
	 *            <ul>
	 *            <li>{@link #NO_BUNDLE_CONTEXT}</li>
	 *            <li>{@link #NO_EVENT_ADMIN}</li>
	 *            </ul>
	 * @param args
	 *            objects that are used to format/replace placeholder in the
	 *            message.
	 * @return the localized message for the given key formatted with the given
	 *         objects.
	 */
	public static String getMessage(String key, Object... args) {
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		}
	}

}