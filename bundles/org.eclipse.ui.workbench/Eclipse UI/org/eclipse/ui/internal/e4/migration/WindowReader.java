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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.layout.ITrimManager;


/**
 * @since 3.5
 *
 */
public class WindowReader extends MementoReader {

	@Inject
	private IMementoReaderFactory readerFactory;

	private IMemento pageMemento;

	public Rectangle getBounds() {
		Rectangle windowBounds = new Rectangle(0, 0, 0, 0);
		Integer bigInt = getInteger(IWorkbenchConstants.TAG_X);
		windowBounds.x = bigInt == null ? 0 : bigInt.intValue();
		bigInt = getInteger(IWorkbenchConstants.TAG_Y);
		windowBounds.y = bigInt == null ? 0 : bigInt.intValue();
		bigInt = getInteger(IWorkbenchConstants.TAG_WIDTH);
		windowBounds.width = bigInt == null ? 0 : bigInt.intValue();
		bigInt = getInteger(IWorkbenchConstants.TAG_HEIGHT);
		windowBounds.height = bigInt == null ? 0 : bigInt.intValue();
		return windowBounds;
	}

	public boolean isCoolbarVisible() {
		IMemento trimLayoutMem = getChild(IWorkbenchConstants.TAG_TRIM);
		if (trimLayoutMem == null) {
			return false;
		}

		boolean visible = false;
		IMemento[] trimAreas = trimLayoutMem.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		IMemento topTrim = null;
		for (IMemento trimArea : trimAreas) {
			if (ITrimManager.TOP == trimArea.getInteger(IMemento.TAG_ID)) {
				topTrim = trimArea;
				break;
			}
		}
		if (topTrim != null) {
			IMemento[] trimItems = topTrim.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
			for (IMemento trimItem : trimItems) {
				if ("org.eclipse.ui.internal.WorkbenchWindow.topBar".equals(trimItem //$NON-NLS-1$
						.getString(IMemento.TAG_ID))) {
					visible = true;
					break;
				}
			}
		}
		return visible;
	}

	public List<PerspectiveReader> getPerspectives() {
		List<PerspectiveReader> perspectives = new ArrayList<PerspectiveReader>();
		IMemento perspContainer = getPerspectiveContainer();
		if (perspContainer != null) {
			IMemento[] perspectiveMems = perspContainer
					.getChildren(IWorkbenchConstants.TAG_PERSPECTIVE);
			for (IMemento perspectiveMem : perspectiveMems) {
				PerspectiveReader perspective = readerFactory
						.createPerspectiveReader(perspectiveMem);
				perspectives.add(perspective);
			}
		}
		return perspectives;
	}

	private IMemento getPerspectiveContainer() {
		IMemento page = getPage();
		IMemento perspContainer = page.getChild(IWorkbenchConstants.TAG_PERSPECTIVES);
		return perspContainer;
	}

	public boolean isSelected() {
		IMemento page = getPage();
		Boolean selected = page.getBoolean(IWorkbenchConstants.TAG_FOCUS);
		return selected == null ? false : selected.booleanValue();
	}

	public String getLabel() {
		IMemento page = getPage();
		return page.getString(IWorkbenchConstants.TAG_LABEL);
	}

	public String getActivePerspectiveId() {
		String activePerspectiveId = null;
		IMemento perspContainer = getPerspectiveContainer();
		if (perspContainer != null) {
			activePerspectiveId = perspContainer.getString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE);
		}
		return activePerspectiveId;
	}

	public String getActivePartId() {
		String activePartId = null;
		IMemento perspContainer = getPerspectiveContainer();
		if (perspContainer != null) {
			activePartId = perspContainer.getString(IWorkbenchConstants.TAG_ACTIVE_PART);
		}
		return activePartId;
	}

	public List<EditorReader> getEditors() {
		IMemento page = getPage();
		IMemento editors = page.getChild(IWorkbenchConstants.TAG_EDITORS);
		List<EditorReader> readers = new ArrayList<EditorReader>();
		if (editors != null) {
			IMemento[] editorMems = editors.getChildren(IWorkbenchConstants.TAG_EDITOR);
			for (IMemento memento : editorMems) {
				readers.add(new EditorReader(memento));
			}
		}
		return readers;
	}

	public List<ViewReader> getViews() {
		IMemento page = getPage();
		IMemento editors = page.getChild(IWorkbenchConstants.TAG_VIEWS);
		List<ViewReader> readers = new ArrayList<ViewReader>();
		if (editors != null) {
			IMemento[] editorMems = editors.getChildren(IWorkbenchConstants.TAG_VIEW);
			for (IMemento memento : editorMems) {
				readers.add(new ViewReader(memento));
			}
		}
		return readers;
	}

	private IMemento getPage() {
		if (pageMemento == null) {
			pageMemento = getChild(IWorkbenchConstants.TAG_PAGE);
		}
		if (pageMemento == null) {
			throw new NullPointerException("Workbench page not found"); //$NON-NLS-1$
		}
		return pageMemento;
	}

	public static class EditorReader extends MementoReader {

		public EditorReader(IMemento memento) {
			this.memento = memento;
		}

		public String getLabel() {
			return getString(IWorkbenchConstants.TAG_TITLE);
		}

		public String getType() {
			return getString(IWorkbenchConstants.TAG_ID);
		}

	}

	public static class ViewReader extends MementoReader {

		public ViewReader(IMemento memento) {
			this.memento = memento;
		}

		public String getId() {
			return getString(IWorkbenchConstants.TAG_ID);
		}

		public String getLabel() {
			return getString(IWorkbenchConstants.TAG_PART_NAME);
		}

		public String getViewState() {
			IMemento viewStateMem = getChild(IWorkbenchConstants.TAG_VIEW_STATE);
			if (viewStateMem == null) {
				return null;
			}
			XMLMemento renamed = createRenamedCopy(viewStateMem, IWorkbenchConstants.TAG_VIEW);
			StringWriter writer = new StringWriter();
			try {
				renamed.save(writer);
			} catch (IOException e) {
				WorkbenchPlugin.log(e);
			}
			return writer.toString();
		}

		private XMLMemento createRenamedCopy(IMemento memento, String newName) {
			XMLMemento newMem = XMLMemento.createWriteRoot(newName);
			newMem.putMemento(memento);
			return newMem;
		}

	}

}
