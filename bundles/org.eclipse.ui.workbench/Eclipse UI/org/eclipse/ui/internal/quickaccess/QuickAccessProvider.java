/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.IQuickAccessElement;
import org.eclipse.ui.quickaccess.IQuickAccessProvider;

/**
 * @since 3.3
 * 
 */
public abstract class QuickAccessProvider implements IQuickAccessProvider {

	private IQuickAccessElement[] sortedElements;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#getId()
	 */
	@Override
	public abstract String getId();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#getName()
	 */
	@Override
	public abstract String getName();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#getImageDescriptor()
	 */
	@Override
	public abstract ImageDescriptor getImageDescriptor();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#getElements()
	 */
	@Override
	public abstract IQuickAccessElement[] getElements();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#getElementsSorted()
	 */
	@Override
	public IQuickAccessElement[] getElementsSorted() {
		if (sortedElements == null) {
			sortedElements = getElements();
			Arrays.sort(sortedElements, new Comparator<IQuickAccessElement>() {
				@Override
				public int compare(IQuickAccessElement e1, IQuickAccessElement e2) {
					return e1.getSortLabel().compareTo(e2.getSortLabel());
				}
			});
		}
		return sortedElements;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#getElementForId(java.lang.String)
	 */
	@Override
	public abstract IQuickAccessElement getElementForId(String id);

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#isAlwaysPresent()
	 */
	@Override
	public boolean isAlwaysPresent() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessProvider#reset()
	 */
	@Override
	public void reset() {
		sortedElements = null;
		doReset();
	}

	protected abstract void doReset();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.quickaccess.IQuickAccessProvider#setContext(org.eclipse
	 * .e4.core.contexts.IEclipseContext)
	 */
	@Override
	public void setContext(IEclipseContext context) {
	}
}
