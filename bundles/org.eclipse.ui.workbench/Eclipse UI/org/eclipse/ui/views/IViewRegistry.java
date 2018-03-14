/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 473845
 *******************************************************************************/
package org.eclipse.ui.views;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.ui.part.ViewPart;

/**
 * The view registry maintains a list of views explicitly registered
 * against the view extension point..
 * <p>
 * The description of a given view is kept in a <code>IViewDescriptor</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.views.IViewDescriptor
 * @see org.eclipse.ui.views.IStickyViewDescriptor
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewRegistry {

	/**
	 * This constant is used as key for persisting the original class for a
	 * legacy {@link ViewPart} in the persisted state of a
	 * {@link MPartDescriptor}.
	 *
	 * @since 3.108
	 */
	public static final String ORIGINAL_COMPATIBILITY_VIEW_CLASS = "originalCompatibilityViewClass"; //$NON-NLS-1$

	/**
	 * This constant is used as key for persisting the original bundle for a
	 * legacy {@link ViewPart} in the persisted state of a
	 * {@link MPartDescriptor}.
	 *
	 * @since 3.108
	 */
	public static final String ORIGINAL_COMPATIBILITY_VIEW_BUNDLE = "originalCompatibilityViewBundle"; //$NON-NLS-1$

    /**
     * Return a view descriptor with the given extension id.  If no view exists
     * with the id return <code>null</code>.
     * Will also return <code>null</code> if the view descriptor exists, but
     * is filtered by an expression-based activity.
     *
     * @param id the id to search for
     * @return the descriptor or <code>null</code>
     */
    public IViewDescriptor find(String id);

    /**
     * Returns an array of view categories.
     *
     * @return the categories.  Never <code>null</code>.
     */
    public IViewCategory[] getCategories();

    /**
     * Return a list of views defined in the registry.
     *
     * @return the views.  Never <code>null</code>.
     */
    public IViewDescriptor[] getViews();

    /**
     * Return a list of sticky views defined in the registry.
     *
     * @return the sticky views.  Never <code>null</code>.
     */
    public IStickyViewDescriptor[] getStickyViews();
}
