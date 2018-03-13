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

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.getSWTColor;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.getSWTColors;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.hasColorDefinitionAsValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.resources.ColorByDefinition;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class CSSSWTColorHelperTest extends CSSSWTHelperTestCase {
	private Display display;
	
	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
	}
	
	public void testHasColorDefinitionAsValue() throws Exception {
		boolean result = hasColorDefinitionAsValue(colorValue(addColorDefinitionMarker("org-eclipse-ui-workbench-INACTIVE_TAB_TEXT_COLOR")));
		
		assertTrue(result);		
	}
	
	public void testHasColorDefinitionAsValueWhenHexColorValue() throws Exception {
		boolean result = hasColorDefinitionAsValue(colorValue("#FF00ab"));
		
		assertFalse(result);
	}
	
	public void testHasColorDefinitionAsValueWhenOtherColorValue() throws Exception {
		boolean result = hasColorDefinitionAsValue(colorValue("red"));
		
		assertFalse(result);
	}
	
	public void testGetSWTColor() throws Exception {
		Color result = getSWTColor(colorValue("red"), display);
		
		assertNotNull(result);
		assertEquals(255, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(0, result.getGreen());
	}
	
	public void testGetSWTColorWhenNotSupportedColorType() throws Exception {
		Color result = getSWTColor(colorValue("123213", CSSValue.CSS_CUSTOM), display);
		
		assertNull(result);
	}
	
	public void testGetSWTColorWhenInvalidColorValue() throws Exception {
		Color result = getSWTColor(colorValue("asdsad12"), display);
		
		assertNotNull(result);
		assertEquals(0, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(0, result.getGreen());
	}
	
	public void testGetSWTColorWhenColorFromDefinition() throws Exception {
		registerColorProviderWith("org.eclipse.jdt.debug.ui.InDeadlockColor", new RGB(0, 255, 0));
		
		Color result = getSWTColor(colorValue(addColorDefinitionMarker("org-eclipse-jdt-debug-ui-InDeadlockColor")), display);
		
		assertNotNull(result);
		assertEquals(0, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(255, result.getGreen());
	}	
	
	public void testGetSWTColorByDefinition() throws Exception {
		//given
		registerColorProviderWith("org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_COLOR", new RGB(255, 0, 0));
		Color previousColor = new Color(display, new RGB(255, 0, 0));
		
		//when
		Color currentColor = getSWTColor(new ColorByDefinition("org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_COLOR", previousColor));
		
		//then
		assertEquals(previousColor, currentColor);
		
		currentColor.dispose();
	}
	
	public void testGetSWTColorByDefinitionWhenDefinitionHasBeenChanged() throws Exception {
		//given
		registerColorProviderWith("org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_COLOR", new RGB(255, 255, 0));
		Color previousColor = new Color(display, new RGB(255, 0, 0));
		
		//when
		Color currentColor = getSWTColor(new ColorByDefinition("org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_COLOR", previousColor));
		
		//then
		assertNotSame(previousColor, currentColor);
		assertEquals(new RGB(255, 255, 0), currentColor.getRGB());
		
		previousColor.dispose();
		currentColor.dispose();
	}
	
	public void testGetSWTColorByDefinitionWhenDefinitionNotFound() throws Exception {
		//given
		registerColorProviderWith("org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_COLOR", new RGB(255, 255, 0));
		Color previousColor = new Color(display, new RGB(255, 0, 0));
		
		//when
		Color currentColor = getSWTColor(new ColorByDefinition("not existing color definition", previousColor));
		
		//then
		assertEquals(previousColor, currentColor);
		
		currentColor.dispose();
	}
	
	public void testGetSWTColorsByGradient() throws Exception {
		//given
		Color color1 = new Color(display, new RGB(255, 0, 0));
		Color color2 = new Color(display, new RGB(255, 255, 0));
		
		CSSPrimitiveValue color1Value = colorValue("rgb(255, 0, 0)");
		CSSPrimitiveValue color2Value = colorValue("rgb(255, 255, 0)");
		
		Gradient gradient = mock(Gradient.class);
		doReturn(Arrays.asList(color1Value, color2Value)).when(gradient).getValues();
		
		CSSEngine engine = mock(CSSEngine.class);
		doReturn(color1).when(engine).convert(color1Value, Color.class, display);
		doReturn(color2).when(engine).convert(color2Value, Color.class, display);
		
		
		//when
		Color[] result = getSWTColors(gradient, display, engine);
		
		
		//then
		assertEquals(2, result.length);
		assertEquals(color1, result[0]);
		assertEquals(color2, result[1]);
		
		color1.dispose();
		color2.dispose();
	}
}
