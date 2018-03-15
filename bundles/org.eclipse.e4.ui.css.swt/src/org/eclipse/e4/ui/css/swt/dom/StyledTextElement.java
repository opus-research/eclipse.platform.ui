/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.dom.scrollbar.StyledTextThemedScrollBarAdapter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.log.LogService;
import org.osgi.service.prefs.Preferences;

public class StyledTextElement extends CompositeElement {

	private SyncScrollBarThemedPreference fSyncScrollBarThemedPreference;

	public StyledTextElement(Composite composite, CSSEngine engine) {
		super(composite, engine);
	}

	public StyledText getStyledText() {
		return (StyledText) getControl();
	}

	private StyledTextThemedScrollBarAdapter getScrollbarAdapter() {
		return StyledTextThemedScrollBarAdapter.getScrollbarAdapter(getStyledText());
	}

	public void setScrollBarBackgroundColor(Color newColor) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarBackgroundColor(newColor);
		}
	}

	public void setScrollBarForegroundColor(Color newColor) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarForegroundColor(newColor);
		}
	}

	public void setScrollBarWidth(int width) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarWidth(width);
		}
	}

	public void setMouseNearScrollScrollBarWidth(int width) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setMouseNearScrollScrollBarWidth(width);
		}
	}

	public void setVerticalScrollBarVisible(boolean visible) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setVerticalScrollBarVisible(visible);
		}
	}

	public void setHorizontalScrollBarVisible(boolean visible) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setHorizontalScrollBarVisible(visible);
		}
	}

	public void setScrollBarBorderRadius(int radius) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarBorderRadius(radius);
		}
	}

	private void setScrollBarThemed(boolean themed) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarThemed(themed);
		}
	}

	@Override
	public void reset() {
		super.reset();
		// Default is not having the scroll bar themed.
		disposePreferenceChangeListener();
		setScrollBarThemed(false);
	}

	@Override
	public void dispose() {
		super.dispose();
		disposePreferenceChangeListener();
	}

	/**
	 * @param cssText
	 *            either "true" or "false" to set it to a value or a preference
	 *            key having a boolean value in the format:
	 *            preference:bundle.qualifier.id/key
	 *
	 *            Note that the user may also set
	 *            -Dswt.enable.themedScrollBar=true/false to ignore the
	 *            preference value and force it to true/false regardless of the
	 *            preference value.
	 */
	public void setScrollBarThemed(String cssText) {
		String value = System.getProperty("swt.enable.themedScrollBar"); //$NON-NLS-1$
		if (value != null) {
			if ("true".equalsIgnoreCase(value)) {
				setScrollBarThemed(true);
			} else {
				setScrollBarThemed(false);
			}
			disposePreferenceChangeListener();

		} else if ("true".equalsIgnoreCase(cssText)) { //$NON-NLS-1$
			setScrollBarThemed(true);
			disposePreferenceChangeListener();

		} else if ("false".equalsIgnoreCase(cssText)) { //$NON-NLS-1$
			setScrollBarThemed(false);
			disposePreferenceChangeListener();

		} else if (cssText.startsWith("preference:")) { //$NON-NLS-1$
			cssText = cssText.substring("preference:".length()); //$NON-NLS-1$
			int index;
			if ((index = cssText.indexOf('/')) != -1) {
				String qualifier = cssText.substring(0, index);
				String key = cssText.substring(index + 1);
				keepPreferenceSynchronized(qualifier, key);
				return;
			}
			// If it didn't return, dispose the old listener
			disposePreferenceChangeListener();

		} else {
			CSSActivator.getDefault().log(LogService.LOG_WARNING,
					"Don't know how to handle setting value: " + cssText //$NON-NLS-1$
					+ " (supported: boolean or preference:bundle.qualifier.id/key)."); //$NON-NLS-1$
		}

	}

	private void disposePreferenceChangeListener() {
		if (fSyncScrollBarThemedPreference != null) {
			fSyncScrollBarThemedPreference.dispose();
			fSyncScrollBarThemedPreference = null;
		}
	}

	/**
	 * Responsible for keeping whether the scrollbar should be themed
	 * synchronized with a preference key.
	 */
	private static class SyncScrollBarThemedPreference implements IPreferenceChangeListener {

		public final String qualifier;
		public final String key;
		private StyledTextElement styledTextElement;

		public SyncScrollBarThemedPreference(String qualifier, String key, StyledTextElement styledTextElement) {
			this.qualifier = qualifier;
			this.key = key;
			this.styledTextElement = styledTextElement;

			IEclipsePreferences node = InstanceScope.INSTANCE.getNode(qualifier);
			boolean setThemed = node.getBoolean(key, false);
			this.styledTextElement.setScrollBarThemed(setThemed);
			node.addPreferenceChangeListener(this);
		}

		public void dispose() {
			IEclipsePreferences node = InstanceScope.INSTANCE.getNode(qualifier);
			node.removePreferenceChangeListener(this);
		}

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			Preferences node = event.getNode();
			StyledText styledText = this.styledTextElement.getStyledText();
			if (styledText.isDisposed()) {
				this.styledTextElement.disposePreferenceChangeListener();
			} else {
				this.styledTextElement.setScrollBarThemed(node.getBoolean(key, false));
			}
		}
	}

	private void keepPreferenceSynchronized(final String qualifier, String key) {
		if (fSyncScrollBarThemedPreference != null) {
			if (!fSyncScrollBarThemedPreference.qualifier.equals(qualifier)
					|| !fSyncScrollBarThemedPreference.key.equals(key)) {
				disposePreferenceChangeListener();
				// Keep on going
			} else {
				return; // It's still the same qualifier/key for the
				// preferences.
			}
		}

		if (fSyncScrollBarThemedPreference == null) {
			fSyncScrollBarThemedPreference = new SyncScrollBarThemedPreference(qualifier, key, this);
			StyledText styledText = this.getStyledText();
			if (!styledText.isDisposed()) {
				styledText.addDisposeListener(new DisposeListener() {

					@Override
					public void widgetDisposed(DisposeEvent e) {
						disposePreferenceChangeListener();
					}
				});
			}
		}
	}
}
