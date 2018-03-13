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

package org.eclipse.ui.internal.registry;

import java.util.Iterator;
import java.util.regex.Pattern;
import org.eclipse.ui.FileSuffixValidationException;
import org.eclipse.ui.FileTypeValidationException;
import org.eclipse.ui.IFileTypeProcessor;

/**
 * @since 3.5
 * 
 */
public class FileTypeProcessor implements IFileTypeProcessor {

	public static final char STAR_CHAR = '*';
	public static final char DOT_CHAR = '.';
	public static final String STAR = "*"; //$NON-NLS-1$
	public static final String DOT = "."; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IFileTypeValidator#validateFileTypePattern(java.lang.String
	 * )
	 */
	public void validateFileTypePattern(String filename) throws FileTypeValidationException {
		// We need kernel api to validate the extension or a filename

		// check for empty name and extension
		if (filename.length() == 0) {
			// reject ''
			throw new FileTypeValidationException.PatternIsEmptyException();
		}

		// check for empty extension if there is no name
		int index = filename.lastIndexOf(DOT_CHAR);
		if (index == filename.length() - 1) {
			if (index == 0 || (index == 1 && filename.charAt(0) == STAR_CHAR)) {
				// reject '.' and '*.' (extension cannot b empty)
				throw new FileTypeValidationException.EmptyExtensionWithNoNameException();
			}
		}

		index = filename.indexOf(STAR_CHAR);
		if (index > -1) {
			if (filename.length() == 1) {
				// reject single wild char alone
				throw new FileTypeValidationException.PatternIsSingleWildCharException();
			}
			if (index != 0 || filename.charAt(1) != DOT_CHAR) {
				// reject single wild char _anywhere_ EXCEPT when
				// 1. it is first char of string
				// 2. OR second char is NOT a dot
				throw new FileTypeValidationException.IllegalWildCharPositionException();
			}
			if (filename.length() > index && filename.indexOf(STAR_CHAR, index + 1) != -1) {
				// reject more than one wild char
				throw new FileTypeValidationException.MultipleWildCharsException();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IFileTypeValidator#isValidFileType(java.lang.String)
	 */
	public boolean isValidFileType(String pattern) {
		try {
			validateFileTypePattern(pattern);
		} catch (FileTypeValidationException e) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IFileTypeProcessor#validateFileSuffixPattern(java.lang
	 * .String)
	 */
	public void validateFileSuffixPattern(String pattern) throws FileSuffixValidationException {
		// must start with '*.'
		if ((pattern.charAt(0) != STAR_CHAR) || (pattern.charAt(1) != DOT_CHAR))
			throw new FileSuffixValidationException.DoesNotStartWithWildCharName();
		// must not have any more wild chars later
		if (pattern.indexOf(STAR_CHAR, 1) != -1)
			throw new FileSuffixValidationException.MultipleWildCharsException();

		// must be at least a '*.[ext1].[ext2]' pattern
		String[] parts = pattern.split(Pattern.quote(DOT));
		if (parts.length < 3)
			throw new FileSuffixValidationException.NotEnoughExtensionPartsException();
		// also, consecutive dots are not allowed
		for (int i = 1; i < parts.length; i++) {
			if (parts[i].length() == 0)
				throw new FileSuffixValidationException.IllegalDotPositionException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IFileTypeProcessor#isValidAssociationPattern(java.lang
	 * .String)
	 */
	public boolean isValidSuffixPattern(String pattern) {
		try {
			validateFileSuffixPattern(pattern);
		} catch (FileSuffixValidationException e) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IFileTypeProcessor#getName(java.lang.String)
	 */
	public String getName(String filename) {
		int index = filename.lastIndexOf(DOT_CHAR);
		if (index == -1) {
			return filename;
		}
		if (index == 0) {
			return STAR;
		}
		return filename.substring(0, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IFileTypeProcessor#getExtension(java.lang.String)
	 */
	public String getExtension(String filename) {
		int index = filename.lastIndexOf(DOT_CHAR);
		if (index == -1) {
			return ""; //$NON-NLS-1$
		}
		if (index == filename.length()) {
			return ""; //$NON-NLS-1$
		}
		return filename.substring(index + 1, filename.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IFileTypeProcessor#getPrefix(java.lang.String)
	 */
	public String getPrefix(String filename) {
		int index = filename.indexOf(DOT_CHAR);
		if (index == -1) {
			return filename;
		}
		if (index == 0) {
			return ""; //$NON-NLS-1$
		}
		return filename.substring(0, index - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IFileTypeProcessor#getSuffix(java.lang.String)
	 */
	public String getSuffix(String filename) {
		int index = filename.indexOf(DOT_CHAR);
		if (index == -1) {
			return ""; //$NON-NLS-1$
		}
		if (index == filename.length()) {
			return ""; //$NON-NLS-1$
		}
		return filename.substring(index + 1, filename.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IFileTypeProcessor#suffixIterator(java.lang.String)
	 */
	public Iterator<String> suffixIterator(final String filename) {
		return new Iterator<String>() {
			final String[] parts = filename.split(Pattern.quote(DOT));
			final StringBuilder sb = new StringBuilder();

			int start = 0;

			public boolean hasNext() {
				return start < parts.length;
			}

			public String next() {
				sb.setLength(0);
				if (start > 0)
					sb.append(STAR_CHAR).append(DOT_CHAR);
				for (int i = start; i < parts.length - 1; i++) {
					sb.append(parts[i]).append(DOT_CHAR);
				}
				sb.append(parts[parts.length - 1]);
				start++;
				return sb.toString();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
