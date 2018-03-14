/*******************************************************************************
 * Copyright (c) 2015 BestSolution.at Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.commands.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * provides an {@link IEclipseContext} who is the root-context of the appliction or a context above
 * it
 */
public interface IContextProvider {
	/**
	 * @return the context or <code>null</code>
	 */
	public IEclipseContext getContext();
}
