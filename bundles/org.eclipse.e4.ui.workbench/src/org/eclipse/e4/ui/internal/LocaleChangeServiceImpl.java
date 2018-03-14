/*******************************************************************************
 * Copyright (c) 2013, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Fabian Miehe - Bug 440435
 *******************************************************************************/
package org.eclipse.e4.ui.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.osgi.service.log.LogService;

/**
 * Default implementation of {@link ILocaleChangeService} that changes the {@link Locale} in the
 * specified {@link IEclipseContext} and additionally fires an event on the event bus.
 *
 * @author Dirk Fauth
 *
 */
public class LocaleChangeServiceImpl implements ILocaleChangeService {

	MApplication application;

	@Inject
	IEventBroker broker;

	@Inject
	@Optional
	LogService logService;

	/**
	 * Create a new {@link LocaleChangeServiceImpl} for the given {@link IEclipseContext}.
	 *
	 * @param application
	 *            The application to retrieve the context from.
	 */
	@Inject
	public LocaleChangeServiceImpl(MApplication application) {
		this.application = application;
	}

	@Override
	public void changeApplicationLocale(Locale locale) {

		// the TranslationService.LOCALE context parameter is specified as String
		// so we put the String representation of the given Locale to the context
		this.application.getContext().set(TranslationService.LOCALE, locale);

		// update model
		updateLocalization(this.application.getChildren());

		// fire event
		broker.post(LOCALE_CHANGE, locale);
	}

	@Override
	public void changeApplicationLocale(String localeString) {
		try {
			Locale locale = toLocale(localeString);

			// set the locale to the application context
			// use the resolved locale instead of the given locale string to avoid invalid locales
			// in context
			this.application.getContext().set(TranslationService.LOCALE, locale);

			// update model
			updateLocalization(this.application.getChildren());

			// fire event
			broker.post(LOCALE_CHANGE, locale);
		} catch (Exception e) {
			// performing a locale update failed
			// there is no locale change performed
			if (logService != null)
				logService.log(LogService.LOG_ERROR, e.getMessage()
						+ " - No Locale change will be performed."); //$NON-NLS-1$
		}
	}

	/**
	 * Will iterate over the given list of {@link MUIElement}s and inform them about the Locale
	 * change if necessary.
	 *
	 * @param children
	 *            The list of {@link MUIElement}s that should be checked for Locale updates.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateLocalization(List<? extends MUIElement> children) {
		for (MUIElement element : children) {
			if (element instanceof MElementContainer) {
				updateLocalization(((MElementContainer) element).getChildren());
			}

			if (element instanceof MWindow) {
				MWindow window = (MWindow) element;
				MMenu mainMenu = window.getMainMenu();
				if (mainMenu != null) {
					mainMenu.updateLocalization();
					updateLocalization(mainMenu.getChildren());
				}
				updateLocalization(window.getSharedElements());
				updateLocalization(window.getWindows());
			}

			if (element instanceof MTrimmedWindow) {
				for (MTrimBar trimBar : ((MTrimmedWindow) element).getTrimBars()) {
					trimBar.updateLocalization();
					updateLocalization(trimBar.getChildren());
				}
			}

			if (element instanceof MPerspective) {
				updateLocalization(((MPerspective) element).getWindows());
			}

			if (element instanceof MPart) {
				MPart mPart = (MPart) element;
				MToolBar toolbar = mPart.getToolbar();
				if (toolbar != null && toolbar.getChildren() != null) {
					toolbar.updateLocalization();
					updateLocalization(toolbar.getChildren());
				}
			}

			((MLocalizable) element).updateLocalization();
		}
	}

	/**
	 * <p>
	 * Converts a String to a Locale.
	 * </p>
	 *
	 * <p>
	 * This method takes the string format of a locale and creates the locale
	 * object from it.
	 * </p>
	 *
	 * <pre>
	 *   MessageFactoryServiceImpl.toLocale("en")         = new Locale("en", "")
	 *   MessageFactoryServiceImpl.toLocale("en_GB")      = new Locale("en", "GB")
	 *   MessageFactoryServiceImpl.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")
	 * </pre>
	 *
	 * <p>
	 * This method validates the input strictly. The language code must be
	 * lowercase. The country code must be uppercase. The separator must be an
	 * underscore. The length must be correct.
	 * </p>
	 *
	 * <p>
	 * This method is inspired by
	 * <code>org.apache.commons.lang.LocaleUtils.toLocale(String)</code> by
	 * fixing the parsing error for uncommon Locales like having a language and
	 * a variant code but no country code, or a Locale that only consists of a
	 * country code.
	 * </p>
	 *
	 * @param str
	 *            the locale String to convert
	 * @return a Locale that matches the specified locale String. If the given
	 *         input String is <code>null</code> or can not be parsed because of
	 *         an invalid format, {@link Locale#getDefault()} will be returned.
	 */
	public Locale toLocale(String str) {
		return toLocale(str, Locale.getDefault());
	}

	/**
	 * <p>
	 * Converts a String to a Locale.
	 * </p>
	 *
	 * <p>
	 * This method takes the string format of a locale and creates the locale
	 * object from it.
	 * </p>
	 *
	 * <pre>
	 *   MessageFactoryServiceImpl.toLocale("en")         = new Locale("en", "")
	 *   MessageFactoryServiceImpl.toLocale("en_GB")      = new Locale("en", "GB")
	 *   MessageFactoryServiceImpl.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")
	 * </pre>
	 *
	 * <p>
	 * This method validates the input strictly. The language code must be
	 * lowercase. The country code must be uppercase. The separator must be an
	 * underscore. The length must be correct.
	 * </p>
	 *
	 * <p>
	 * This method is inspired by
	 * <code>org.apache.commons.lang.LocaleUtils.toLocale(String)</code> by
	 * fixing the parsing error for uncommon Locales like having a language and
	 * a variant code but no country code, or a Locale that only consists of a
	 * country code.
	 * </p>
	 *
	 * <p>
	 * <b>Note:</b> This is the same logic as used in
	 * <code>EquinoxConfiguration.toLocale()</code>
	 * </p>
	 *
	 * @param localeString
	 *            the locale String to convert
	 * @param defaultLocale
	 *            the Locale that should be returned in case of an invalid
	 *            Locale String
	 * @return a Locale that matches the specified locale String. If the given
	 *         input String is <code>null</code> or can not be parsed because of
	 *         an invalid format, the given default {@link Locale} will be
	 *         returned.
	 */
	private Locale toLocale(String localeString, Locale defaultLocale) {
		if (localeString == null) {
			if (logService != null)
				logService.log(LogService.LOG_ERROR,
						"Given locale String is null" + " - Default Locale will be used instead."); //$NON-NLS-1$//$NON-NLS-2$
			return defaultLocale;
		}

		String language = ""; //$NON-NLS-1$
		String country = ""; //$NON-NLS-1$
		String variant = ""; //$NON-NLS-1$

		String[] localeParts = localeString.split("_"); //$NON-NLS-1$
		if (localeParts.length == 0 || localeParts.length > 3
				|| (localeParts.length == 1 && localeParts[0].length() == 0)) {
			logInvalidFormat(localeString, logService);
			return defaultLocale;
		}

		if (localeParts[0].length() > 0 && !localeParts[0].matches("[a-zA-Z]{2,8}")) { //$NON-NLS-1$
			logInvalidFormat(localeString, logService);
			return defaultLocale;
		}

		language = localeParts[0];

		if (localeParts.length > 1) {
			if (localeParts[1].length() > 0 && !localeParts[1].matches("[a-zA-Z]{2}|[0-9]{3}")) { //$NON-NLS-1$
				if (language.length() > 0) {
					if (logService != null)
						logService.log(LogService.LOG_ERROR, "Invalid locale format: " + localeString //$NON-NLS-1$
								+ " - Only language part will be used to create the Locale."); //$NON-NLS-1$
					return new Locale(language);
				}
				logInvalidFormat(localeString, logService);
				return defaultLocale;
			}

			country = localeParts[1];
		}

		if (localeParts.length == 3) {
			if (localeParts[2].length() == 0) {
				if (logService != null)
					logService.log(LogService.LOG_ERROR, "Invalid locale format: " + localeString //$NON-NLS-1$
							+ " - Only language and country part will be used to create the Locale."); //$NON-NLS-1$
				return new Locale(language, country);
			}
			variant = localeParts[2];
		}

		return new Locale(language, country, variant);
	}

	private static HashSet<String> invalidLocalesLogged = new HashSet<>();

	static void logInvalidFormat(String str, LogService logService) {
		if (logService != null && !invalidLocalesLogged.contains(str)) {
			invalidLocalesLogged.add(str);
			logService.log(LogService.LOG_ERROR, "Invalid locale format: " + str //$NON-NLS-1$
					+ " - Default Locale will be used instead."); //$NON-NLS-1$
		}
	}
}
