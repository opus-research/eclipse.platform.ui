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

package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import junit.framework.TestCase;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer.ThemeDefinitionChangedHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.osgi.service.event.Event;

/**
 *
 */
public class ThemeDefinitionChangedHandlerTest extends TestCase {
	public void testHandleEventWhenThemeChanged() throws Exception {
		// given
		final MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(MBasicFactory.INSTANCE.createWindow());
		application.getChildren().add(MBasicFactory.INSTANCE.createWindow());

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(UIEvents.EventTags.ELEMENT, application);
		params.put(UIEvents.EventTypes.ADD_MANY, false);

		Event event = new Event("topic", params);

		SWTResourcesRegistry registry = mock(SWTResourcesRegistry.class);

		CSSEngine engine = mock(CSSEngine.class);
		doReturn(registry).when(engine).getResourcesRegistry();

		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());
		doReturn(engine).when(handler).getEngine(any(MWindow.class));

		// when
		handler.handleEvent(event);

		// then
		verify(engine, times(2)).reapply();
		verify(registry, times(2)).invalidateResources(Font.class, Color.class);
	}

	public void testHandleEventWhenElementIsNotMApplication() throws Exception {
		// given
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(UIEvents.EventTags.ELEMENT,
				MBasicFactory.INSTANCE.createWindow());
		params.put(UIEvents.EventTypes.ADD_MANY, true);

		Event event = new Event("topic", params);

		CSSEngine engine = mock(CSSEngine.class);

		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());
		doReturn(engine).when(handler).getEngine(any(MWindow.class));

		// when
		handler.handleEvent(event);

		// then
		verify(engine, never()).reapply();
	}

	public void testHandleEventWhenCSSEngineNotFoundForWidget()
			throws Exception {
		// given
		MWindow window1 = MBasicFactory.INSTANCE.createWindow();
		MWindow window2 = MBasicFactory.INSTANCE.createWindow();

		final MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window1);
		application.getChildren().add(window2);

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(UIEvents.EventTags.ELEMENT, application);
		params.put(UIEvents.EventTypes.ADD_MANY, false);

		Event event = new Event("topic", params);

		SWTResourcesRegistry registry = mock(SWTResourcesRegistry.class);

		CSSEngine engine = mock(CSSEngine.class);
		doReturn(registry).when(engine).getResourcesRegistry();

		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());
		doReturn(null).when(handler).getEngine(window1);
		doReturn(engine).when(handler).getEngine(window2);

		// when
		handler.handleEvent(event);

		// then
		verify(engine, times(1)).reapply();
		verify(registry, times(1)).invalidateResources(Font.class, Color.class);
	}

	protected static class ThemeDefinitionChangedHandlerTestable extends
			ThemeDefinitionChangedHandler {
		@Override
		public CSSEngine getEngine(MWindow window) {
			return super.getEngine(window);
		}
	}
}