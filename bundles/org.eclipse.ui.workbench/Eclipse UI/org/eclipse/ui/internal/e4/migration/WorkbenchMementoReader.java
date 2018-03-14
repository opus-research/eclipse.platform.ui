/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.5
 *
 */
public class WorkbenchMementoReader extends MementoReader {

	@Inject
	private IMementoReaderFactory readerFactory;

	public List<WindowReader> getWindowReaders() {
		IMemento[] windowMems = getChildren(IWorkbenchConstants.TAG_WINDOW);
		List<WindowReader> windows = new ArrayList<WindowReader>(windowMems.length);
		for (IMemento windowMem : windowMems) {
			windows.add(readerFactory.createWindowReader(windowMem));
		}
		return windows;
	}

	public String getMRU() {
		XMLMemento root = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		IMemento mruList = memento.getChild(IWorkbenchConstants.TAG_MRU_LIST);
		if (mruList != null) {
			root.copyChild(mruList);
		}
		StringWriter writer = new StringWriter();
		try {
			root.save(writer);
		} catch (IOException e) {
			WorkbenchPlugin.log(e);
		}
		return writer.toString();
	}

}
