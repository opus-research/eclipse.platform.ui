/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Schwarz - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.filter;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * A filter that allows to restrict the creation and rendering of the
 * implementation of a {@link MPart}. Further it allows to restrict the
 * displaying of a View in the QuickAccess and the ShowView Dialog.
 *
 * @implement This filter is intended to be implemented by clients
 * @since 1.5
 */
public interface IPartFilter {

	/**
	 * Checks if the view associated to the given MPart should be displayed and
	 * rendered.
	 *
	 * @param part
	 *            the part that should be analyzed.
	 * @return <b>true</b> if the part should not be shown otherwise <b>false</b>.
	 */
	public boolean filterPart(MPart part);

	/**
	 * Checks if the view associated to the given MPartDescriptor should be shown in
	 * the QuickAccess and the ShowView Dialog.
	 *
	 * @param partDescriptor
	 *            the part descriptor that should be analyzed.
	 * @return <b>true</b> if the part should not be shown otherwise <b>false</b>.
	 */
	public boolean filterPart(MPartDescriptor partDescriptor);

}
