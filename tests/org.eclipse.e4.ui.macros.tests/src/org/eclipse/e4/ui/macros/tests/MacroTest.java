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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.macros.EMacroContextService;
import org.eclipse.e4.core.macros.IMacroCommand;
import org.eclipse.e4.core.macros.IMacroCommandFactory;
import org.eclipse.e4.core.macros.IMacroContextListener;
import org.eclipse.e4.core.macros.IMacroPlaybackContext;
import org.eclipse.e4.core.macros.internal.MacroContextServiceImplementation;
import org.eclipse.e4.core.macros.internal.MacroManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MacroTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static class PlaybackContext implements IMacroPlaybackContext {

		public StringBuffer buffer = new StringBuffer();

		public void recordPlayback(String name) {
			if (buffer.length() > 0) {
				buffer.append("\n");
			}
			buffer.append(name);
		}
	}

	private static class DummyMacroCommand implements IMacroCommand {

		private String fName;

		public DummyMacroCommand(String name) {
			this.fName = name;
		}

		@Override
		public void execute(IMacroPlaybackContext macroPlaybackContext) {
			((PlaybackContext) macroPlaybackContext).recordPlayback(this.fName);
		}

		@Override
		public String getId() {
			return "dummy";
		}

		@Override
		public String toString() {
			return "Dummy command";
		}

		@Override
		public Map<String, String> toMap() {
			HashMap<String, String> map = new HashMap<>();
			map.put("dummyKey", "dummyValue");
			map.put("name", this.fName);
			return map;
		}

	}

	@Test
	public void testRecordingState() throws Exception {
		MacroContextServiceImplementation macroContext = new MacroContextServiceImplementation(null);
		File root = folder.getRoot();
		macroContext.getMacroManager().setMacrosDirectories(root);
		final StringBuilder buf = new StringBuilder();
		macroContext.getMacroManager().addMacroListener(new IMacroContextListener() {

			@Override
			public void macroStateChanged(EMacroContextService macroContext) {
				buf.append("rec: ").append(macroContext.isRecording()).append(" play: ")
						.append(macroContext.isPlayingBack()).append("\n");
			}
		});

		Assert.assertFalse(macroContext.isRecording());
		Assert.assertEquals("", buf.toString());

		macroContext.toggleMacroRecord();
		Assert.assertEquals("rec: true play: false\n", buf.toString());
		buf.setLength(0);
		Assert.assertTrue(macroContext.isRecording());

		macroContext.toggleMacroRecord();
		Assert.assertEquals("rec: false play: false\n", buf.toString());
		buf.setLength(0);
		Assert.assertFalse(macroContext.isRecording());

		macroContext.playbackLastMacro();
		Assert.assertEquals("rec: false play: true\n"
				+ "rec: false play: false\n", buf.toString());
		buf.setLength(0);

		macroContext.toggleMacroRecord();
		macroContext.playbackLastMacro();
		macroContext.toggleMacroRecord();
		Assert.assertEquals(
				"rec: true play: false\n"
				+ "rec: true play: true\n"
				+ "rec: true play: false\n"
				+ "rec: false play: false\n",
				buf.toString());
	}

	@Test
	public void testAddMacroCommands() throws Exception {
		PlaybackContext playbackContext = new PlaybackContext();
		MacroManager macroManager = new MacroManager();
		Map<String, IMacroCommandFactory> macroCommandIdToFactory = makeMacroCommandIdToFactory();
		macroManager.toggleMacroRecord(null, macroCommandIdToFactory);
		Assert.assertTrue(macroManager.isRecording());
		macroManager.addMacroCommand(new DummyMacroCommand("macro1"));
		macroManager.addMacroCommand(new DummyMacroCommand("macro2"));
		macroManager.toggleMacroRecord(null, macroCommandIdToFactory);
		Assert.assertFalse(macroManager.isRecording());

		macroManager.playbackLastMacro(null, playbackContext, macroCommandIdToFactory);
		Assert.assertEquals("macro1\nmacro2", playbackContext.buffer.toString());
	}

	@Test
	public void testMacroManagerSaveRestore() throws Exception {
		File root = folder.getRoot();
		MacroManager macroManager = new MacroManager(root);
		Map<String, IMacroCommandFactory> macroCommandIdToFactory = makeMacroCommandIdToFactory();
		createMacroWithOneDummyCommand(macroManager, macroCommandIdToFactory);
		String[] macroNames = listTemporaryMacros(root);
		Assert.assertEquals(1, macroNames.length);

		// Create a new macroManager (to force getting from the disk).
		macroManager = new MacroManager(root);
		PlaybackContext playbackContext = new PlaybackContext();
		macroManager.playbackLastMacro(null, playbackContext, macroCommandIdToFactory);
		Assert.assertEquals("macro1", playbackContext.buffer.toString());
	}

	@Test
	public void testMacroManagerMaxNumberOfMacros() throws Exception {
		File root = folder.getRoot();
		MacroManager macroManager = new MacroManager(root);
		macroManager.setmaxNumberOfTemporaryMacros(2);
		Map<String, IMacroCommandFactory> macroCommandIdToFactory = makeMacroCommandIdToFactory();

		createMacroWithOneDummyCommand(macroManager, macroCommandIdToFactory);

		String[] macroNames1 = listTemporaryMacros(root);
		Assert.assertEquals(macroNames1.length, 1);

		// Sleep to make sure that the time of the file will be different
		// (otherwise, if it runs too fast, the same time could be applied to
		// the first, second and last file, in which case we may remove the
		// wrong one).
		sleepABit();
		createMacroWithOneDummyCommand(macroManager, macroCommandIdToFactory);

		String[] macroNames2 = listTemporaryMacros(root);
		Assert.assertEquals(2, macroNames2.length);

		// Now, creating a new one removes the old one
		sleepABit();
		createMacroWithOneDummyCommand(macroManager, macroCommandIdToFactory);

		String[] macroNames3 = listTemporaryMacros(root);
		Assert.assertEquals(2, macroNames3.length);

		Assert.assertTrue(!Arrays.asList(macroNames3).contains(macroNames1[0]));
	}

	private void sleepABit() {
		synchronized (this) {
			try {
				this.wait(2); // 2 millis should be enough for a new timestamp
								// on files.
			} catch (InterruptedException e) {
				// Ignore in this case
			}
		}

	}

	protected String[] listTemporaryMacros(File root) {
		String[] files = root.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".js") && name.startsWith("temp_macro");
			}
		});
		return files;
	}

	protected void createMacroWithOneDummyCommand(MacroManager macroManager,
			Map<String, IMacroCommandFactory> macroCommandIdToFactory) {
		macroManager.toggleMacroRecord(null, macroCommandIdToFactory);
		macroManager.addMacroCommand(new DummyMacroCommand("macro1"));
		macroManager.toggleMacroRecord(null, macroCommandIdToFactory);
	}

	private Map<String, IMacroCommandFactory> makeMacroCommandIdToFactory() {
		Map<String, IMacroCommandFactory> macroCommandIdToFactory = new HashMap<>();
		macroCommandIdToFactory.put("dummy", new IMacroCommandFactory() {

			@Override
			public IMacroCommand create(Map<String, String> stringMap) {
				if (stringMap.size() != 2) {
					throw new AssertionError("Expected map size to be 2. Found: " + stringMap.size());
				}
				if (!stringMap.get("dummyKey").equals("dummyValue")) {
					throw new AssertionError("Did not find dummyKey->dummyValue mapping.");
				}
				if (stringMap.get("name") == null) {
					throw new AssertionError("Expected name to be defined.");
				}

				return new DummyMacroCommand(stringMap.get("name"));
			}
		});
		return macroCommandIdToFactory;
	}

}
