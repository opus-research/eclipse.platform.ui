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

package org.eclipse.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.e4.migration.ApplicationBuilder;
import org.eclipse.ui.internal.e4.migration.IMementoReaderFactory;
import org.eclipse.ui.internal.e4.migration.IModelBuilderFactory;
import org.eclipse.ui.internal.e4.migration.MementoReaderFactoryImpl;
import org.eclipse.ui.internal.e4.migration.ModelBuilderFactoryImpl;
import org.eclipse.ui.internal.e4.migration.PerspectiveBuilder;
import org.eclipse.ui.internal.e4.migration.WorkbenchMementoReader;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.StickyViewDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class WorkbenchMigrationProcessor {

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	@Inject
	private EModelService modelService;

	private IMemento workbenchMemento;

	private File legacyWorkbenchFile;

	private boolean migrated;

	private List<MWindow> defaultWindows;

	public void process() {
		legacyWorkbenchFile = getLegacyWorkbenchFile();
		if (legacyWorkbenchFile == null || !legacyWorkbenchFile.exists()) {
			return;
		}

		workbenchMemento = loadMemento();
		if (workbenchMemento == null) {
			return;
		}
		defaultWindows = new ArrayList<MWindow>(application.getChildren());
		application.getChildren().clear();
		IEclipseContext builderContext = context.createChild();

		IMementoReaderFactory readerFactory = ContextInjectionFactory.make(
				MementoReaderFactoryImpl.class, builderContext);
		builderContext.set(IMementoReaderFactory.class, readerFactory);

		IModelBuilderFactory builderFactory = ContextInjectionFactory.make(
				ModelBuilderFactoryImpl.class, builderContext);
		builderContext.set(IModelBuilderFactory.class, builderFactory);

		WorkbenchMementoReader memReader = readerFactory.createWorkbenchReader(workbenchMemento);
		ApplicationBuilder modelBuilder = builderFactory.createApplicationBuilder(memReader);
		modelBuilder.createApplication();
		context.remove(E4Workbench.NO_SAVED_MODEL_FOUND);
		PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, false);
		migrated = true;
	}

	private IMemento loadMemento() {
		FileInputStream input;
		BufferedReader reader = null;
		IMemento memento = null;
		try {
			input = new FileInputStream(legacyWorkbenchFile);
			reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
			memento = XMLMemento.createReadRoot(reader);
		} catch (IOException e) {
			WorkbenchPlugin.log("Failed to load " + legacyWorkbenchFile.getAbsolutePath(), e); //$NON-NLS-1$
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Failed to load " + legacyWorkbenchFile.getAbsolutePath(), e); //$NON-NLS-1$
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					WorkbenchPlugin.log(e);
				}
			}
		}
		return memento;
	}

	private File getLegacyWorkbenchFile() {
		if (legacyWorkbenchFile == null) {
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null) {
			return null;
		}
		path = path.append(Workbench.DEFAULT_WORKBENCH_STATE_FILENAME);
			legacyWorkbenchFile = path.toFile();
		}
		return legacyWorkbenchFile;
	}

	public boolean isWorkbenchMigrated() {
		return migrated;
	}

	public void updatePartsAfterMigration(IPerspectiveRegistry perspectiveRegistry,
			IViewRegistry viewRegistry) {
		if (!migrated) {
			return;
		}

		for (MPartDescriptor desc : application.getDescriptors()) {
			List<MPart> parts = modelService.findElements(application, desc.getElementId(),
					MPart.class,
					null);
			for (MPart part : parts) {
				if (part.getLabel() == null) {
					part.setLabel(desc.getLocalizedLabel());
				}
				if (part.getTooltip() == null) {
					part.setTooltip(desc.getLocalizedTooltip());
				}
				if (part.getIconURI() == null) {
					part.setIconURI(desc.getIconURI());
				}
			}

		}
		List<MPerspective> persps = modelService.findElements(application, null,
				MPerspective.class, null);
		for (MPerspective persp : persps) {
			if (persp.getTransientData().containsKey(PerspectiveBuilder.ORIGINAL_ID)) {
				String originPerspId = (String) persp.getTransientData().get(
						PerspectiveBuilder.ORIGINAL_ID);
				IPerspectiveDescriptor opd = perspectiveRegistry.findPerspectiveWithId(originPerspId);
				if (opd != null) {
					persp.setIconURI(MenuHelper.getIconURI(opd.getImageDescriptor(), context));
				}
			}
		}

		for (MUIElement element : application.getSnippets()) {
			if (element instanceof MPerspective) {
				MPerspective persp = (MPerspective) element;
				if (persp.getTransientData().containsKey("origin")) { //$NON-NLS-1$
					String originPerspId = (String) persp.getTransientData().get("origin"); //$NON-NLS-1$
					IPerspectiveDescriptor opd = perspectiveRegistry
							.findPerspectiveWithId(originPerspId);
					persp.setIconURI(MenuHelper.getIconURI(opd.getImageDescriptor(), context));
				}
			}
		}

		IStickyViewDescriptor[] stickyViews = viewRegistry.getStickyViews();
		for (IStickyViewDescriptor stickyView : stickyViews) {
			String stickyViewId = stickyView.getId();
			List<MPlaceholder> placeholders = modelService.findElements(application, stickyViewId,
					MPlaceholder.class, null);
			for (MPlaceholder placeholder : placeholders) {
				MElementContainer<MUIElement> parent = placeholder.getParent();
				if (StickyViewDescriptor.STICKY_FOLDER_RIGHT.equals(parent.getElementId())) {
					continue;
				}
				placeholder.setToBeRendered(false);
				placeholder.setVisible(false);
				parent.getChildren().remove(placeholder);
				// remove empty container
				if (parent.getChildren().isEmpty()) {
					parent.getParent().getChildren().remove(parent);
				} else if (parent.getSelectedElement() == placeholder) {
					parent.setSelectedElement(null);
				}
			}
		}

		List<MPartStack> stickyFolders = modelService.findElements(application,
				StickyViewDescriptor.STICKY_FOLDER_RIGHT, MPartStack.class, null);
		for (MPartStack stickyFolder : stickyFolders) {
			for (IStickyViewDescriptor stickyView : stickyViews) {
				addPartToStickyFolder(stickyFolder, stickyView.getId());
			}
		}
	}

	private MPlaceholder addPartToStickyFolder(MPartStack stickyFolder, String partId) {
		MPart part = null;
		MPlaceholder placeholder = null;
		MWindow window = modelService.getTopLevelWindowFor(stickyFolder);
		for (MUIElement element : window.getSharedElements()) {
			if (element.getElementId().equals(partId)) {
				part = (MPart) element;
				break;
			}
		}
		if (part == null) {
			part = modelService.createModelElement(MPart.class);
			part.setElementId(partId);
			part.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
			part.getTags().add("View"); //$NON-NLS-1$
			window.getSharedElements().add(part);
		}
		placeholder = modelService.createModelElement(MPlaceholder.class);
		placeholder.setElementId(partId);
		placeholder.setRef(part);
		placeholder.setToBeRendered(false);
		part.setCurSharedRef(placeholder);
		stickyFolder.getChildren().add(placeholder);
		return placeholder;
	}

	public void restoreDefaultModel() {
		application.getTags().clear();
		application.getPersistedState().clear();
		application.getSnippets().clear();
		application.getDescriptors().clear();
		application.getChildren().clear();
		application.getChildren().addAll(defaultWindows);
	}

}
