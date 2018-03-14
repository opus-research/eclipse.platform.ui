/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;

/**
 * @since 3.7
 *
 */
class ApplicationMenuManager extends MenuManager {

	private final Menu appMenu;
	private boolean disposing;

	public ApplicationMenuManager(Menu appMenu) {
		this.appMenu = appMenu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.MenuManager#createMenuBar(org.eclipse.swt.widgets
	 * .Decorations)
	 */
	@Override
	public Menu createMenuBar(Decorations parent) {
		return appMenu;
	}

	@Override
	protected boolean menuExist() {
		// our menu always exist,
		// except disposing - if not dispose will be called on this menu
		return !disposing;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.MenuManager#getMenuItemCount()
	 */
	@Override
	protected int getMenuItemCount() {
		return appMenu.getItemCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.MenuManager#getMenuItem(int)
	 */
	@Override
	protected Item getMenuItem(int index) {
		return appMenu.getItem(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.MenuManager#getMenuItems()
	 */
	@Override
	protected Item[] getMenuItems() {
		return appMenu.getItems();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.MenuManager#doItemFill(org.eclipse.jface.action
	 * .IContributionItem, int)
	 */
	@Override
	protected void doItemFill(IContributionItem ci, int index) {
		ci.fill(appMenu, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.MenuManager#dispose()
	 */
	@Override
	public void dispose() {
		disposing = true;
		super.dispose();
		disposing = false;
	}
}
