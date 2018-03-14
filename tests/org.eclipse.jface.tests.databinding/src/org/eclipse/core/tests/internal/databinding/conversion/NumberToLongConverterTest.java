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
import org.eclipse.core.internal.databinding.conversion.NumberToLongConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToLongConverterTest extends NumberToNumberTestHarness {
	private NumberFormat numberFormat;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		numberFormat = NumberFormat.getInstance();
	}
	
	protected Number doGetOutOfRangeNumber() {
		return new Double(Double.MAX_VALUE);
	}

	protected IConverter doGetToBoxedTypeValidator(Class fromType) {
		return new NumberToLongConverter(numberFormat, fromType, false);
	}

	protected IConverter doGetToPrimitiveValidator(Class fromType) {
		return new NumberToLongConverter(numberFormat, fromType, true);
	}

	protected Class doGetToType(boolean primitive) {
		return (primitive) ? Long.TYPE : Long.class;
	}
}
