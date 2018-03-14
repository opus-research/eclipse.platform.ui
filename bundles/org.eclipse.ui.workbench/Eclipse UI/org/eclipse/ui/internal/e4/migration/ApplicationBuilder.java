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

import java.io.StringReader;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.5
 *
 */
@SuppressWarnings("restriction")
public class ApplicationBuilder {

	@Inject
	private MApplication application;

	@Inject
	private WorkbenchMementoReader reader;

	@Inject
	private IModelBuilderFactory builderFactory;

	@Inject
	private IMementoReaderFactory readerFactory;

	@Inject
	@Preference(nodePath = "org.eclipse.ui.workbench")
	private IEclipsePreferences preferences;

	public void createApplication() {
		List<MWindow> windows = application.getChildren();
		List<WindowReader> windowReaders = reader.getWindowReaders();
		for (WindowReader windowReader : windowReaders) {
			WindowBuilder windowBuilder = builderFactory.createWindowBuilder(windowReader);
			MWindow window = windowBuilder.createWindow();
			windows.add(window);
			if (windowBuilder.isSelected()) {
				application.setSelectedElement(window);
			}
		}
		if (!windows.isEmpty()) {
			windows.get(0).setElementId("IDEWindow"); //$NON-NLS-1$
		}
		addClosedPerspectives();
		addMRU();
	}

	private void addClosedPerspectives() {
		String perspProp = preferences.get(IWorkbenchConstants.TAG_PERSPECTIVES, null);
		if (perspProp == null) {
			return;
		}
		List<MUIElement> snippets = application.getSnippets();
		String[] perspNames = perspProp.split(" "); //$NON-NLS-1$
		for (String perspName : perspNames) {
			String key = perspName + "_persp"; //$NON-NLS-1$
			String xml = preferences.get(key, ""); //$NON-NLS-1$
			StringReader stringReader = new StringReader(xml);
			IMemento memento = null;
			try {
				memento = XMLMemento.createReadRoot(stringReader);
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log("Loading custom perspective failed: " + perspName, e); //$NON-NLS-1$
			}
			PerspectiveReader perspReader = readerFactory.createPerspectiveReader(memento);
			PerspectiveBuilder perspBuilder = builderFactory.createPerspectiveBuilder(perspReader);
			snippets.add(perspBuilder.createPerspective());
		}
	}

	private void addMRU() {
		MementoSerializer serializer = new MementoSerializer(reader.getMruMemento());
		application.getPersistedState().put("memento", serializer.serialize()); //$NON-NLS-1$
	}

}
