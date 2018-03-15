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
package org.eclipse.e4.core.macros.internal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.macros.Activator;
import org.eclipse.e4.core.macros.IMacroInstruction;
import org.eclipse.e4.core.macros.IMacroInstructionFactory;
import org.eclipse.e4.core.macros.IMacroPlaybackContext;

/**
 * This is a macro which is created from a sequence of instructions which are
 * stored in-memory (and may be persisted later on).
 */
/* default */ class ComposableMacro implements IMacro {

	/**
	 * Provides the macro instruction id to an implementation which is able to
	 * recreate it.
	 */
	private Map<String, IMacroInstructionFactory> fMacroInstructionIdToFactory;

	/**
	 * The macro instructions which compose this macro.
	 */
	private List<IMacroInstruction> fMacroInstructions = new ArrayList<>();

	private static class IndexAndPriority {

		private final int fIndex;
		private final int fPriority;

		private IndexAndPriority(int index, int priority) {
			this.fIndex = index;
			this.fPriority = priority;
		}

	}

	/**
	 * Map of an event to the current index of the macro instruction in
	 * fMacroInstructions and the priority for the given macro instruction.
	 */
	private Map<Object, IndexAndPriority> fEventToPlacement = new HashMap<>();

	/**
	 * @param macroInstructionIdToFactory
	 *            Only macros instructions which have ids available as keys in
	 *            the macroInstructionIdToFactory will be accepted.
	 */
	public ComposableMacro(Map<String, IMacroInstructionFactory> macroInstructionIdToFactory) {
		fMacroInstructionIdToFactory = macroInstructionIdToFactory;
	}

	/**
	 * Checks whether the added macro instruction is suitable to be added to
	 * this macro.
	 *
	 * @param macroInstruction
	 *            the macro instruction to be checked.
	 */
	private void checkMacroInstruction(IMacroInstruction macroInstruction) {
		if (fMacroInstructionIdToFactory != null && !fMacroInstructionIdToFactory.containsKey(macroInstruction.getId())) {
			throw new RuntimeException(
					String.format("Macro instruction: %s not properly registered through a %s extension point.", //$NON-NLS-1$
							macroInstruction.getId(), MacroServiceImplementation.MACRO_INSTRUCTION_FACTORY_EXTENSION_POINT));
		}
	}

	/**
	 * Adds a new macro instruction to this macro.
	 *
	 * @param macroInstruction
	 *            the macro instruction to be appended to this macro.
	 */
	public void addMacroInstruction(IMacroInstruction macroInstruction) {
		checkMacroInstruction(macroInstruction);
		fMacroInstructions.add(macroInstruction);
	}

	/**
	 * Adds a macro instruction to be added to the current macro being recorded.
	 * The difference between this method and
	 * {@link #addMacroInstruction(IMacroInstruction)} is that it's meant to be
	 * used when an event may trigger the creation of multiple macro
	 * instructions and only one of those should be recorded.
	 *
	 * @param macroInstruction
	 *            the macro instruction to be added to the macro currently being
	 *            recorded.
	 * @param event
	 *            the event that triggered the creation of the macro instruction
	 *            to be added. If there are multiple macro instructions added
	 *            for the same event, only the one with the highest priority
	 *            will be kept (if 2 events have the same priority, the last one
	 *            will replace the previous one).
	 * @param priority
	 *            the priority of the macro instruction being added (to be
	 *            compared against the priority of other added macro
	 *            instructions for the same event).
	 * @see #addMacroInstruction(IMacroInstruction)
	 */
	public void addMacroInstruction(IMacroInstruction macroInstruction, Object event, int priority) {
		Assert.isNotNull(event);
		IndexAndPriority currentIndexAndPriority = this.fEventToPlacement.get(event);
		if (currentIndexAndPriority == null) {
			this.addMacroInstruction(macroInstruction);
			this.fEventToPlacement.put(event, new IndexAndPriority(this.fMacroInstructions.size() - 1, priority));
		} else {
			if (priority >= currentIndexAndPriority.fPriority) {
				checkMacroInstruction(macroInstruction);
				fMacroInstructions.set(currentIndexAndPriority.fIndex, macroInstruction);
				this.fEventToPlacement.put(event, new IndexAndPriority(currentIndexAndPriority.fIndex, priority));
			}
		}
	}

	/**
	 * Clears information obtained during recording which should be no longer
	 * needed after the macro is properly composed.
	 */
	public void clearCachedInfo() {
		this.fEventToPlacement.clear();
	}

	@Override
	public void playback(IMacroPlaybackContext macroPlaybackContext) throws Exception {
		for (IMacroInstruction macroInstruction : fMacroInstructions) {
			macroInstruction.execute(macroPlaybackContext);
		}
	}


	/**
	 * Actually returns the bytes to be written to the disk to be loaded back
	 * later on (the actual load and playback is later done by
	 * {@link SavedJSMacro}.
	 *
	 * @return an UTF-8 encoded array of bytes which can be used to rerun the
	 *         macro later on.
	 */
	/* default */ byte[] toJSBytes() {
		final StringBuilder buf = new StringBuilder(this.fMacroInstructions.size() * 60);

		buf.append("// Macro generated by the Eclipse macro record engine.\n"); //$NON-NLS-1$
		buf.append("// The runMacro() function will be later run by the macro engine.\n"); //$NON-NLS-1$
		buf.append("function runMacro(){\n"); //$NON-NLS-1$

		for (IMacroInstruction macroInstruction : this.fMacroInstructions) {
			Map<String, String> map = macroInstruction.toMap();
			Assert.isNotNull(map);

			buf.append("    runMacroInstruction("); //$NON-NLS-1$
			buf.append(JSONHelper.quote(macroInstruction.getId()));
			buf.append(", "); //$NON-NLS-1$
			buf.append(JSONHelper.toJSon(map));
			buf.append(");\n"); //$NON-NLS-1$
		}
		buf.append("}\n"); //$NON-NLS-1$

		try {
			return buf.toString().getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// Make this a RuntimeException (UTF-8 should definitely be
			// supported).
			Activator.log(e);
			throw new RuntimeException(e);
		}
	}
}
