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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
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
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ElementMatcher;
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
		URI uri = URI.createPlatformPluginURI(
				"org.eclipse.e4.ui.tests/xmi/SearchModelElement.e4xmi", true);
		ResourceSet set = new ResourceSetImpl();
		Resource resource = set.getResource(uri, true);

		assertNotNull(resource);
		assertEquals(E4XMIResource.class, resource.getClass());
		assertEquals(1, resource.getContents().size());
		MApplication app = (MApplication) resource.getContents().get(0);
		app.setContext(applicationContext);

		return app;
	}

	private Selector getSelector(String id, Class<?> clazz, List<String> tags) {
		return new ElementMatcher(id, clazz, tags);
	}

	private Selector getSelector(String id) {
		return getSelector(id, null, null);
	}

	private Selector getSelector(Class<?> clazz) {
		return getSelector(null, clazz, null);
	}

	private Selector getSelector() {
		return getSelector(null, null, null);
	}

	private Selector getSelector(List<String> tags) {
		return getSelector(null, null, tags);
	}

	public void testFindElementsIdOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<? extends MApplicationElement> elements;

		elements = modelService.findElements(application, null,
				EModelService.ANYWHERE, getSelector("singleValidId"));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, null,
				EModelService.ANYWHERE, getSelector("twoValidIds"));
		assertEquals(2, elements.size());

		elements = modelService.findElements(application, null,
				EModelService.ANYWHERE, getSelector("invalidId"));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU
						| EModelService.IN_PART, getSelector("menuItem1Id"));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU
						| EModelService.IN_PART, getSelector("toolControl1Id"));
		assertEquals(1, elements.size());
	}

	public void testFindElementsTypeOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<? extends MApplicationElement> elements;

		elements = modelService.findElements(application, MPart.class,
				EModelService.ANYWHERE, getSelector(MPart.class));
		assertEquals(5, elements.size());

		elements = modelService.findElements(application, MPartStack.class,
				EModelService.ANYWHERE, getSelector(MPartStack.class));
		assertEquals(3, elements.size());

		List<MDirtyable> dirtyableElements = modelService.findElements(
				application, MDirtyable.class, EModelService.ANYWHERE,
				getSelector(MDirtyable.class));
		assertEquals(5, dirtyableElements.size());

		elements = modelService.findElements(application, MMenuElement.class,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU
						| EModelService.IN_PART,
				getSelector(MMenuElement.class));
		assertEquals(14, elements.size());

		elements = modelService.findElements(application,
				MToolBarElement.class, EModelService.ANYWHERE
						| EModelService.IN_MAIN_MENU | EModelService.IN_PART,
				getSelector(MToolBarElement.class));
		assertEquals(2, elements.size());

		// Should find all the elements
		elements = modelService.findElements(application, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU
						| EModelService.IN_PART, getSelector());
		assertEquals(65, elements.size());

		elements = modelService.findElements(application, null,
				EModelService.OUTSIDE_PERSPECTIVE,
				getSelector("InsideOutsidePerspective"));
		assertEquals(1, elements.size());

		// Should match 0 since String is not an MUIElement
		List<String> strings = modelService.findElements(application, null,
				String.class, null);
		assertEquals(strings.size(), 0);
	}

	public void testFindElementsTagsOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MUIElement> oneTags = modelService.findElements(application, null,
				EModelService.ANYWHERE, getSelector(tags));
		assertEquals(oneTags.size(), 1);

		tags.clear();
		tags.add("twoValidTags");
		List<MUIElement> twoTags = modelService.findElements(application, null,
				EModelService.ANYWHERE, getSelector(tags));
		assertEquals(twoTags.size(), 2);

		tags.clear();
		tags.add("invalidTag");
		List<MUIElement> invalidTags = modelService.findElements(application,
				null, EModelService.ANYWHERE, getSelector(tags));
		assertEquals(invalidTags.size(), 0);

		tags.clear();
		tags.add("twoValidTags");
		tags.add("secondTag");
		List<MUIElement> combinedTags = modelService.findElements(application,
				null, EModelService.ANYWHERE, getSelector(tags));
		assertEquals(combinedTags.size(), 1);

		tags.clear();
		tags.add("oneValidTag");
		tags.add("secondTag");
		List<MUIElement> unmatchedTags = modelService.findElements(application,
				null, EModelService.ANYWHERE, getSelector(tags));
		assertEquals(unmatchedTags.size(), 0);
	}

	public void testFindElementsCombinations() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<? extends MApplicationElement> elements;

		elements = modelService.findElements(application, null,
				EModelService.IN_PART, getSelector("singleValidId"));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application,
				MPartSashContainer.class, EModelService.ANYWHERE,
				getSelector("twoValidIds", MPartSashContainer.class, tags));
		assertEquals(1, elements.size());

		List<MPartSashContainer> typeAndTag = modelService.findElements(
				application, MPartSashContainer.class, EModelService.ANYWHERE,
				getSelector(tags));
		assertEquals(1, typeAndTag.size());

		elements = modelService.findElements(application,
				MPartSashContainer.class, EModelService.ANYWHERE,
				getSelector("twoValidIds"));
		assertEquals(1, elements.size());

		List<MUIElement> idAndTag = modelService.findElements(application,
				null, EModelService.ANYWHERE,
				getSelector("twoValidIds", null, tags));
		assertEquals(1, idAndTag.size());

		List<MPartSashContainer> idAndTypeAndTags = modelService.findElements(
				application, MPartSashContainer.class, EModelService.ANYWHERE,
				getSelector("twoValidIds", MPartSashContainer.class, null));
		assertEquals(1, idAndTypeAndTags.size());

		List<MPartSashContainer> badIdAndTypeAndTags = modelService
				.findElements(application, MPartSashContainer.class,
						EModelService.ANYWHERE, getSelector("invalidId"));
		assertEquals(0, badIdAndTypeAndTags.size());
	}

	public void testFindElements_NullCheck() {
		MApplication application = createApplication();
		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
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
			modelService.findElements(null, null, null, null,
					EModelService.ANYWHERE);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			modelService.findElements(null, null, EModelService.ANYWHERE, null);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testFlags() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<? extends MApplicationElement> clazz;
		List<? extends MApplicationElement> elements;

		clazz = MToolBarElement.class;
		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_PART,
				getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector(clazz));
		assertEquals(2, elements.size());

		clazz = MMenuElement.class;
		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(5, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_PART,
				getSelector(clazz));
		assertEquals(5, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE, getSelector(clazz));
		assertEquals(4, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE | EModelService.IN_PART,
				getSelector(clazz));
		assertEquals(4, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_MAIN_MENU,
				getSelector(clazz));
		assertEquals(12, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_MAIN_MENU, getSelector(clazz));
		assertEquals(7, elements.size());
	}

	public void testFindHandler() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<? extends MApplicationElement> clazz = MHandler.class;
		List<? extends MApplicationElement> elements;

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector("handler1", clazz, null));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_PART, getSelector("handler1", clazz, null));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector("invalidId", clazz, null));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, null, MHandler.class,
				null);
		assertEquals(8, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector(clazz));
		assertEquals(8, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_PART, getSelector(clazz));
		assertEquals(4, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(4, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE, getSelector(clazz));
		assertEquals(3, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_TRIM, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_SHARED_AREA, getSelector(clazz));
		assertEquals(1, elements.size());
	}

	public void testBug314685() {
		MApplication application = createApplication();
		application.setContext(applicationContext);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(partStack);
		perspectiveA.getChildren().add(placeholderA);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(partStack);
		perspectiveB.getChildren().add(placeholderB);

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MPart> elements = modelService.findElements(window, null,
				MPart.class, null);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		assertEquals(part, elements.get(0));
	}

	public void testFind_MCommands() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<MCommand> clazz = MCommand.class;
		List<MCommand> elements = null;

		elements = modelService.findElements(application, null, clazz, null,
				EModelService.ANYWHERE);
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, null, clazz, null,
				EModelService.IN_PART);
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, null, clazz, null,
				EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, null, clazz, null,
				EModelService.IN_ACTIVE_PERSPECTIVE);
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, null, clazz, null,
				EModelService.IN_TRIM);
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, null, clazz, null,
				EModelService.IN_SHARED_AREA);
		assertEquals(0, elements.size());
	}

	public void testFind_MBindingContext() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<MBindingContext> clazz = MBindingContext.class;
		List<MBindingContext> elements = null;

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE,
				getSelector("org.eclipse.ui.contexts.window", clazz, null));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector(clazz));
		assertEquals(3, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.OUTSIDE_PERSPECTIVE, getSelector(clazz));
		assertEquals(3, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_PART, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_TRIM, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_SHARED_AREA, getSelector(clazz));
		assertEquals(0, elements.size());
	}

	public void testFind_MBindingTable() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<MBindingTable> clazz = MBindingTable.class;
		List<MBindingTable> elements = null;

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector(clazz));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.OUTSIDE_PERSPECTIVE, getSelector(clazz));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_PART, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_TRIM, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_SHARED_AREA, getSelector(clazz));
		assertEquals(0, elements.size());
	}

	public void testFind_MAddons() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<MAddon> clazz = MAddon.class;
		List<MAddon> elements = null;

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector(clazz));
		assertEquals(7, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.OUTSIDE_PERSPECTIVE, getSelector(clazz));
		assertEquals(7, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_PART, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_TRIM, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_SHARED_AREA, getSelector(clazz));
		assertEquals(0, elements.size());
	}

	public void testFind_MCategory() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		Class<MCategory> clazz = MCategory.class;
		List<MCategory> elements = null;

		elements = modelService.findElements(application, clazz,
				EModelService.ANYWHERE, getSelector(clazz));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.OUTSIDE_PERSPECTIVE, getSelector(clazz));
		assertEquals(1, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_PART, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ANY_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_ACTIVE_PERSPECTIVE, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_TRIM, getSelector(clazz));
		assertEquals(0, elements.size());

		elements = modelService.findElements(application, clazz,
				EModelService.IN_SHARED_AREA, getSelector(clazz));
		assertEquals(0, elements.size());
	}
}
