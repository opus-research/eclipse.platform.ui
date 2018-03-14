/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import org.eclipse.ui.IMemento;

/**
 * @since 3.5
 *
 */
public interface IMementoReaderFactory {

	WorkbenchMementoReader createWorkbenchReader(IMemento workbenchMem);

	WindowReader createWindowReader(IMemento windowMem);

	PerspectiveReader createPerspectiveReader(IMemento perspectiveMem);

	InfoReader createInfoReader(IMemento infoMem);

}
