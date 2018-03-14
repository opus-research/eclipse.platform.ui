/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 271720)
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 468293
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.UnsupportedTemporalTypeException;

import org.eclipse.jface.internal.databinding.swt.DateTimeSelectionProperty;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;

/**
 * @since 3.2
 *
 */
public class DateTimeSelectionPropertyTest extends AbstractSWTTestCase {
	DateTime dateTime;
	DateTimeSelectionProperty property;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dateTime = new DateTime(getShell(), SWT.DATE);
		property = new DateTimeSelectionProperty();
	}

	public void testSetValue_NullThrowIllegalArgumentException() {
		try {
			property.setValue(dateTime, null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	public void testLocalDateTime() {
		property.setValue(dateTime, LocalDateTime.now());
		assertTrue(true);
	}

	public void testLocalDate() {
		property.setValue(dateTime, LocalDate.now());
		assertTrue(true);
	}

	// LocalTime supports not dates
	public void testLocalTime_UnsupportedTemporalTypeException() {
		try {
			property.setValue(dateTime, LocalTime.now());
			fail("Expected IllegalArgumentException");
		} catch (UnsupportedTemporalTypeException expected) {
		}
	}
}
