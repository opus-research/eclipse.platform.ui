/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandros Karypidis - bug 22905
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import java.util.Iterator;

import org.eclipse.ui.FileTypeValidationException;
import org.eclipse.ui.IFileTypeProcessor;
import org.eclipse.ui.internal.registry.FileTypeProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileTypeProcessorTest {

	IFileTypeProcessor validator;

	@Before
	public void setup() {
		validator = new FileTypeProcessor();
	}

	@Test(expected = FileTypeValidationException.PatternIsEmptyException.class)
	public void testPatternIsEmptyException()
			throws FileTypeValidationException {
		validator.validateFileTypePattern("");
	}

	@Test(expected = FileTypeValidationException.EmptyExtensionWithNoNameException.class)
	public void testEmptyExtensionWithNoNameException()
			throws FileTypeValidationException {
		validator.validateFileTypePattern("*.");
	}

	@Test(expected = FileTypeValidationException.PatternIsSingleWildCharException.class)
	public void testPatternIsSingleWildCharException()
			throws FileTypeValidationException {
		validator.validateFileTypePattern("*");
	}

	@Test(expected = FileTypeValidationException.IllegalWildCharPositionException.class)
	public void testIllegalWildCharPositionException1()
			throws FileTypeValidationException {
		validator.validateFileTypePattern("name*.ext");
	}

	@Test(expected = FileTypeValidationException.IllegalWildCharPositionException.class)
	public void testIllegalWildCharPositionException2()
			throws FileTypeValidationException {
		validator.validateFileTypePattern("name.*.ext");
	}

	@Test(expected = FileTypeValidationException.MultipleWildCharsException.class)
	public void testMultipleWildCharsException()
			throws FileTypeValidationException {
		validator.validateFileTypePattern("*.name.*.ext");
	}

	@Test
	public void testSuffixIterator() {
		Iterator<String> it = validator
				.suffixIterator("filename.ext1.ext2.ext3");
		String[] expectedItems = new String[] { "filename.ext1.ext2.ext3",
				"*.ext1.ext2.ext3", "*.ext2.ext3", "*.ext3", };
		int i = 0;
		while ((i < expectedItems.length) && it.hasNext()) {
			String actual = it.next();
			Assert.assertEquals(expectedItems[i++], actual);
		}
		Assert.assertEquals(expectedItems.length, i);
		Assert.assertFalse(it.hasNext());
	}
}