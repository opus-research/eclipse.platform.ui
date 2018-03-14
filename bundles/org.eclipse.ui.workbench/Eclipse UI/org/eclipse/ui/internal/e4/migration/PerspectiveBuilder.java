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
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.PerspectiveTagger;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.e4.migration.InfoReader.PageReader;
import org.eclipse.ui.internal.e4.migration.PerspectiveReader.DetachedWindowReader;
import org.eclipse.ui.internal.registry.StickyViewDescriptor;

/**
 * @since 3.5
 *
 */
public class PerspectiveBuilder {

	/**
	 * 
	 */
	private static final String ID_EDITOR_AREA = IPageLayout.ID_EDITOR_AREA;

	public static final String ORIGINAL_ID = "originalId"; //$NON-NLS-1$

	private static final String TAG_MINIMIZED = "minimized"; //$NON-NLS-1$

	private static final String BASE_PERSPECTIVE_ID = "basePerspectiveId"; //$NON-NLS-1$

	@Inject
	private PerspectiveReader perspReader;

	@Inject
	private EModelService modelService;

	private MPerspective perspective;

	private List<String> tags;

	private List<String> renderedViews;

	private List<MPlaceholder> viewPlaceholders = new ArrayList<MPlaceholder>();

	private MPlaceholder editorAreaPlaceholder;

	public MPerspective createPerspective() {
		create();
		tags = perspective.getTags();
		populate();
		return perspective;
	}

	private void create() {
		perspective = modelService.createModelElement(MPerspective.class);
		String id = perspReader.getId();
		perspective.setElementId(id);
		String label = perspReader.getLabel();
		perspective.setLabel(label);
		perspective.setTooltip(label);
		if (perspReader.isCustom()) {
			String originalPerspective = perspReader.getBasicPerspectiveId();
			perspective.getTransientData().put(BASE_PERSPECTIVE_ID, originalPerspective);
			String originalId = perspReader.getOriginalId();
			perspective.getTransientData().put(ORIGINAL_ID, originalId);
		}
	}

	private void populate() {
		addActionSetTags();
		addPerspectiveShortcutTags();
		addNewWizardTags();
		addShowViewTags();
		addHiddenItems();

		List<InfoReader> infos = perspReader.getInfos();
		for (InfoReader info : infos) {
			if (info.isEditorArea()) {
				addEditorArea(info);
			} else if (info.isFolder()) {
				MPartStack stack = addPartStack(info);
				populatePartStack(stack, info);
			}
		}
		addDetachedWindows();
		hideEmptyStacks();
		for (MPartSashContainerElement element : perspective.getChildren()) {
			removeUnnecessaryPartSashes(element);
		}
		PerspectiveTagger.tagPerspective(perspective, modelService);
	}

	private void addDetachedWindows() {
		List<DetachedWindowReader> detachedWindows = perspReader.getDetachedWindows();
		for (DetachedWindowReader detachedWindowReader : detachedWindows) {
			MTrimmedWindow detachedWindow = modelService.createModelElement(MTrimmedWindow.class);
			Rectangle bounds = detachedWindowReader.getBounds();
			detachedWindow.setX(bounds.x);
			detachedWindow.setY(bounds.y);
			detachedWindow.setWidth(bounds.width);
			detachedWindow.setHeight(bounds.height);
			MPartStack stack = modelService.createModelElement(MPartStack.class);
			populatePartStack(stack, detachedWindowReader);
			detachedWindow.getChildren().add(stack);
			perspective.getWindows().add(detachedWindow);
		}
	}

	// TODO review
	private void removeUnnecessaryPartSashes(MPartSashContainerElement element) {
		if (!(element instanceof MPartSashContainer)) {
			return;
		}
		MPartSashContainer sash = ((MPartSashContainer) element);
		List<MPartSashContainerElement> children = sash.getChildren();
		MElementContainer<MUIElement> parent = sash.getParent();
		if (children.isEmpty()) {
			parent.getChildren().remove(sash);
			sash.setParent(null);
			return;
		}
		if (children.size() == 1) {
			parent.getChildren().remove(sash);
			sash.setParent(null);
			parent.getChildren().addAll(sash.getChildren());
			for (MPartSashContainerElement child : sash.getChildren()) {
				child.setParent(parent);
				removeUnnecessaryPartSashes(child);
			}
			sash.getChildren().clear();
		}
	}

	private void hideEmptyStacks() {
		List<MPartStack> stacks = modelService.findElements(perspective, null, MPartStack.class,
				null);
		for (MPartStack stack : stacks) {
			if (ID_EDITOR_AREA.equals(stack.getElementId())
					|| ID_EDITOR_AREA.equals(stack.getParent().getElementId())) {
				continue;
			}
			if (!hasVisibleContent(stack)) {
				if (stack.getTags().contains(TAG_MINIMIZED)) {
					stack.getTags().remove(TAG_MINIMIZED);
					MStackElement firstElement = stack.getChildren().get(0);
					firstElement.setVisible(true);
					firstElement.setToBeRendered(true);
					stack.setSelectedElement(firstElement);
				} else {
					stack.setToBeRendered(false);
				}
			}
		}
	}

	private boolean hasVisibleContent(MPartStack stack) {
		for (MStackElement child : stack.getChildren()) {
			if (child.isVisible() && child.isToBeRendered()) {
				return true;
			}
		}
		return false;
	}

	private void addToPerspective(MPartSashContainerElement element, InfoReader info) {
		if (info.isRelativelyPositioned()) {
			insert(element, info);
		} else {
			perspective.getChildren().add(element);
		}
	}

	private void addEditorArea(InfoReader info) {
		editorAreaPlaceholder = modelService.createModelElement(MPlaceholder.class);
		editorAreaPlaceholder.setElementId(ID_EDITOR_AREA);
		addToPerspective(editorAreaPlaceholder, info);
	}

	private MPartStack addPartStack(InfoReader info) {
		MPartStack stack = createPartStack(info);
		if (info.isRelativelyPositioned()) {
			String refElementId = info.getRelative();
			MUIElement refElement = modelService.find(refElementId, perspective);
			MElementContainer<?> parent = refElement.getParent();
			if (parent instanceof MPartStack) {
				// we don't want to put a stack in a stack
				refElement = parent;
			}

			insert(stack, refElement, info);
		} else {
			perspective.getChildren().add(stack);
		}
		if (info.isMinimized()) {
			stack.getTags().add(TAG_MINIMIZED);
		}
		return stack;
	}

	private void insert(MUIElement element, MUIElement refElement, InfoReader info) {
		float ratio = info.getRatio();
		int relationship = info.getRelationship();
		ModeledPageLayout.insert(element, refElement, ModeledPageLayout.plRelToSwt(relationship),
				ratio);
	}

	private void insert(MUIElement element, InfoReader info) {
		String refElementId = info.getRelative();
		MUIElement refElement = modelService.find(refElementId, perspective);
		insert(element, refElement, info);
	}

	private MPartStack createPartStack(InfoReader info) {
		MPartStack stack = null;
		String stackId = info.getId();
		if (stackId != null) {
			if (stackId.equals(StickyViewDescriptor.STICKY_FOLDER_RIGHT)) {
				stackId = "legacyStickyFolderRight"; //$NON-NLS-1$
			}
		}
		boolean visible = info.isVisible();
		stack = ModeledPageLayout.createStack(stackId, visible);
		return stack;
	}

	private void populatePartStack(MPartStack stack, InfoReader info) {
		List<PageReader> pages = info.getPages();
		for (PageReader page : pages) {
			addPlaceholderToStack(stack, page.getId());
		}
		String selectedElementId = info.getActivePageId();
		MStackElement selectedElement = (MStackElement) modelService.find(selectedElementId, stack);
		// TODO selected element is not always visible - investigate
		if (selectedElement != null) {
			selectedElement.setToBeRendered(true);
			selectedElement.setVisible(true);
		}
		stack.setSelectedElement(selectedElement);
	}

	private void populatePartStack(MPartStack stack, DetachedWindowReader info) {
		List<PageReader> pages = info.getPages();
		for (PageReader page : pages) {
			String id = page.getId();
			addPlaceholderToStack(stack, id);
		}
		String selectedElementId = info.getActivePageId();
		MStackElement selectedElement = (MStackElement) modelService.find(selectedElementId, stack);
		stack.setSelectedElement(selectedElement);
	}

	public void addPlaceholderToStack(MPartStack stack, String partId) {
		MPlaceholder placeholder = modelService.createModelElement(MPlaceholder.class);
		placeholder.setElementId(partId);
		if (!isToBeRendered(placeholder)) {
			placeholder.setToBeRendered(false);
		}
		stack.getChildren().add(placeholder);
		viewPlaceholders.add(placeholder);
	}

	private boolean isToBeRendered(MPlaceholder placeholder) {
		if (renderedViews == null) {
			renderedViews = perspReader.getRenderedViewIds();
		}
		return renderedViews.contains(placeholder.getElementId());
	}

	private void addPerspectiveShortcutTags() {
		List<String> shortcutIds = perspReader.getPerspectiveShortcutIds();
		for (String shortcutId : shortcutIds) {
			tags.add(ModeledPageLayout.PERSP_SHORTCUT_TAG + shortcutId);
		}
	}

	private void addActionSetTags() {
		List<String> actionSetIds = perspReader.getActionSetIds();
		for (String actionSetId : actionSetIds) {
			tags.add(ModeledPageLayout.ACTION_SET_TAG + actionSetId);
		}
	}

	private void addNewWizardTags() {
		List<String> newWizardActionIds = perspReader.getNewWizardActionIds();
		for (String actionId : newWizardActionIds) {
			tags.add(ModeledPageLayout.NEW_WIZARD_TAG + actionId);
		}
	}

	private void addShowViewTags() {
		List<String> showViewActionIds = perspReader.getShowViewActionIds();
		for (String actionId : showViewActionIds) {
			tags.add(ModeledPageLayout.SHOW_VIEW_TAG + actionId);
		}
	}

	private void addHiddenItems() {
		String comma = ","; //$NON-NLS-1$
		StringBuilder value = new StringBuilder();
		List<String> hiddenItems = perspReader.getHiddenMenuItemIds();
		for (String elementId : hiddenItems) {
			value.append(ModeledPageLayout.HIDDEN_MENU_PREFIX);
			value.append(elementId);
			value.append(comma);
		}
		hiddenItems = perspReader.getHiddenToolbarItemIds();
		for (String elementId : hiddenItems) {
			value.append(ModeledPageLayout.HIDDEN_TOOLBAR_PREFIX);
			value.append(elementId);
			value.append(comma);
		}
		Map<String, String> persistedState = perspective.getPersistedState();
		persistedState.put(ModeledPageLayout.HIDDEN_ITEMS_KEY, value.toString());
	}
	
	public List<MPlaceholder> getPlaceholders() {
		return viewPlaceholders;
	}

	public MPlaceholder getEditorAreaPlaceholder() {
		return editorAreaPlaceholder;
	}
}
