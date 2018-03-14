/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToShortConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToShortConverterTest extends NumberToNumberTestHarness {
	private NumberFormat numberFormat;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		numberFormat = NumberFormat.getInstance();
	}
	
	protected Number doGetOutOfRangeNumber() {
		return new Integer(Short.MAX_VALUE + 1);
	}

	protected IConverter doGetToBoxedTypeValidator(Class fromType) {
		return new NumberToShortConverter(numberFormat, fromType, false);
	}

	protected IConverter doGetToPrimitiveValidator(Class fromType) {
		return new NumberToShortConverter(numberFormat, fromType, true);
	}

	protected Class doGetToType(boolean primitive) {
		return (primitive) ? Short.TYPE : Short.class;
	}
}
