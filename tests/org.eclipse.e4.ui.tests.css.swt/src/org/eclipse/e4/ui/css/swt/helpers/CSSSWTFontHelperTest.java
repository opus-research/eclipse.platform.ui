/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.VALUE_FROM_FONT_DEFINITION;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.getFontData;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.hasFontDefinitionAsFamily;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.getFont;

import org.eclipse.e4.ui.css.swt.resources.FontByDefinition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class CSSSWTFontHelperTest extends CSSSWTHelperTestCase {	
	private Display display;
	
	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
	}
	
	public void testHasFontDefinitionAsFamily() throws Exception {
		boolean result = hasFontDefinitionAsFamily(fontProperties(addFontDefinitionMarker("org-eclipse-wst-sse-ui-textfont"), 10, SWT.NORMAL));
		
		assertTrue(result);
	}
	
	public void testHasFontDefinitionAsFamilyWhenNotDefinitionAsFamily() throws Exception {
		boolean result = hasFontDefinitionAsFamily(fontProperties("arial", 10, SWT.NORMAL));
		
		assertFalse(result);
	}
	
	public void testGetFontData() throws Exception {		
		FontData result = getFontData(fontProperties("Times", 11, SWT.NORMAL), 
				new FontData());
		
		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}
	
	public void testGetFontDataWhenMissingFamilyInCss() throws Exception {		
		FontData result = getFontData(fontProperties(null, 11, SWT.NORMAL), 
				new FontData("Courier", 5, SWT.ITALIC));
		
		assertEquals("Courier", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}
	
	public void testGetFontDataWhenMissingSizeInCss() throws Exception {		
		FontData result = getFontData(fontProperties("Arial", null, SWT.NORMAL), 
				new FontData("Courier", 5, SWT.ITALIC));
		
		assertEquals("Arial", result.getName());
		assertEquals(5, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}
	
	public void testGetFontDataWhenMissingStyleInCss() throws Exception {		
		FontData result = getFontData(fontProperties("Times", 11, null), 
				new FontData("Courier", 5, SWT.ITALIC));
		
		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC, result.getStyle());
	}
	
	public void testGetFontDataWhenFontFamilyFromDefinition() throws Exception {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);
		
		FontData result = getFontData(fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont"), 10, SWT.NORMAL), 
				new FontData());
		
		assertEquals("Arial", result.getName());
		assertEquals(10, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}
	
	public void testGetFontDataWhenFontFamilyAndSizeFromDefinition() throws Exception {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);
		
		FontData result = getFontData(fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont"), VALUE_FROM_FONT_DEFINITION, SWT.NORMAL), 
				new FontData());
		
		assertEquals("Arial", result.getName());	
		assertEquals(15, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}
	
	public void testGetFontDataWhenFontFamilySizeAndStyleFromDefinition() throws Exception {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);
		
		FontData result = getFontData(fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont"), VALUE_FROM_FONT_DEFINITION, VALUE_FROM_FONT_DEFINITION), 
				new FontData());
		
		assertEquals("Arial", result.getName());	
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC, result.getStyle());
	}		
	
	public void testGetFontByDefinition() throws Exception {
		//given
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);		
		Font previousFont = new Font(display, new FontData("Arial", 15, SWT.ITALIC));
		
		//when
		Font currentFont = getFont(new FontByDefinition("org.eclipse.jface.bannerfont", previousFont));
		
		//then
		assertEquals(previousFont, currentFont);
		
		previousFont.dispose();
	}
	
	public void testGetFontByDefinitionWhenDefinitionHasBeenChanged() throws Exception {
		//given
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);		
		Font previousFont = new Font(display, new FontData("Times", 15, SWT.NORMAL));
		
		//when
		Font currentFont = getFont(new FontByDefinition("org.eclipse.jface.bannerfont", previousFont));
		
		//then
		assertNotSame(previousFont, currentFont);
		
		previousFont.dispose();
		currentFont.dispose();
	}
	
	public void testGetFontByDefinitionWhenDefinitionNotFound() throws Exception {
		//given
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);		
		Font previousFont = new Font(display, new FontData("Times", 15, SWT.NORMAL));
		
		//when
		Font currentFont = getFont(new FontByDefinition("some not existing definition", previousFont));
		
		//then
		assertEquals(previousFont, currentFont);
		
		previousFont.dispose();
	}
}
