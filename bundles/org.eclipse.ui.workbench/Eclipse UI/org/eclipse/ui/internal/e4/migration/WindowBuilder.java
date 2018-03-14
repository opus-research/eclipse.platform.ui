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

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.e4.migration.WindowReader.EditorReader;
import org.eclipse.ui.internal.e4.migration.WindowReader.ViewReader;
import org.eclipse.ui.internal.registry.StickyViewDescriptor;

/**
 * @since 3.5
 *
 */
public class WindowBuilder {

	public static final String ID_EDITOR_AREA = IPageLayout.ID_EDITOR_AREA;

	private static final String PRIMARY_DATA_STACK = "org.eclipse.e4.primaryDataStack"; //$NON-NLS-1$

	private MWindow window;

	private List<MUIElement> sharedElements;

	private MPartSashContainer mainSash;

	private MPartStack editorStack;

	@Inject
	private WindowReader windowReader;

	@Inject
	private EModelService modelService;

	@Inject
	private IModelBuilderFactory factory;

	private MArea editorArea;

	public MWindow createWindow() {
		create();
		populate();
		return window;
	}

	public boolean isSelected() {
		return windowReader.isSelected();
	}

	private void create() {
		window = modelService.createModelElement(MTrimmedWindow.class);
		String label = windowReader.getLabel();
		window.setLabel(label);
		Rectangle bounds = windowReader.getBounds();
		window.setX(bounds.x);
		window.setY(bounds.y);
		window.setWidth(bounds.width);
		window.setHeight(bounds.height);
		window.getTags().add("topLevel"); //$NON-NLS-1$

		String coolbarVisible = Boolean.TRUE.toString();
		if (!windowReader.isCoolbarVisible()) {
			coolbarVisible = Boolean.FALSE.toString();
		}
		window.getPersistedState().put(IPreferenceConstants.COOLBAR_VISIBLE, coolbarVisible);
		window.getPersistedState().put(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE, coolbarVisible);

		sharedElements = window.getSharedElements();
	}

	private void populate() {
		addEditorArea();
		addEditors();
		addViews();

		mainSash = modelService.createModelElement(MPartSashContainer.class);
		mainSash.setHorizontal(true);

		MPerspectiveStack perspectiveStack = createPerspectiveStack();
		mainSash.getChildren().add(perspectiveStack);

		window.getChildren().add(mainSash);

		List<PerspectiveReader> perspReaders = windowReader.getPerspectiveReaders();
		for (PerspectiveReader perspReader : perspReaders) {
			PerspectiveBuilder builder = factory.createPerspectiveBuilder(perspReader);
			perspectiveStack.getChildren().add(builder.createPerspective());
			MPlaceholder eaPlaceholder = builder.getEditorAreaPlaceholder();
			if (eaPlaceholder != null) {
				eaPlaceholder.setRef(editorArea);
			}
			for (MPlaceholder viewPlaceholder : builder.getPlaceholders()) {
				String id = viewPlaceholder.getElementId();
				if (id != null) {
					MPart part = getSharedView(id);
					viewPlaceholder.setRef(part);
				}
			}
		}

		String activePerspectiveId = windowReader.getActivePerspectiveId();
		if (activePerspectiveId != null) {
			for (MPerspective persp : perspectiveStack.getChildren()) {
				String id = persp.getElementId();
				String originalId = (String) persp.getTransientData().get(
						PerspectiveBuilder.ORIGINAL_ID);
				if (originalId != null) {
					id = originalId;
				}
				if (activePerspectiveId.equals(id)) {
					perspectiveStack.setSelectedElement(persp);
					break;
				}
			}
		}
		addStickyFolder();
	}

	private void addEditors() {
		List<EditorReader> readers = windowReader.getEditors();
		for (EditorReader editorReader : readers) {
			MPart editor = modelService.createModelElement(MPart.class);
			editor.setElementId("org.eclipse.e4.ui.compatibility.editor"); //$NON-NLS-1$
			editor.setContributionURI(CompatibilityPart.COMPATIBILITY_EDITOR_URI);
			editor.setLabel(editorReader.getLabel());
			MementoSerializer serializer = new MementoSerializer(editorReader.getMemento());
			editor.getPersistedState().put("memento", serializer.serialize()); //$NON-NLS-1$
			List<String> tags = editor.getTags();
			tags.add("Editor"); //$NON-NLS-1$
			tags.add("removeOnHide"); //$NON-NLS-1$
			tags.add(editorReader.getType());
			editorStack.getChildren().add(editor);
		}
	}

	private void addViews() {
		List<ViewReader> readers = windowReader.getViews();
		for (ViewReader viewReader : readers) {
			MPart view = createView(viewReader);
			sharedElements.add(view);
		}
	}

	private MPart createView(ViewReader viewReader) {
		MPart view = modelService.createModelElement(MPart.class);
		String id = viewReader.getId();
		view.setElementId(id);
		view.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
		view.setLabel(viewReader.getLabel());
		List<String> tags = view.getTags();
		tags.add("View"); //$NON-NLS-1$
		MementoSerializer serializer = new MementoSerializer(viewReader.getViewState());
		view.getPersistedState().put("memento", serializer.serialize()); //$NON-NLS-1$
		return view;
	}

	private void addEditorArea() {
		editorArea = modelService.createModelElement(MArea.class);
		sharedElements.add(editorArea);
		editorArea.setElementId(IPageLayout.ID_EDITOR_AREA);
		editorStack = modelService.createModelElement(MPartStack.class);
		editorStack.setElementId(PRIMARY_DATA_STACK);
		editorStack.getTags().add(PRIMARY_DATA_STACK);
		editorStack.getTags().add("EditorStack"); //$NON-NLS-1$
		editorArea.getChildren().add(editorStack);
	}

	public MArea getEditorArea() {
		return editorArea;
	}

	private MPerspectiveStack createPerspectiveStack() {
		MPerspectiveStack perspStack = modelService
				.createModelElement(MPerspectiveStack.class);
		perspStack.setElementId("PerspectiveStack"); //$NON-NLS-1$
		return perspStack;
	}

	private void addStickyFolder() {
		MPartStack stickyFolder = modelService.createModelElement(MPartStack.class);
		stickyFolder.setElementId(StickyViewDescriptor.STICKY_FOLDER_RIGHT);
		stickyFolder.setContainerData("2500"); //$NON-NLS-1$
		stickyFolder.setToBeRendered(false);
		mainSash.getChildren().add(stickyFolder);
	}

	private MPart getSharedView(String id) {
		MPart part = null;
		for (MUIElement element : sharedElements) {
			if (id.equals(element.getElementId()) && element instanceof MPart) {
				part = (MPart) element;
				break;
			}
		}
		if (part == null) {
			part = modelService.createModelElement(MPart.class);
			part.setElementId(id);
			part.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
			part.getTags().add("View"); //$NON-NLS-1$
			sharedElements.add(part);
		}
		return part;
	}

}
