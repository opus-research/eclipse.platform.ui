/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.IQuickAccessElement;
import org.eclipse.ui.quickaccess.IQuickAccessProvider;
import org.eclipse.ui.quickaccess.QuickAccessMatch;

/**
 * @since 3.3
 * 
 */
public abstract class QuickAccessElement implements IQuickAccessElement {

	static final String separator = " - "; //$NON-NLS-1$

	private static final int[][] EMPTY_INDICES = new int[0][0];
	private IQuickAccessProvider provider;

	/**
	 * @param provider
	 */
	public QuickAccessElement(IQuickAccessProvider provider) {
		super();
		this.provider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#getLabel()
	 */
	@Override
	public abstract String getLabel();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#getImageDescriptor()
	 */
	@Override
	public abstract ImageDescriptor getImageDescriptor();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#getId()
	 */
	@Override
	public abstract String getId();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#execute()
	 */
	@Override
	public abstract void execute();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#getSortLabel()
	 */
	@Override
	public String getSortLabel() {
		return getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#getProvider()
	 */
	@Override
	public IQuickAccessProvider getProvider() {
		return provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.quickaccess.IQuickAccessElement#match(java.lang.String, org.eclipse.ui.quickaccess.IQuickAccessProvider)
	 */
	@Override
	public QuickAccessMatch match(String filter,
			IQuickAccessProvider providerForMatching) {
		String sortLabel = getLabel();
		int index = sortLabel.toLowerCase().indexOf(filter);
		if (index != -1) {
			int quality = sortLabel.toLowerCase().equals(filter) ? QuickAccessMatch.MATCH_PERFECT : (sortLabel
					.toLowerCase().startsWith(filter) ? QuickAccessMatch.MATCH_EXCELLENT
 : QuickAccessMatch.MATCH_GOOD);
			return new QuickAccessMatch(this, providerForMatching,
					new int[][] { { index, index + filter.length() - 1 } },
 EMPTY_INDICES, quality);
		}
		String combinedLabel = (providerForMatching.getName() + " " + getLabel()); //$NON-NLS-1$
		index = combinedLabel.toLowerCase().indexOf(filter);
		if (index != -1) {
			int lengthOfElementMatch = index + filter.length()
					- providerForMatching.getName().length() - 1;
			if (lengthOfElementMatch > 0) {
				return new QuickAccessMatch(this, providerForMatching,
						new int[][] { { 0, lengthOfElementMatch - 1 } },
 new int[][] { { index,
 index + filter.length() - 1 } }, QuickAccessMatch.MATCH_GOOD);
			}
			return new QuickAccessMatch(this, providerForMatching,
					EMPTY_INDICES, new int[][] { { index,
					index + filter.length() - 1 } }, QuickAccessMatch.MATCH_GOOD);
		}
		String camelCase = CamelUtil.getCamelCase(sortLabel);
		index = camelCase.indexOf(filter);
		if (index != -1) {
			int[][] indices = CamelUtil.getCamelCaseIndices(sortLabel, index, filter
					.length());
			return new QuickAccessMatch(this, providerForMatching, indices,
 EMPTY_INDICES,
 QuickAccessMatch.MATCH_GOOD);
		}
		String combinedCamelCase = CamelUtil.getCamelCase(combinedLabel);
		index = combinedCamelCase.indexOf(filter);
		if (index != -1) {
			String providerCamelCase = CamelUtil.getCamelCase(providerForMatching
					.getName());
			int lengthOfElementMatch = index + filter.length()
					- providerCamelCase.length();
			if (lengthOfElementMatch > 0) {
				return new QuickAccessMatch(
						this,
						providerForMatching,
						CamelUtil.getCamelCaseIndices(sortLabel, 0, lengthOfElementMatch),
						CamelUtil.getCamelCaseIndices(providerForMatching.getName(),
 index,
								filter.length() - lengthOfElementMatch),
 QuickAccessMatch.MATCH_GOOD);
			}
			return new QuickAccessMatch(this, providerForMatching,
					EMPTY_INDICES, CamelUtil.getCamelCaseIndices(providerForMatching
.getName(), index,
 filter.length()), QuickAccessMatch.MATCH_GOOD);
		}
		return null;
	}
}
