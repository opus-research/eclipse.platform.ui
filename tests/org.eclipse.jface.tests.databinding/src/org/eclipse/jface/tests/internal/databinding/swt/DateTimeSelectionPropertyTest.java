/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 271720)
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.fail;

import org.eclipse.jface.internal.databinding.swt.DateTimeSelectionProperty;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class DateTimeSelectionPropertyTest extends AbstractSWTTestCase {
	DateTime dateTime;
	DateTimeSelectionProperty property;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		dateTime = new DateTime(getShell(), SWT.DATE);
		property = new DateTimeSelectionProperty();
	}

	@Test
	public void testSetValue_NullThrowIllegalArgumentException() {
		try {
			property.setValue(dateTime, null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}
}
