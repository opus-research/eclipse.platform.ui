/*******************************************************************************
 * Copyright (c) 2016 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alexandra Buzila - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.ExtensionsSort;
import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("nls")
public class ModelAssemblerTests {
	final private static String extensionPointID = "org.eclipse.e4.workbench.model";
	final private static String bundleSymbolicName = "org.eclipse.e4.ui.tests";
	final private static String appId = "org.eclipse.e4.ui.tests.modelassembler.app";
	private IEclipseContext appContext;
	private MApplication application;
	private E4XMIResourceFactory factory;
	private ResourceSetImpl resourceSet;
	private E4XMIResource appResource;
	private ModelAssembler assembler;
	private Logger logger;

	@Before
	public void setup() {

		appContext = E4Application.createDefaultContext();
		application = ApplicationFactoryImpl.eINSTANCE.createApplication();
		application.setElementId(appId);
		application.setContext(appContext);

		logger = mock(Logger.class);

		appContext.set(Logger.class, logger);
		appContext.set(MApplication.class, application);

		factory = new E4XMIResourceFactory();
		appResource = (E4XMIResource) factory.createResource(URI.createURI("virtualuri"));
		resourceSet = new ResourceSetImpl();
		resourceSet.getResources().add(appResource);
		appResource.getContents().add((EObject) application);
		assembler = new ModelAssembler();
		ContextInjectionFactory.inject(assembler, appContext);
	}

	@Test
	/**
	 * Test the handling of a fragment contribution with no elements to merge.
	 */
	public void testFragments_emptyFragment() throws Exception {
		MModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		final String contributorURI = "testFragments_emptyFragment_contribURI";

		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorURI, true);
		assertTrue(elements.isEmpty());

		EModelService modelService = application.getContext().get(EModelService.class);
		List<MApplicationElement> modelElements = modelService.findElements(application, MApplicationElement.class,
				EModelService.ANYWHERE, new Selector() {
					@Override
					public boolean select(MApplicationElement element) {
						return element.getContributorURI() != null
								&& element.getContributorURI().equals(contributorURI);
					}
				});
		assertTrue(modelElements.isEmpty());

		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests that fragments are correctly contributed to the application model.
	 */
	public void testFragments_workingFragment() throws Exception {
		// the contributed element
		MWindow window = MBasicFactory.INSTANCE.createWindow();
		final String contributedElementId = "testFragments_workingFragment-contributedWindow";
		window.setElementId(contributedElementId);

		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		final String fragmentParentId = "org.eclipse.e4.ui.tests.modelassembler.app";
		fragment.setParentElementId(fragmentParentId);
		fragment.getElements().add(window);
		// add fragment to resource
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		EModelService modelService = application.getContext().get(EModelService.class);
		assertEquals(null, modelService.find(contributedElementId, application));

		final String contributorURI = "testFragments_emptyFragment_contribURI";
		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorURI, false);

		assertEquals(window, modelService.find(contributedElementId, application));
		assertEquals(1, elements.size());
		assertEquals(contributorURI, elements.get(0).getContributorURI());
		assertTrue(elements.contains(window));
		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(window, found);
		assertEquals(fragmentParentId, found.getParent().getElementId());

		verifyZeroInteractions(logger);


	}

	@Test
	@Ignore // currently ignored due to bug 487748
	public void testFragments_existingXMIID_checkExists() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId("org.eclipse.e4.ui.tests.modelassembler.app");
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String contributedElementId = "testFragments_existingElementID-contributedWindow";
		MWindow window1 = MBasicFactory.INSTANCE.createWindow();
		window1.setElementId(contributedElementId);
		MWindow window2 = MBasicFactory.INSTANCE.createWindow();
		window2.setElementId(contributedElementId);

		// add window1 to app and window2 to fragment
		application.getChildren().add(window1);
		fragment.getElements().add(window2);

		// set the same resource xmi id to window1 and window2
		final String xmiId = "testFragments_existingXMIID_XMIID";
		appResource.setID((EObject) window1, xmiId);
		fragmentResource.setID((EObject) window2, xmiId);
		final String contributorURI = "testFragments_existingElementID_contribURI";
		window1.setContributorURI(contributorURI);
		window2.setContributorURI(contributorURI);
		List<MApplicationElement> elements = assembler.processModelFragment(fragment,
				contributorURI, true);

		// fragment wasn't merged as the contributed element was already part of
		// the application model
		assertEquals(0, elements.size());
		EModelService modelService = application.getContext().get(EModelService.class);
		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(window1, found);

		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests that fragments configured to be always merged are correctly
	 * contributed to the application model, even if the model already contains
	 * the contributed element.
	 */
	public void testFragments_existingXMIID_ignoreExists() throws Exception {
		// create fragment
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename("children");
		fragment.setParentElementId("org.eclipse.e4.ui.tests.modelassembler.app");
		// create fragment resource
		E4XMIResource fragmentResource = (E4XMIResource) factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragment);

		final String contributedElementId = "testFragments_existingElementID-contributedWindow";
		MWindow window1 = MBasicFactory.INSTANCE.createWindow();
		window1.setElementId(contributedElementId);
		MWindow window2 = MBasicFactory.INSTANCE.createWindow();
		window2.setElementId(contributedElementId);

		// add window1 to app and window2 to fragment
		application.getChildren().add(window1);
		fragment.getElements().add(window2);

		// set the same resource xmi id to window1 and window2
		final String xmiId = "testFragments_existingXMIID_XMIID";
		appResource.setID((EObject) window1, xmiId);
		fragmentResource.setID((EObject) window2, xmiId);

		final String contributorID = "testFragments_existingElementID_contribURI";
		List<MApplicationElement> elements = assembler.processModelFragment(fragment, contributorID, false);

		assertEquals(elements.size(), 1);
		EModelService modelService = application.getContext().get(EModelService.class);
		MUIElement found = modelService.find(contributedElementId, application);
		assertEquals(found, window2);
		assertEquals(contributorID, found.getContributorURI());

		verifyZeroInteractions(logger);
	}

	@Test
	/** Tests that correctly configured imports are correctly handled. */
	public void testImports() {
		List<MApplicationElement> imports = new ArrayList<MApplicationElement>();
		List<MApplicationElement> addedElements = new ArrayList<MApplicationElement>();

		final String windowElementId = "testImports_emptyList_window1";
		MTrimmedWindow importWindow1 = MBasicFactory.INSTANCE.createTrimmedWindow();
		importWindow1.setElementId(windowElementId);
		MModelFragments fragment = MFragmentFactory.INSTANCE.createModelFragments();
		fragment.getImports().add(importWindow1);
		imports.add(importWindow1);

		MTrimmedWindow realWindow1 = MBasicFactory.INSTANCE.createTrimmedWindow();
		realWindow1.setElementId(windowElementId);
		application.getChildren().add(realWindow1);

		MPlaceholder placeholder = MAdvancedFactory.INSTANCE.createPlaceholder();
		placeholder.setRef(importWindow1);
		addedElements.add(placeholder);

		assembler.resolveImports(imports, addedElements);
		assertEquals(realWindow1, placeholder.getRef());
		verifyZeroInteractions(logger);
	}

	/** Tests the processing of an import with a null/incorrect element id. */
	@Test
	public void testImports_noImportElementId() {
		List<MApplicationElement> imports = new ArrayList<MApplicationElement>();
		List<MApplicationElement> addedElements = new ArrayList<MApplicationElement>();

		MTrimmedWindow importWindow1 = MBasicFactory.INSTANCE.createTrimmedWindow();
		importWindow1.setElementId(null);
		MModelFragments fragment = MFragmentFactory.INSTANCE.createModelFragments();
		fragment.getImports().add(importWindow1);
		imports.add(importWindow1);
		MTrimmedWindow realWindow1 = MBasicFactory.INSTANCE.createTrimmedWindow();
		realWindow1.setElementId("testImports_emptyList_window1");
		application.getChildren().add(realWindow1);

		MPlaceholder placeholder = MAdvancedFactory.INSTANCE.createPlaceholder();
		placeholder.setRef(importWindow1);
		addedElements.add(placeholder);

		assembler.resolveImports(imports, addedElements);
		assertEquals(null, placeholder.getRef());
		verify(logger).warn("Could not resolve an import element for 'null'");
		verify(logger).warn("Could not resolve import for null");
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests that pre-processors running from a non-persisted state that are
	 * marked as "always" are executed.
	 */
	public void testPreProcessor_nonPersistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", true, false);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.pre", application.getDescriptors().get(0).getElementId());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests that pre-processors running from a persisted state that are marked
	 * as "always" are executed.
	 */
	public void testPreProcessor_persistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", false, false);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.pre", application.getDescriptors().get(0).getElementId());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests that pre-processors running from a non-persisted state and marked
	 * as "initial" are executed.
	 */
	public void testPreProcessor_nonPersistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", true, false);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.pre", application.getDescriptors().get(0).getElementId());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests that pre-processors running from a persisted state and marked as
	 * "initial" are not executed.
	 */
	public void testPreProcessor_persistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", false, false);
		assertEquals(0, application.getDescriptors().size());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests the execution of post-processors that should always be applied,
	 * running from a persisted state.
	 */
	public void testPostProcessor_persistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", false, true);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests the execution of post-processors that should always be applied,
	 * running from a non-persisted state.
	 */
	public void testPostProcessor_nonPersistedState_always() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_always.xml", true, true);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests the execution of post-processors running from a non-persisted state
	 * declared to be applied as "initial".
	 */
	public void testPostProcessor_NonPersistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", true, true);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Processors running from a persisted state declared to be applied as
	 * "initial" should not be run.
	 */
	public void testPostProcessor_persistedState_initial() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", false, true);
		assertEquals(0, application.getDescriptors().size());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Test handling of processor contribution without any processor class. A
	 * warning should be logged in such cases.
	 */
	public void testProcessor_noProcessor() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processor_null.xml", true, false);
		verify(logger).warn("Unable to create processor null from org.eclipse.e4.ui.tests");
		assertEquals(0, application.getDescriptors().size());
		verifyZeroInteractions(logger);
	}

	@Test
	/**
	 * Tests a contribution containing an nonexistent processor class. A warning
	 * should be logged in such cases.
	 */
	public void testProcessor_processorNotFound() throws Exception {
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processor_wrongProcessorClass.xml", true, false);
		verify(logger).warn(
				"Unable to create processor org.eclipse.e4.ui.tests.workbench.SimplePreProcessor_NotFound from org.eclipse.e4.ui.tests");
		assertEquals(0, application.getDescriptors().size());
		verifyZeroInteractions(logger);
	}


	@Test
	/**
	 * Tests a processor contribution that adds to the context an element with
	 * an id that does not exist in the application model. A warning should be
	 * logged, but the processors should still be executed.
	 */
	public void testProcessor_wrongAppId() throws Exception {
		application.setElementId("newID");
		testProcessor("org.eclipse.e4.ui.tests/data/ModelAssembler/processors_initial.xml", true, true);
		verify(logger).warn("Could not find element with id 'org.eclipse.e4.ui.tests.modelassembler.app'");
		verifyZeroInteractions(logger);
		assertEquals(1, application.getDescriptors().size());
		assertEquals("simpleprocessor.post", application.getDescriptors().get(0).getElementId());
	}

	private void testProcessor(String filePath, boolean initial, boolean afterFragments) throws Exception {
		IContributor contributor = ContributorFactorySimple.createContributor(bundleSymbolicName);
		IExtensionRegistry registry = createTestExtensionRegistry();
		assertEquals(0, registry.getConfigurationElementsFor(extensionPointID).length);
		registry.addContribution(getContentsAsInputStream(filePath), contributor, false, null, null, null);
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = new ExtensionsSort().sort(extPoint.getExtensions());
		assertEquals(0, application.getDescriptors().size());
		assembler.runProcessors(extensions, initial, afterFragments);
	}

	private IExtensionRegistry createTestExtensionRegistry() {
		IExtensionRegistry defaultRegistry = RegistryFactory.getRegistry();
		IExtensionPoint extensionPoint = defaultRegistry.getExtensionPoint(extensionPointID);
		ExtensionRegistry registry = (ExtensionRegistry) RegistryFactory.createRegistry(null, null, null);
		registry.addExtensionPoint(extensionPoint.getUniqueIdentifier(), extensionPoint.getContributor(), false,
				extensionPoint.getLabel(), extensionPoint.getSchemaReference(), null);
		appContext.set(IExtensionRegistry.class, registry);
		return registry;
	}

	private InputStream getContentsAsInputStream(String filePath) throws IOException {
		URI uri = URI.createPlatformPluginURI(filePath, true);
		return URIConverter.INSTANCE.createInputStream(uri);
	}
}
