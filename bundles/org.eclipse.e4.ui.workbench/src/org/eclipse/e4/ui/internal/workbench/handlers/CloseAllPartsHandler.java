/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.handlers;

/**
 * Handle the closing of all parts
 */
public class CloseAllPartsHandler extends AbstractCloseFileHandler {

	/**
	 * Configure the close handler
	 */
	public CloseAllPartsHandler() {
		super(true, true);
	}

}
