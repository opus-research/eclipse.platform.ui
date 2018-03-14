/*******************************************************************************
 * Copyright (c) 2015 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wayne Beaton (The Eclipse Foundation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.IQuickAccessElement;
import org.eclipse.ui.quickaccess.QuickAccessMatch;

public class ExtensionQuickAccessElement extends QuickAccessElement {
	private IQuickAccessElement element;

	/**
	 * @param provider
	 * @param element
	 */
	public ExtensionQuickAccessElement(ExtensionQuickAccessProvider provider, IQuickAccessElement element) {
		super(provider);
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.quickaccess.IQuickAccessElement#getLabel()
	 */
	@Override
	public String getLabel() {
		return getElement().getLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.quickaccess.IQuickAccessElement#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		ImageDescriptor descriptor = getElement().getImageDescriptor();
		if (descriptor != null)
			return descriptor;
		return getProvider().getImageDescriptor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.quickaccess.IQuickAccessElement#getId()
	 */
	@Override
	public String getId() {
		return getElement().getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.quickaccess.IQuickAccessElement#execute()
	 */
	@Override
	public void execute() {
		getElement().execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.quickaccess.IQuickAccessElement#getSortLabel()
	 */
	@Override
	public String getSortLabel() {
		return getElement().getSortLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.quickaccess.IQuickAccessElement#match(java.lang.String,
	 * org.eclipse.ui.quickaccess.IQuickAccessProvider)
	 */
	@Override
	public QuickAccessEntry match(String filter, QuickAccessProvider providerForMatching) {
		if (providerForMatching != getProvider())
			return null;

		QuickAccessMatch match = getElement().match(filter,
				((ExtensionQuickAccessProvider) providerForMatching).getProvider());
		if (match == null)
			return null;

		return new QuickAccessEntry(this, providerForMatching, match.elementMatchRegions, match.providerMatchRegions,
				match.getMatchQuality());
	}

	public IQuickAccessElement getElement() {
		return element;
	}

}