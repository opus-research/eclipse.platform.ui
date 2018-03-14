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
import javax.inject.Inject;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.e4.migration.InfoReader.PageReader;

/**
 * @since 3.5
 *
 */
public class PerspectiveReader extends MementoReader {

	private static final String TAG_ID = IWorkbenchConstants.TAG_ID;

	@Inject
	private IMementoReaderFactory readerFactory;

	private DescriptorReader descriptor;

	public String getId() {
		DescriptorReader descr = getDescriptor();
		return descr.getId();
	}

	public String getLabel() {
		DescriptorReader descr = getDescriptor();
		return descr.getLabel();
	}

	private DescriptorReader getDescriptor() {
		if (descriptor == null) {
			IMemento desriptorMem = getChild(IWorkbenchConstants.TAG_DESCRIPTOR);
			if (desriptorMem == null) {
				throw new NullPointerException("Perspective descriptor not found"); //$NON-NLS-1$
			}
			descriptor = new DescriptorReader(desriptorMem);
		}
		return descriptor;
	}

	public List<InfoReader> getInfos() {
		IMemento[] infoMems = getInfoMems();
		List<InfoReader> infos = new ArrayList<InfoReader>(infoMems.length);
		InfoReader info = null;
		for (IMemento infoMem : infoMems) {
			info = readerFactory.createInfoReader(infoMem);
			infos.add(info);
		}
		return infos;
	}

	private IMemento[] getInfoMems() {
		IMemento[] infoMems = null;
		IMemento layout = getLayout();
		if (layout != null) {
			IMemento mainWindow = layout.getChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
			if (mainWindow != null) {
				infoMems = mainWindow.getChildren(IWorkbenchConstants.TAG_INFO);
			}
		}
		if (infoMems == null) {
			infoMems = new IMemento[0];
		}
		return infoMems;
	}

	private IMemento getLayout() {
		return getChild(IWorkbenchConstants.TAG_LAYOUT);
	}

	public List<String> getPerspectiveShortcutIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_PERSPECTIVE_ACTION);
	}

	public List<String> getActionSetIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_ALWAYS_ON_ACTION_SET);
	}

	public List<String> getShowViewActionIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_SHOW_VIEW_ACTION);
	}

	public List<String> getNewWizardActionIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_NEW_WIZARD_ACTION);
	}

	public List<String> getRenderedViewIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_VIEW);
	}

	public List<String> getHiddenMenuItemIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_HIDE_MENU);
	}

	public List<String> getHiddenToolbarItemIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_HIDE_TOOLBAR);
	}

	private List<String> getChildrenIds(String tag) {
		IMemento[] idMemArr = getChildren(tag);
		List<String> idList = new ArrayList<String>(idMemArr.length);
		for (IMemento idMem : idMemArr) {
			idList.add(idMem.getString(TAG_ID));
		}
		return idList;
	}

	public List<DetachedWindowReader> getDetachedWindows() {
		List<DetachedWindowReader> readers = new ArrayList<DetachedWindowReader>();
		IMemento layout = getLayout();
		if (layout != null) {
			IMemento[] mems = layout.getChildren(IWorkbenchConstants.TAG_DETACHED_WINDOW);
			for (IMemento mem : mems) {
				readers.add(new DetachedWindowReader(mem));
			}
		}
		return readers;
	}

	public boolean isCustom() {
		DescriptorReader descr = getDescriptor();
		return descr.isCustom();
	}

	public String getBasicPerspectiveId() {
		DescriptorReader descr = getDescriptor();
		return descr.getBasicPerspectiveId();
	}

	public String getOriginalId() {
		DescriptorReader descr = getDescriptor();
		return descr.getOriginalId();
	}

	public static class DetachedWindowReader extends MementoReader {

		public DetachedWindowReader(IMemento memento) {
			this.memento = memento;
		}

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

		public String getActivePageId() {
			String activePageId = null;
			IMemento folder = getFolder();
			if (folder != null) {
				activePageId = folder.getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);
			}
			return activePageId;
		}

		public List<PageReader> getPages() {
			IMemento folder = getFolder();
			List<PageReader> pages = new ArrayList<PageReader>();
			if (folder != null) {
				IMemento[] pageMems = folder.getChildren(IWorkbenchConstants.TAG_PAGE);
				for (IMemento pageMem : pageMems) {
					pages.add(new PageReader(pageMem));
				}
			}
			return pages;
		}

		private IMemento getFolder() {
			return getChild(IWorkbenchConstants.TAG_FOLDER);
		}

	}

	private static class DescriptorReader extends MementoReader {

		private static final String TAG_DESCRIPTOR = IWorkbenchConstants.TAG_DESCRIPTOR;

		public DescriptorReader(IMemento memento) {
			this.memento = memento;
		}

		public String getId() {
			String id = getOriginalId();
			if (isCustom()) {
				String originPerspId = getBasicPerspectiveId();
				id = originPerspId + "." + id; //$NON-NLS-1$
			}
			return id;
		}

		public String getOriginalId() {
			String id = getString(TAG_ID);
			if (id == null) {
				throw new NullPointerException("Perspective ID not found"); //$NON-NLS-1$
			}
			return id;
		}

		public boolean isCustom() {
			return contains(TAG_DESCRIPTOR);
		}

		public String getBasicPerspectiveId() {
			String id = getString(TAG_DESCRIPTOR);
			if (id == null) {
				throw new NullPointerException("Basic perspective ID not found"); //$NON-NLS-1$
			}
			return id;
		}

		public String getLabel() {
			return getString(IWorkbenchConstants.TAG_LABEL);
		}

	}

}
