/*******************************************************************************
 * Copyright (c) 2017 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation - http://eclip.se/8519
 *******************************************************************************/
package org.eclipse.e4.ui.macros.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.macros.EMacroContextService;
import org.eclipse.e4.core.macros.internal.MacroContextServiceImplementation;
import org.eclipse.e4.core.macros.internal.MacroContextServiceCreationFunction;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.macros.EAcceptedCommands;
import org.eclipse.e4.ui.macros.internal.AcceptedCommandsServiceCreationFunction;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("restriction")
public class KeyBindingDispatcherMacroIntegrationTest {

	private static final String ID_DIALOG = "org.eclipse.ui.contexts.dialog";
	private static final String ID_DIALOG_AND_WINDOW = "org.eclipse.ui.contexts.dialogAndWindow";
	private static final String ID_WINDOW = "org.eclipse.ui.contexts.window";

	final static String[] CONTEXTS = { ID_DIALOG_AND_WINDOW, "DAW", null, ID_DIALOG, "Dialog", ID_DIALOG_AND_WINDOW,
			ID_WINDOW, "Window", ID_DIALOG_AND_WINDOW, };

	private static final String TEST_CAT1 = "test.cat1";
	private static final String TEST_ID1 = "test.id1";

	static class CallHandler {
		public boolean q1;
		public boolean q2;

		@CanExecute
		public boolean canExecute() {
			q1 = true;
			return true;
		}

		@Execute
		public Object execute() {
			q2 = true;
			if (q1) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
	}

	private Display display;
	private IEclipseContext workbenchContext;
	private CallHandler handler;
	private File fMacrosDirectory;

	private void defineCommands(IEclipseContext context) {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		Category category = cs.defineCategory(TEST_CAT1, "CAT1", null);
		cs.defineCommand(TEST_ID1, "ID1", null, category, null);
		ParameterizedCommand cmd = cs.createCommand(TEST_ID1, null);
		EHandlerService hs = workbenchContext.get(EHandlerService.class);
		handler = new CallHandler();
		hs.activateHandler(TEST_ID1, handler);
		EBindingService bs = workbenchContext.get(EBindingService.class);
		TriggerSequence seq = bs.createSequence("CTRL+A");
		Binding db = createDefaultBinding(bs, seq, cmd);
		bs.activateBinding(db);
	}

	private Binding createDefaultBinding(EBindingService bs, TriggerSequence sequence, ParameterizedCommand command) {

		Map<String, String> attrs = new HashMap<>();
		attrs.put("schemeId", "org.eclipse.ui.defaultAcceleratorConfiguration");

		return bs.createBinding(sequence, command, ID_WINDOW, attrs);
	}

	@Before
	public void setUp() {
		// Dispose of the current default to make sure the current state doesn't
		// mess with our tests.
		display = Display.getDefault();
		display.dispose();

		display = Display.getDefault();
		IEclipseContext globalContext = Activator.getDefault().getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, workbenchContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, workbenchContext);
		defineContexts(workbenchContext);
		defineBindingTables(workbenchContext);
		defineCommands(workbenchContext);

		KeyBindingDispatcher dispatcher = new KeyBindingDispatcher();
		workbenchContext.set(KeyBindingDispatcher.class, dispatcher);
		ContextInjectionFactory.inject(dispatcher, workbenchContext);

		workbenchContext.set(EMacroContextService.class.getName(), new MacroContextServiceCreationFunction());
		workbenchContext.set(EAcceptedCommands.class.getName(), new AcceptedCommandsServiceCreationFunction());

		final Listener listener = dispatcher.getKeyDownFilter();
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);

		assertFalse(handler.q2);

		fMacrosDirectory = folder.getRoot();
		EMacroContextService macroContext = workbenchContext.get(EMacroContextService.class);
		((MacroContextServiceImplementation) macroContext).getMacroManager().setMacrosDirectories(fMacrosDirectory);
	}

	private void defineContexts(IEclipseContext context) {
		ContextManager contextManager = context.get(ContextManager.class);
		for (int i = 0; i < CONTEXTS.length; i += 3) {
			Context c = contextManager.getContext(CONTEXTS[i]);
			c.define(CONTEXTS[i + 1], null, CONTEXTS[i + 2]);
		}

		EContextService cs = context.get(EContextService.class);
		cs.activateContext(ID_DIALOG_AND_WINDOW);
		cs.activateContext(ID_WINDOW);
	}

	private void defineBindingTables(IEclipseContext context) {
		BindingTableManager btm = context.get(BindingTableManager.class);
		ContextManager cm = context.get(ContextManager.class);
		btm.addTable(new BindingTable(cm.getContext(ID_DIALOG_AND_WINDOW)));
		btm.addTable(new BindingTable(cm.getContext(ID_WINDOW)));
		btm.addTable(new BindingTable(cm.getContext(ID_DIALOG)));
	}

	@After
	public void tearDown() {
		workbenchContext.dispose();
		workbenchContext = null;
		display.dispose();
		display = null;
	}

	private void notifyCtrlA(Shell shell) {
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.CTRL;
		shell.notifyListeners(SWT.KeyDown, event);

		event = new Event();
		event.type = SWT.KeyDown;
		event.stateMask = SWT.CTRL;
		event.keyCode = 'A';
		shell.notifyListeners(SWT.KeyDown, event);
	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testMacroIntegration() throws Exception {
		EMacroContextService macroContext = workbenchContext.get(EMacroContextService.class);
		EAcceptedCommands acceptedCommands = workbenchContext.get(EAcceptedCommands.class);
		Shell shell = new Shell(display, SWT.NONE);

		// Whitelist command and record
		HashMap<String, Boolean> macroAcceptedCommandIds = new HashMap<>();
		macroAcceptedCommandIds.put(TEST_ID1, true);
		setAcceptedMacroCommandIds(acceptedCommands, macroAcceptedCommandIds);

		macroContext.toggleMacroRecord();
		assertTrue(macroContext.isRecording());

		notifyCtrlA(shell);
		assertTrue(handler.q2);

		macroContext.toggleMacroRecord();
		assertFalse(macroContext.isRecording());

		handler.q2 = false;
		macroContext.playbackLastMacro();
		assertTrue(handler.q2);

		// Blacklist command
		handler.q2 = false;
		setAcceptedMacroCommandIds(acceptedCommands, new HashMap<>());

		macroContext.toggleMacroRecord();
		assertTrue(macroContext.isRecording());
		notifyCtrlA(shell);
		macroContext.toggleMacroRecord();
		assertFalse(handler.q2);
	}

	private void setAcceptedMacroCommandIds(EAcceptedCommands acceptedCommands,
			Map<String, Boolean> macroAcceptedCommandIds) throws Exception {

		// Blacklist all commands there
		Map<String, Boolean> map = acceptedCommands.getCommandsAccepted();
		for (String commandId : map.keySet()) {
			acceptedCommands.setCommandAccepted(commandId, false, false);
		}

		// Whitelist the ones we want.
		for (Entry<String, Boolean> entry : macroAcceptedCommandIds.entrySet()) {
			acceptedCommands.setCommandAccepted(entry.getKey(), true, entry.getValue());
		}
	}

	@Test
	public void testMacroIntegrationSaveRestore() throws Exception {
		FilenameFilter macrosFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".js");
			}
		};
		assertEquals(0, fMacrosDirectory.list(macrosFilter).length);

		EMacroContextService macroContext = workbenchContext.get(EMacroContextService.class);
		EAcceptedCommands acceptedCommands = workbenchContext.get(EAcceptedCommands.class);

		assertFalse(handler.q2);

		Shell shell = new Shell(display, SWT.NONE);

		// Accept/record command
		HashMap<String, Boolean> macroAcceptedCommandIds = new HashMap<>();
		macroAcceptedCommandIds.put(TEST_ID1, true);
		setAcceptedMacroCommandIds(acceptedCommands, macroAcceptedCommandIds);

		macroContext.toggleMacroRecord();
		notifyCtrlA(shell);
		assertTrue(handler.q2);
		macroContext.toggleMacroRecord();

		// Macro was saved in the dir.
		assertEquals(1, fMacrosDirectory.list(macrosFilter).length);

		// Check if reloading from disk and playing it back works.
		((MacroContextServiceImplementation) macroContext).getMacroManager().reloadMacros();
		handler.q2 = false;
		macroContext.playbackLastMacro();
		assertTrue(handler.q2);
	}

}
