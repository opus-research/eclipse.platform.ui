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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * @since 3.5
 *
 */
public class InfoReader extends MementoReader {

	private static final String TAG_RATIO = IWorkbenchConstants.TAG_RATIO;

	private static final String TAG_FOLDER = IWorkbenchConstants.TAG_FOLDER;

	private List<PageReader> pages;

	private IMemento memFolder;

	public String getId() {
		return getString(IWorkbenchConstants.TAG_PART);
	}

	public boolean isRelativelyPositioned() {
		return contains(TAG_RATIO);
	}

	public boolean isFolder() {
		return getBoolean(TAG_FOLDER);
	}

	public boolean isEditorArea() {
		String id = getId();
		return IPageLayout.ID_EDITOR_AREA.equals(id);
	}

	private IMemento getFolder() {
		if (memFolder == null) {
			memFolder = memento.getChild(TAG_FOLDER);
		}
		return memFolder;
	}

	public List<PageReader> getPages() {
		if (pages == null) {
			IMemento folder = getFolder();
			if (folder != null) {
				IMemento[] pageMems = folder.getChildren(IWorkbenchConstants.TAG_PAGE);
				pages = new ArrayList<PageReader>(pageMems.length);
				for (IMemento pageMem : pageMems) {
					pages.add(new PageReader(pageMem));
				}
			}
		}
		return pages;
	}

	public String getActivePageId() {
		String activePageId = null;
		IMemento folder = getFolder();
		if (folder != null) {
			activePageId = folder.getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);
		}
		return activePageId;
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return true;
	}

	public float getRatio() {
		return getFloat(TAG_RATIO);
	}

	public int getRelationship() {
		return getInteger(IWorkbenchConstants.TAG_RELATIONSHIP);
	}

	public String getRelative() {
		return getString(IWorkbenchConstants.TAG_RELATIVE);
	}

	public boolean isMinimized() {
		int minimized = 1;
		IMemento folder = getFolder();
		if (folder != null) {
			minimized = folder.getInteger(IWorkbenchConstants.TAG_EXPANDED);
		}
		return minimized == 0;
	}

	public static class PageReader extends MementoReader {

		public PageReader(IMemento pageMemento) {
			this.memento = pageMemento;
		}

		public String getId() {
			return getString(IWorkbenchConstants.TAG_CONTENT);
		}

		public String getLabel() {
			String label = getString(IWorkbenchConstants.TAG_LABEL);
			return "LabelNotFound".equals(label) ? null : label; //$NON-NLS-1$
		}

	}

}
