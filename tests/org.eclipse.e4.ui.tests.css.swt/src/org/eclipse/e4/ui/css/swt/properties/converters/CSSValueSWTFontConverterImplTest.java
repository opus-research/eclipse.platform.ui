/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.converters;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.VALUE_FROM_FONT_DEFINITION;
import static org.mockito.Mockito.mock;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelperTestCase;
import org.eclipse.e4.ui.css.swt.resources.FontByDefinition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class CSSValueSWTFontConverterImplTest extends CSSSWTHelperTestCase {
	private Display display;
	
	private CSSValueSWTFontConverterImplTestable converter;
	
	@Override
	public void setUp() throws Exception {
		display = Display.getDefault();
		converter = new CSSValueSWTFontConverterImplTestable();
	}
	
	public void testConvert() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties("Arial", 10, SWT.NORMAL);
		
		//when
		Object result = converter.convert(fontProperties, mock(CSSEngine.class), display);
		
		//then
		assertEquals(Font.class, result.getClass());
		
		((Font) result).dispose();
	}
	
	public void testConvertWhenDefinitionAsFontFamily() throws Exception {
		//given
		registerFontProviderWith("org.eclipse.egit.ui.IgnoredResourceFont", "Times", 11, SWT.ITALIC);
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("org-eclipse-egit-ui-IgnoredResourceFont"), 
				VALUE_FROM_FONT_DEFINITION, VALUE_FROM_FONT_DEFINITION);
				
		//when
		Object result = converter.convert(fontProperties, mock(CSSEngine.class), display);
				
		//then
		assertEquals(FontByDefinition.class, result.getClass());
		
		Font font = ((FontByDefinition) result).getResource();
		assertNotNull(font.getFontData());
		assertTrue(font.getFontData().length > 0);
		assertEquals("Times", font.getFontData()[0].getName());
		assertEquals(11, font.getFontData()[0].getHeight());
		assertEquals(SWT.ITALIC, font.getFontData()[0].getStyle());
				
		font.dispose();
	}
	
	public void testCreateFontByDefinition() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("org-eclipse-egit-ui-IgnoredResourceFont"), 
				VALUE_FROM_FONT_DEFINITION, VALUE_FROM_FONT_DEFINITION);
		
		Font fontFromDefinition = new Font(display, "Times", 10, SWT.NORMAL);
		
		//when
		FontByDefinition definition = converter.createFontByDefinition(fontProperties, fontFromDefinition);
		
		//then
		assertEquals("org.eclipse.egit.ui.IgnoredResourceFont", definition.getSymbolicName());
		assertNull(definition.getHeight());
		assertNull(definition.getStyle());
		
		fontFromDefinition.dispose();
	}
	
	public void testCreateFontByDefinitionWhenOverridenSizeAndStyleAttributes() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("org-eclipse-egit-ui-IgnoredResourceFont"), 
				11, SWT.ITALIC);
		
		Font fontFromDefinition = new Font(display, "Times", 11, SWT.ITALIC);
		
		//when
		FontByDefinition definition = converter.createFontByDefinition(fontProperties, fontFromDefinition);
		
		//then
		assertEquals("org.eclipse.egit.ui.IgnoredResourceFont", definition.getSymbolicName());
		assertEquals(11, definition.getHeight().intValue());
		assertEquals(SWT.ITALIC, definition.getStyle().intValue());
		
		fontFromDefinition.dispose();
	}
	
	private static class CSSValueSWTFontConverterImplTestable extends CSSValueSWTFontConverterImpl {
		@Override
		public FontByDefinition createFontByDefinition(CSS2FontProperties props, Font font) {
			return super.createFontByDefinition(props, font);
		}
	}
}
