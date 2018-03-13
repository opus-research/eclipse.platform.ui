/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandros Karypidis - bug 22905
 *******************************************************************************/
package org.eclipse.ui;

import java.util.Iterator;

/**
 * @since 3.106
 */
public interface IFileTypeProcessor {
	public void validateFileTypePattern(String pattern) throws FileTypeValidationException;

	public boolean isValidFileType(String pattern);

	public void validateFileSuffixPattern(String pattern) throws FileSuffixValidationException;

	public boolean isValidSuffixPattern(String pattern);

	/**
	 * Get the name.
	 * 
	 * @param filename
	 * 
	 * @return the name
	 */
	public String getName(String filename);

	/**
	 * Get the extension.
	 * 
	 * @param filename
	 * 
	 * @return the extension
	 */
	public String getExtension(String filename);
	
	/**
	 * Get the prefix.
	 * 
	 * @param filename
	 * 
	 * @return the extension
	 */
	public String getPrefix(String filename);

	/**
	 * Get the suffix.
	 * 
	 * @param filename
	 * 
	 * @return the extension
	 */
	public String getSuffix(String filename);

	public Iterator<String> suffixIterator(final String filename);
}
