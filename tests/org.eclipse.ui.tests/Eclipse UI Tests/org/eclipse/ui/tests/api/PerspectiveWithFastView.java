/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Denis Zygann <d.zygann@web.de> - Bug 457390
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This perspective is used for testing api. It defines an initial layout with a
 * fast view.
 * @deprecated discontinued support for fast views
 */
@Deprecated
public class PerspectiveWithFastView implements IPerspectiveFactory {
    @Deprecated
    public static String PERSP_ID = "org.eclipse.ui.tests.fastview_perspective"; //$NON-NLS-1$

    /**
     * Constructs a new Default layout engine.
     */
    public PerspectiveWithFastView() {
        super();
    }

    @Override
	public void createInitialLayout(IPageLayout layout) {
        defineLayout(layout);
    }

    /**
     * Define the initial layout by adding a fast view.
     *
     * @param layout
     *            The page layout.
     * @deprecated discontinued support for fast views
     */
    @Deprecated
	public void defineLayout(IPageLayout layout) {
        // not supported anymore
    }
}
