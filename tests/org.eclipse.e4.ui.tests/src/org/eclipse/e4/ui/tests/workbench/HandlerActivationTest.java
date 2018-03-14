/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Helming - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon;
import org.eclipse.e4.ui.internal.workbench.addons.HandlerProcessingAddon;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

/**
 * Tests the activation of Handlers based on their Handler Container, e.g.
 * MWindow, MPerspective or MPart
 */
public class HandlerActivationTest extends TestCase {

	/**
	 * The ID for the test command
	 */
	private static final String COMMANDID = "handlerActivationTest";

	/**
	 * A Test Handler
	 */
	public interface TestHandler {
		public boolean isExecuted();
	}

	protected IEclipseContext appContext;
	protected E4Workbench wb;
	private MCommand command;
	private MWindow window;
	private EHandlerService handlerService;
	private ParameterizedCommand parameterizedCommand;
	private MPerspective perspectiveA;
	private MPart partA1;
	private MPerspective perspectiveB;
	private MPart partA2;
	private EPartService partService;
	private MPart partB1;

	@SuppressWarnings("static-access")
	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, appContext);
		appContext.set(E4Workbench.PRESENTATION_URI_ARG, PartRenderingEngine.engineURI);
		createLayoutWithThreeContextLayers();
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	/**
	 * Creates an example application model with one window and two perspectives
	 */
	public void createLayoutWithThreeContextLayers() {
		window = BasicFactoryImpl.eINSTANCE.createWindow();
		MPerspectiveStack perspectiveStack = MAdvancedFactory.INSTANCE.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		perspectiveA = MAdvancedFactory.INSTANCE.createPerspective();
		perspectiveB = MAdvancedFactory.INSTANCE.createPerspective();

		MPartStack stackA = MBasicFactory.INSTANCE.createPartStack();
		partA1 = MBasicFactory.INSTANCE.createPart();
		partA2 = MBasicFactory.INSTANCE.createPart();
		stackA.getChildren().add(partA1);
		stackA.getChildren().add(partA2);
		perspectiveA.getChildren().add(stackA);
		perspectiveStack.getChildren().add(perspectiveA);

		MPartStack stackB = MBasicFactory.INSTANCE.createPartStack();
		partB1 = MBasicFactory.INSTANCE.createPart();
		stackA.getChildren().add(partB1);
		stackA.setSelectedElement(partB1);
		perspectiveB.getChildren().add(stackB);
		perspectiveStack.getChildren().add(perspectiveB);

		perspectiveStack.setSelectedElement(perspectiveA);
		stackA.setSelectedElement(partA1);

		command = CommandsFactoryImpl.eINSTANCE.createCommand();
		command.setElementId(COMMANDID);
		command.setCommandName("Test Handler Activation");

		MApplication application = ApplicationFactoryImpl.eINSTANCE.createApplication();
		application.getCommands().add(command);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		ContextInjectionFactory.make(CommandProcessingAddon.class, appContext);
		ContextInjectionFactory.make(HandlerProcessingAddon.class, appContext);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		// force the part activation to ensure they have a context

		ECommandService commandService = appContext.get(ECommandService.class);
		handlerService = appContext.get(EHandlerService.class);
		parameterizedCommand = commandService.createCommand(COMMANDID, null);
		partService = appContext.get(EPartService.class);

	}

	public void testHandlerInWindowOnly() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(window);
		executeCommand();
		assertTrue(testHandler.isExecuted());
	}

	public void testHandlerInActivePerspectiveOnly() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(perspectiveA);
		executeCommand();
		assertTrue(testHandler.isExecuted());
	}

	public void testHandlerInActivePartOnly() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partA1);
		executeCommand();
		assertTrue(testHandler.isExecuted());
	}

	public void testHandlerInInactivePerspectiveOnly() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(perspectiveB);
		executeCommand();
		assertFalse(testHandler.isExecuted());
	}

	public void testHandlerInInactivePartOnly() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partA2);
		executeCommand();
		assertFalse(testHandler.isExecuted());
	}

	public void testHandlerInActivePartAndPerspective() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partA1);
		TestHandler testHandler2 = createTestHandlerInHandlerContainer(perspectiveA);
		executeCommand();
		assertTrue(testHandler.isExecuted());
		assertFalse(testHandler2.isExecuted());
	}

	public void testHandlerInActivePartAndWindow() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partA1);
		TestHandler testHandler2 = createTestHandlerInHandlerContainer(window);
		executeCommand();
		assertTrue(testHandler.isExecuted());
		assertFalse(testHandler2.isExecuted());
	}

	public void testHandlerInActivePerspectiveAndWindow() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(perspectiveA);
		TestHandler testHandler2 = createTestHandlerInHandlerContainer(window);
		executeCommand();
		assertTrue(testHandler.isExecuted());
		assertFalse(testHandler2.isExecuted());
	}

	public void testHandlerInActivePartAndPerspectiveAndWindow() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partA1);
		TestHandler testHandler2 = createTestHandlerInHandlerContainer(perspectiveA);
		TestHandler testHandler3 = createTestHandlerInHandlerContainer(window);
		executeCommand();
		assertTrue(testHandler.isExecuted());
		assertFalse(testHandler2.isExecuted());
		assertFalse(testHandler3.isExecuted());
	}

	public void testHandlerSwitchToInactivePart() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partA2);
		executeCommand();
		assertFalse(testHandler.isExecuted());
		partService.activate(partA2);
		executeCommand();
		assertTrue(testHandler.isExecuted());
	}

	public void testHandlerSwitchToInactivePerspective() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(perspectiveB);
		executeCommand();
		assertFalse(testHandler.isExecuted());
		partService.switchPerspective(perspectiveB);
		executeCommand();
		assertTrue(testHandler.isExecuted());
	}

	public void testHandlerSwitchToInactivePartInOtherPerspectiveWithPerspectiveHandlers() {
		TestHandler testHandler = createTestHandlerInHandlerContainer(partB1);
		TestHandler testHandler1 = createTestHandlerInHandlerContainer(perspectiveA);
		TestHandler testHandler2 = createTestHandlerInHandlerContainer(perspectiveB);
		executeCommand();
		assertFalse(testHandler.isExecuted());
		assertTrue(testHandler1.isExecuted());
		assertFalse(testHandler2.isExecuted());
		partService.activate(partB1);
		executeCommand();
		assertTrue(testHandler.isExecuted());
		assertTrue(testHandler1.isExecuted());
		assertFalse(testHandler2.isExecuted());
	}

	private TestHandler createTestHandlerInHandlerContainer(MHandlerContainer handlerContainer) {
		MHandler handler = CommandsFactoryImpl.eINSTANCE.createHandler();
		handler.setCommand(command);
		TestHandler testHandler = new TestHandler() {


			private boolean executed;

			@Execute
			public void execute() {
				executed = true;
			}

			@Override
			public boolean isExecuted() {
				// TODO Auto-generated method stub
				return executed;
			}
		};
		handler.setObject(testHandler);

		handlerContainer.getHandlers().add(handler);
		return testHandler;
	}

	/**
	 *
	 */
	private void executeCommand() {
		handlerService.executeHandler(parameterizedCommand);

	}

}
