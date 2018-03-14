/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.ui.model.application.commands.MBindingTable;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class EModelServiceFindTest extends TestCase {

	private IEclipseContext applicationContext;

	MApplication app = null;

	@Override
	protected void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		applicationContext.dispose();
	}

	private MApplication createApplication() {
		URI uri = URI.createPlatformPluginURI("org.eclipse.e4.ui.tests/xmi/SearchModelElement.e4xmi", true);
		ResourceSet set = new ResourceSetImpl();
		Resource resource = set.getResource(uri, true);

		assertNotNull(resource);
		assertEquals(E4XMIResource.class, resource.getClass());
		assertEquals(1, resource.getContents().size());
		MApplication app = (MApplication) resource.getContents().get(0);
		app.setContext(applicationContext);

		return app;
	}

	public void testFindElementsIdOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MUIElement> elements1 = modelService.findElements(application,"singleValidId", null, null);
		assertEquals(1, elements1.size());

		List<MUIElement> elements2 = modelService.findElements(application,"twoValidIds", null, null);
		assertEquals(2, elements2.size());

		List<MUIElement> elements3 = modelService.findElements(application,"invalidId", null, null);
		assertEquals(0, elements3.size());

		List<MUIElement> elements4 = modelService.findElements(application, "menuItem1Id", null, null, EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(1, elements4.size());

		List<MUIElement> elements5 = modelService.findElements(application, "toolControl1Id", null, null, EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(1, elements5.size());
	}

	public void testFindElementsTypeOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MPart> parts = modelService.findElements(application, null, MPart.class, null);
		assertEquals(5, parts.size());

		List<MPartStack> stacks = modelService.findElements(application, null, MPartStack.class, null);
		assertEquals(3, stacks.size());

		List<MDirtyable> dirtyableElements = modelService.findElements(application, null, MDirtyable.class, null);
		assertEquals(5, dirtyableElements.size());

		List<MMenuElement> menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(13, menuElements.size());

		List<MToolBarElement> toolBarElements = modelService.findElements(application, null, MToolBarElement.class, null, EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(2, toolBarElements.size());

		// Should find all the elements
		List<MUIElement> uiElements = modelService.findElements(application,null, null, null, EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(40, uiElements.size());

		// Should match 0 since String is not an MUIElement
		List<String> strings = modelService.findElements(application, null, String.class, null);
		assertEquals(strings.size(), 0);
	}

	public void testFindElementsTagsOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MUIElement> oneTags = modelService.findElements(application, null, null, tags);
		assertEquals(oneTags.size(), 1);

		tags.clear();
		tags.add("twoValidTags");
		List<MUIElement> twoTags = modelService.findElements(application, null, null, tags);
		assertEquals(twoTags.size(), 2);

		tags.clear();
		tags.add("invalidTag");
		List<MUIElement> invalidTags = modelService.findElements(application, null, null, tags);
		assertEquals(invalidTags.size(), 0);

		tags.clear();
		tags.add("twoValidTags");
		tags.add("secondTag");
		List<MUIElement> combinedTags = modelService.findElements(application, null, null, tags);
		assertEquals(combinedTags.size(), 1);

		tags.clear();
		tags.add("oneValidTag");
		tags.add("secondTag");
		List<MUIElement> unmatchedTags = modelService.findElements(application, null, null, tags);
		assertEquals(unmatchedTags.size(), 0);
	}

	public void testFindElementsCombinations() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext() .get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MPartSashContainer> idAndType = modelService.findElements(application, "twoValidIds", MPartSashContainer.class, tags);
		assertEquals(idAndType.size(), 1);

		List<MPartSashContainer> typeAndTag = modelService.findElements(application, null, MPartSashContainer.class, tags);
		assertEquals(typeAndTag.size(), 1);

		List<MUIElement> idAndTag = modelService.findElements(application, "twoValidIds", null, tags);
		assertEquals(idAndTag.size(), 1);

		List<MPartSashContainer> idAndTypeAndTags = modelService.findElements(application, "twoValidIds", MPartSashContainer.class, null);
		assertEquals(idAndTypeAndTags.size(), 1);

		List<MPartSashContainer> badIdAndTypeAndTags = modelService.findElements(application, "invalidId", MPartSashContainer.class, null);
		assertEquals(badIdAndTypeAndTags.size(), 0);
	}

	public void testFindElements_NullCheck() {
		MApplication application = createApplication();
		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);

		try {
			modelService.find("a", null);
			fail("An exception should have prevented a null parameter to find(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			modelService.findElements(null, null, null, null);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			modelService.findElements(null, null, null, null,EModelService.ANYWHERE);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testFlags() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MToolBarElement> toolBarElements = modelService.findElements(application, null, MToolBarElement.class, null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, toolBarElements.size());
		
		toolBarElements = modelService.findElements(application, null,MToolBarElement.class, null, EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_PART);
		assertEquals(0, toolBarElements.size());
		
		toolBarElements = modelService.findElements(application, null, MToolBarElement.class, null, EModelService.ANYWHERE);
		assertEquals(2, toolBarElements.size());

		List<MMenuElement> menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(4, menuElements.size());

		menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_PART);
		assertEquals(4, menuElements.size());
		
		menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.IN_ACTIVE_PERSPECTIVE );
		assertEquals(3, menuElements.size());
		
		menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.IN_ACTIVE_PERSPECTIVE | EModelService.IN_PART);
		assertEquals(3, menuElements.size());

		menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_MAIN_MENU);
		assertEquals(13, menuElements.size());

		menuElements = modelService.findElements(application, null, MMenuElement.class, null, EModelService.IN_MAIN_MENU);
		assertEquals(9, menuElements.size());
	}

	public void testFindHandler() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);


		List<MHandler> foundHandlers = null;

		foundHandlers = modelService.findElements(application, "handler1", MHandler.class, null);
		assertNotNull(foundHandlers);
		
		foundHandlers = modelService.findElements(application, "handler1", MHandler.class, null, EModelService.IN_PART);
		assertTrue(foundHandlers.isEmpty());

		foundHandlers = modelService.findElements(application, "invalidId", MHandler.class, null);
		assertTrue(foundHandlers.isEmpty());

		foundHandlers = modelService.findElements(application, "", MHandler.class, null);
		assertTrue(foundHandlers.isEmpty());

		foundHandlers = modelService.findElements(application, null, MHandler.class, null);
		assertEquals(8, foundHandlers.size());
		
		foundHandlers = modelService.findElements(application, null, MHandler.class, null, EModelService.ANYWHERE);
		assertEquals(8, foundHandlers.size());
		
		foundHandlers = modelService.findElements(application, null, MHandler.class, null, EModelService.IN_PART);
		assertEquals(4, foundHandlers.size());
		
		foundHandlers = modelService.findElements(application, null, MHandler.class, null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(4, foundHandlers.size());
		
		foundHandlers = modelService.findElements(application, null, MHandler.class, null, EModelService.IN_ACTIVE_PERSPECTIVE);
		assertEquals(3, foundHandlers.size());
		
		foundHandlers = modelService.findElements(application, null, MHandler.class, null, EModelService.IN_TRIM);
		assertEquals(0, foundHandlers.size());
		
		foundHandlers = modelService.findElements(application, null, MHandler.class, null, EModelService.IN_SHARED_AREA);
		assertEquals(1, foundHandlers.size());
	}

	public void testBug314685() {
		MApplication application = createApplication();
		application.setContext(applicationContext);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		placeholderA.setRef(partStack);
		perspectiveA.getChildren().add(placeholderA);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		placeholderB.setRef(partStack);
		perspectiveB.getChildren().add(placeholderB);

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MPart> elements = modelService.findElements(window, null,MPart.class, null);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		assertEquals(part, elements.get(0));
	}
	
	public void testFind_MCommands() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);
		
		Class<MCommand> clazz = MCommand.class;
		List<MCommand> elements = null;
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.ANYWHERE);
		assertEquals(1, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_PART);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_ACTIVE_PERSPECTIVE);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_TRIM);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_SHARED_AREA);
		assertEquals(0, elements.size());
	}
	
	public void testFind_MBindingContext() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);
		
		Class<MBindingContext> clazz = MBindingContext.class;
		List<MBindingContext> elements = null;
		
		elements = modelService.findElements(application, "org.eclipse.ui.contexts.window", clazz, null, EModelService.ANYWHERE);
		assertEquals(1, elements.size());
		
		elements = modelService.findElements(application, "org.eclipse.ui.contexts.window", null, null, EModelService.ANYWHERE);
		assertEquals(1, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.ANYWHERE);
		assertEquals(3, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_PART);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_ACTIVE_PERSPECTIVE);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_TRIM);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_SHARED_AREA);
		assertEquals(0, elements.size());
	}
	
	public void testFind_MBindingTable() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext().get(EModelService.class.getName());
		assertNotNull(modelService);
		
		Class<MBindingTable> clazz = MBindingTable.class;
		List<MBindingTable> elements = null;
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.ANYWHERE);
		assertEquals(1, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_PART);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_ACTIVE_PERSPECTIVE);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_TRIM);
		assertEquals(0, elements.size());
		
		elements = modelService.findElements(application, null, clazz, null, EModelService.IN_SHARED_AREA);
		assertEquals(0, elements.size());
	}
}
