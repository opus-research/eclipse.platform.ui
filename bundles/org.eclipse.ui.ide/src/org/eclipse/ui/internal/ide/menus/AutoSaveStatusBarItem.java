/*******************************************************************************
 * Copyright (c) 2016 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Axel Richard <axel.richard@obeo.fr> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.menus;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * Auto-save item for the Eclipse Status Bar. A double-click on this item allows
 * to quickly enabled/disabled auto-save for editors.
 *
 * @since 3.12
 *
 */
public class AutoSaveStatusBarItem extends WorkbenchWindowControlContribution {

	private ToolBar toolBar;

	private ToolItem toolItem;

	private IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (toolItem != null && !toolItem.isDisposed()
					&& event.getProperty() == IDEInternalPreferences.SAVE_AUTOMATICALLY) {
				String autoSaveState = event.getNewValue().toString();
				if (Boolean.valueOf(autoSaveState)) {
					toolItem.setText(IDEWorkbenchMessages.AutoSaveStatusBarItem_on);
				} else {
					toolItem.setText(IDEWorkbenchMessages.AutoSaveStatusBarItem_off);
				}
				toolBar.pack();
			}
		}
	};

	/**
	 * Default constructor.
	 */
	public AutoSaveStatusBarItem() {
	}

	/**
	 * @param id
	 *            The id of this contribution
	 */
	public AutoSaveStatusBarItem(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		toolBar = new ToolBar(parent, SWT.HORIZONTAL);
		toolItem = new ToolItem(toolBar, SWT.PUSH);
		boolean autoSave = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
		if (autoSave) {
			toolItem.setText(IDEWorkbenchMessages.AutoSaveStatusBarItem_on);
		} else {
			toolItem.setText(IDEWorkbenchMessages.AutoSaveStatusBarItem_off);
		}
		toolItem.setToolTipText(IDEWorkbenchMessages.AutoSaveStatusBarItem_tooltip);
		toolBar.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				// Nothing to do here.
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// Nothing to do here.
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
				boolean autoSaveState = store.getBoolean(IDEInternalPreferences.SAVE_AUTOMATICALLY);
				if (autoSaveState) {
					store.setValue(IDEInternalPreferences.SAVE_AUTOMATICALLY, false);
				} else {
					store.setValue(IDEInternalPreferences.SAVE_AUTOMATICALLY, true);
				}

			}
		});

		IDEWorkbenchPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
		toolBar.pack();
		return toolBar;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	@Override
	public void dispose() {
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
		toolItem.dispose();
		toolBar.dispose();
		super.dispose();
	}
}
