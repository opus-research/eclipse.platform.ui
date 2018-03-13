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
public class FileTypeValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public static class PatternIsEmptyException extends FileTypeValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class EmptyExtensionWithNoNameException extends FileTypeValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class PatternIsSingleWildCharException extends FileTypeValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class IllegalWildCharPositionException extends FileTypeValidationException {
		private static final long serialVersionUID = 1L;
	}

	public static class MultipleWildCharsException extends FileTypeValidationException {
		private static final long serialVersionUID = 1L;
	}

}
