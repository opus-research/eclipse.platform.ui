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

package org.eclipse.ui;

/**
 * @since 3.106
 * 
 */
public class FileSuffixValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public static class DoesNotStartWithWildCharName extends FileSuffixValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class MultipleWildCharsException extends FileSuffixValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class NotEnoughExtensionPartsException extends FileSuffixValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class IllegalDotPositionException extends FileSuffixValidationException {
		private static final long serialVersionUID = 1L;
	}


}
