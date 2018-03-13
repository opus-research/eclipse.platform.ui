/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSValueKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelperTestCase;
import org.eclipse.e4.ui.css.swt.resources.ColorByDefinition;
import org.eclipse.e4.ui.css.swt.resources.FontByDefinition;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.e4.ui.css.swt.resources.VolatileResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.css.CSSPrimitiveValue;

@SuppressWarnings("restriction")
public class CSSSWTEngineImplTest extends CSSSWTHelperTestCase {
	private Display display;
	
	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
	}
	
	@SuppressWarnings("unchecked")
	public void testConvertWhenFont() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties("Arial", 11, SWT.ITALIC);
				
		Font font = new Font(display, "Arial", 11, SWT.ITALIC);
		
		IResourcesRegistry resourceRegistry = mock(IResourcesRegistry.class);		
		when(resourceRegistry.getResource(Font.class, getCSSValueKey(fontProperties))).thenReturn(font);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);		
		
		
		//when
		Object result = engine.convert(fontProperties, Font.class, display);
		
		
		//then
		assertEquals(font, result);		
		verify(engine, never()).processVolatileResource(any(VolatileResource.class));
		
		font.dispose();
	}
	
	@SuppressWarnings("unchecked")
	public void testConvertWhenColor() throws Exception {
		//given
		CSSPrimitiveValue colorValue = colorValue("#00ff00");
				
		Color color = new Color(display, new RGB(0, 255, 0));
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);
		when(resourceRegistry.getResource(Font.class, getCSSValueKey(colorValue))).thenReturn(color);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);		
		
		
		//when
		Object result = engine.convert(colorValue, Color.class, display);
		
		
		//then
		assertEquals(color, result);		
		verify(engine, never()).processVolatileResource(any(VolatileResource.class));
		
		color.dispose();
	}
	
	public void testConvertWhenFontByDefinition() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("symbolicName"), 11, SWT.ITALIC);
		
		Font font = new Font(display, "Arial", 11, SWT.ITALIC);
		
		FontByDefinition fontByDefinition = new FontByDefinition("symbolicName", font);
		fontByDefinition.setValid(true);
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);		
		when(resourceRegistry.getResource(Font.class, getCSSValueKey(fontProperties))).thenReturn(fontByDefinition);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);		
		
		
		//when
		Object result = engine.convert(fontProperties, Font.class, display);
		
		
		//then
		assertEquals(font, result);		
		verify(engine, times(1)).processVolatileResource(fontByDefinition);
		verify(engine, never()).getCurrentResource(fontByDefinition);
		verify(resourceRegistry, never()).addUnusedResource(any(Resource.class));
		
		font.dispose();
	}
	
	public void testConvertWhenFontByDefinitionAndVolatileResourceInvalidAndFontValid() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("symbolicName"), 11, SWT.ITALIC);
		
		final Font font = new Font(display, "Arial", 11, SWT.ITALIC);
		
		FontByDefinition fontByDefinition = new FontByDefinition("symbolicName", font);
		fontByDefinition.setValid(false);
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);		
		when(resourceRegistry.getResource(Font.class, getCSSValueKey(fontProperties))).thenReturn(fontByDefinition);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);
		doAnswer(new Answer<Font>() {
			public Font answer(InvocationOnMock invocation) throws Throwable {
				return font;
			}			
		}).when(engine).getCurrentFont(fontByDefinition);
		
		
		//when
		Object result = engine.convert(fontProperties, Font.class, display);
		
		
		//then
		assertEquals(font, result);
		verify(engine, times(1)).processVolatileResource(fontByDefinition);
		verify(engine, times(1)).getCurrentResource(fontByDefinition);
		verify(engine, times(1)).getCurrentFont(fontByDefinition);
		verify(engine, never()).getCurrentColor(any(ColorByDefinition.class));		
		verify(resourceRegistry, never()).addUnusedResource(any(Resource.class));
		
		font.dispose();
	}
	
	public void testConvertWhenFontByDefinitionAndVolatileResourceInvalidAndFontInvalid() throws Exception {
		//given
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("symbolicName"), 11, SWT.ITALIC);
		
		Font previousFont = new Font(display, "Arial", 11, SWT.ITALIC);
		
		final Font newFont = new Font(display, "Arial", 22, SWT.NORMAL);
		
		FontByDefinition fontByDefinition = new FontByDefinition("symbolicName", previousFont);
		fontByDefinition.setValid(false);
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);		
		when(resourceRegistry.getResource(Font.class, getCSSValueKey(fontProperties))).thenReturn(fontByDefinition);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);
		doAnswer(new Answer<Font>() {
			public Font answer(InvocationOnMock invocation) throws Throwable {
				return newFont;
			}			
		}).when(engine).getCurrentFont(fontByDefinition);
		
		
		//when
		Object result = engine.convert(fontProperties, Font.class, display);
		
		
		//then
		assertEquals(newFont, result);
		verify(engine, times(1)).processVolatileResource(fontByDefinition);
		verify(engine, times(1)).getCurrentResource(fontByDefinition);
		verify(engine, times(1)).getCurrentFont(fontByDefinition);
		verify(engine, never()).getCurrentColor(any(ColorByDefinition.class));		
		verify(resourceRegistry, times(1)).addUnusedResource(previousFont);
		
		previousFont.dispose();
		newFont.dispose();
	}
	
	public void testConvertWhenColorByDefinition() throws Exception {
		//given
		CSSPrimitiveValue colorValue = colorValue(addColorDefinitionMarker("symbolicName"));
		
		Color color = new Color(display, new RGB(255,0,0));
		
		ColorByDefinition colorByDefinition = new ColorByDefinition("symbolicName", color);
		colorByDefinition.setValid(true);
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);		
		when(resourceRegistry.getResource(Color.class, getCSSValueKey(colorValue))).thenReturn(colorByDefinition);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);		
		
		
		//when
		Object result = engine.convert(colorValue, Color.class, display);
		
		
		//then
		assertEquals(color, result);
		verify(engine, times(1)).processVolatileResource(colorByDefinition);
		verify(engine, never()).getCurrentResource(colorByDefinition);
		verify(resourceRegistry, never()).addUnusedResource(any(Resource.class));
		
		color.dispose();
	}
	
	public void testConvertWhenColorByDefinitionAndVolatileResourceInvalidAndColorValid() throws Exception {
		//given
		CSSPrimitiveValue colorValue = colorValue(addColorDefinitionMarker("symbolicName"));
		
		final Color color = new Color(display, new RGB(255,0,0));
		
		ColorByDefinition colorByDefinition = new ColorByDefinition("symbolicName", color);
		colorByDefinition.setValid(false);
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);		
		when(resourceRegistry.getResource(Color.class, getCSSValueKey(colorValue))).thenReturn(colorByDefinition);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);	
		doAnswer(new Answer<Color>() {
			public Color answer(InvocationOnMock invocation) throws Throwable {
				return color;
			}			
		}).when(engine).getCurrentColor(colorByDefinition);
		
		
		//when
		Object result = engine.convert(colorValue, Color.class, display);
		
		
		//then
		assertEquals(color, result);				
		verify(engine, times(1)).processVolatileResource(colorByDefinition);
		verify(engine, times(1)).getCurrentResource(colorByDefinition);
		verify(engine, never()).getCurrentFont(any(FontByDefinition.class));
		verify(engine, times(1)).getCurrentColor(colorByDefinition);		
		verify(resourceRegistry, never()).addUnusedResource(any(Resource.class));
		
		color.dispose();
	}
	
	public void testConvertWhenColorByDefinitionAndVolatileResourceInvalidAndColorInvalid() throws Exception {
		//given
		CSSPrimitiveValue colorValue = colorValue(addColorDefinitionMarker("symbolicName"));
		
		Color previousColor = new Color(display, new RGB(255,0,0));
		
		final Color newColor = new Color(display, new RGB(0, 255, 0));
		
		ColorByDefinition colorByDefinition = new ColorByDefinition("symbolicName", previousColor);
		colorByDefinition.setValid(false);
		
		SWTResourcesRegistry resourceRegistry = mock(SWTResourcesRegistry.class);		
		when(resourceRegistry.getResource(Color.class, getCSSValueKey(colorValue))).thenReturn(colorByDefinition);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		engine.setResourcesRegistry(resourceRegistry);	
		doAnswer(new Answer<Color>() {
			public Color answer(InvocationOnMock invocation) throws Throwable {
				return newColor;
			}			
		}).when(engine).getCurrentColor(colorByDefinition);
		
		
		//when
		Object result = engine.convert(colorValue, Color.class, display);
		
		
		//then
		assertEquals(newColor, result);				
		verify(engine, times(1)).processVolatileResource(colorByDefinition);
		verify(engine, times(1)).getCurrentResource(colorByDefinition);
		verify(engine, never()).getCurrentFont(any(FontByDefinition.class));
		verify(engine, times(1)).getCurrentColor(colorByDefinition);		
		verify(resourceRegistry, times(1)).addUnusedResource(previousColor);
		
		previousColor.dispose();
		newColor.dispose();
	}
	
	public void testGetCurrentResourceWhenFontByDefinition() throws Exception {
		//given
		final Font font = new Font(display, "Arial", 11, SWT.NORMAL);
		
		FontByDefinition fontByDefinition = new FontByDefinition("symbolicName", null);
		fontByDefinition.setValid(false);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		doAnswer(new Answer<Font>() {
			public Font answer(InvocationOnMock invocation) throws Throwable {
				return font;
			}			
		}).when(engine).getCurrentFont(fontByDefinition);
		
		
		//when
		Resource result = engine.getCurrentResource(fontByDefinition);
		
		
		//then
		assertEquals(font, result);
		assertTrue(fontByDefinition.isValid());
		verify(engine, times(1)).getCurrentFont(any(FontByDefinition.class));
		verify(engine, never()).getCurrentColor(any(ColorByDefinition.class));
		
		font.dispose();
	}
	
	public void testGetCurrentResourceWhenColorByDefinition() throws Exception {
		//given
		final Color color = new Color(display, 255, 0, 0);
		
		ColorByDefinition colorByDefinition = new ColorByDefinition("symbolicName", null);
		colorByDefinition.setValid(false);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		doAnswer(new Answer<Color>() {
			public Color answer(InvocationOnMock invocation) throws Throwable {
				return color;
			}			
		}).when(engine).getCurrentColor(colorByDefinition);
		
		
		//when
		Resource result = engine.getCurrentResource(colorByDefinition);
		
		
		//then
		assertEquals(color, result);
		assertTrue(colorByDefinition.isValid());
		verify(engine, never()).getCurrentFont(any(FontByDefinition.class));
		verify(engine, times(1)).getCurrentColor(any(ColorByDefinition.class));
		
		color.dispose();
	}
	
	public void testGetCurrentResourceWhenResourceNotFound() throws Exception {
		//given		
		ColorByDefinition colorByDefinition = new ColorByDefinition("symbolicName", null);
		colorByDefinition.setValid(false);
		
		CSSSWTEngineImplTestable engine = spy(new CSSSWTEngineImplTestable(display));
		doAnswer(new Answer<Color>() {
			public Color answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}			
		}).when(engine).getCurrentColor(colorByDefinition);
		
		
		//when
		Resource result = engine.getCurrentResource(colorByDefinition);
		
		
		//then
		assertNull(result);
		assertFalse(colorByDefinition.isValid());
		verify(engine, never()).getCurrentFont(any(FontByDefinition.class));
		verify(engine, times(1)).getCurrentColor(any(ColorByDefinition.class));		
	}
		
	@SuppressWarnings("unchecked")
	public void testGetCurrentResourceWhenNotSupportedVolatileResource() throws Exception {
		//given		
		VolatileResource<Image> volatileResource = mock(VolatileResource.class);
		
		CSSSWTEngineImplTestable engine = new CSSSWTEngineImplTestable(display);
		
		Exception thrownException = null;
		
		
		//when
		try {
			engine.getCurrentResource(volatileResource);
		} catch (Exception exc) {
			thrownException = exc;
		}
		
		
		//then
		assertNotNull(thrownException);
		assertThat(thrownException, instanceOf(IllegalArgumentException.class));
		assertThat(thrownException.getMessage(), containsString(volatileResource.getClass().getName()));			
	}
	
	public static class CSSSWTEngineImplTestable extends CSSSWTEngineImpl {
		public CSSSWTEngineImplTestable(Display display) {
			super(display);
		}

		@Override
		public Font getCurrentFont(FontByDefinition definition) {
			return super.getCurrentFont(definition);
		}
		
		@Override
		public Color getCurrentColor(ColorByDefinition definition) {
			return super.getCurrentColor(definition);
		}
		
		@Override
		public <T extends Resource> T getCurrentResource(VolatileResource<T> volatileResource) {
			return super.getCurrentResource(volatileResource);
		}
		
		@Override
		public <T extends Resource> Resource processVolatileResource(VolatileResource<T> volatileResource) {
			return super.processVolatileResource(volatileResource);
		}
	}
}
