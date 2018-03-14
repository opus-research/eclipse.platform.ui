/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.IWorkbenchConstants;

public class InfoReader extends MementoReader {

	private static final int STATE_MINIMIZED = 0;
	private static final int STATE_MAXIMIZED = 1;
	private static final int STATE_RESTORED = 2;

	private List<PageReader> pages;

	private IMemento memFolder;

	InfoReader(IMemento memento) {
		super(memento);
	}

	String getId() {
		return getString(IWorkbenchConstants.TAG_PART);
	}

	boolean isRelativelyPositioned() {
		return contains(IWorkbenchConstants.TAG_RATIO);
	}

	boolean isFolder() {
		return getBoolean(IWorkbenchConstants.TAG_FOLDER);
	}

	boolean isEditorArea() {
		String id = getId();
		return IPageLayout.ID_EDITOR_AREA.equals(id);
	}

	private IMemento getFolder() {
		if (memFolder == null) {
			memFolder = memento.getChild(IWorkbenchConstants.TAG_FOLDER);
		}
		return memFolder;
	}

	int[] getPartOrder() {
		IMemento folder = getFolder();
		if (folder == null) {
			return null;
		}

		IMemento presentation = folder.getChild(IWorkbenchConstants.TAG_PRESENTATION);
		if (presentation == null) {
			return null;
		}

		IMemento[] partPositions = presentation.getChildren(IWorkbenchConstants.TAG_PART);
		int[] partOrder = new int[partPositions.length];
		for (int i = 0; i < partPositions.length; i++) {
			partOrder[i] = partPositions[i].getInteger(IWorkbenchConstants.TAG_ID);
		}
		return partOrder;
	}
	
	List<PageReader> getPages() {
		if (pages != null) {
			return pages;
		}

		IMemento folder = getFolder();
		if (folder != null) {
			IMemento[] pageMems = folder.getChildren(IWorkbenchConstants.TAG_PAGE);
			pages = new ArrayList<PageReader>(pageMems.length);
			for (IMemento pageMem : pageMems) {
				pages.add(new PageReader(pageMem));
			}
		}
		return pages;
	}

	String getActivePageId() {
		String activePageId = null;
		IMemento folder = getFolder();
		if (folder != null) {
			activePageId = folder.getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);
		}
		return activePageId;
	}

	float getRatio() {
		return getFloat(IWorkbenchConstants.TAG_RATIO);
	}

	int getRelationship() {
		return getInteger(IWorkbenchConstants.TAG_RELATIONSHIP);
	}

	String getRelative() {
		return getString(IWorkbenchConstants.TAG_RELATIVE);
	}

	boolean isMinimized() {
		return getState() == STATE_MINIMIZED;
	}

	boolean isMaximized() {
		return getState() == STATE_MAXIMIZED;
	}

	private int getState() {
		IMemento folder = getFolder();
		if (folder == null) {
			return STATE_RESTORED;
		}
		Integer state = folder.getInteger(IWorkbenchConstants.TAG_EXPANDED);
		return state == null ? STATE_RESTORED : state.intValue();
	}

	static class PageReader extends MementoReader {

		PageReader(IMemento pageMemento) {
			super(pageMemento);
		}

		String getId() {
			return getString(IWorkbenchConstants.TAG_CONTENT);
		}

	}

}
