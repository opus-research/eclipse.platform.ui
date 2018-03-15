/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.addons;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;

/**
 * This addon is used to add and remove the {@link IPresentationEngine#ON_TOP}
 * tag to {@link MStackElement} according to the state whether they can be seen
 * in the current UI or not.
 * <p>
 * In order to track the on top state of {@link MStackElement} instances the
 * following tag change listener can be used.
 * </p>
 *
 * <pre>
 * &#64;Inject
 * &#64;Optional
 * private void subscribeTopicTagsChanged(&#64;UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {
 * 	Object changedObj = event.getProperty(EventTags.ELEMENT);
 *
 * 	if (!(changedObj instanceof MUIElement))
 * 		return;
 *
 * 	final MUIElement changedElement = (MUIElement) changedObj;
 *
 * 	if (UIEvents.isADD(event)) {
 * 		if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE, IPresentationEngine.ON_TOP)) {
 * 			// element is on top now
 * 		}
 * 	} else if (UIEvents.isREMOVE(event)) {
 * 		if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE, IPresentationEngine.ON_TOP)) {
 * 			// element is not on top any more
 * 		}
 * 	}
 * }
 * </pre>
 */
@SuppressWarnings("restriction")
public class ElementOnTopAddon {

	@Inject
	private EModelService modelService;

	/**
	 * Initialize all on top elements at creation by applying the
	 * {@link IPresentationEngine#ON_TOP} to every {@link MStackElement}, where
	 * its contents can be seen in the UI.
	 *
	 * @param event
	 *            {@link Event} containing the widget data
	 *
	 */
	@Inject
	@Optional
	public void subscribeTopicWidget(@UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) Event event) {
		Object element = event.getProperty(EventTags.ELEMENT);
		Object newValue = event.getProperty(EventTags.NEW_VALUE);

		if (element instanceof MStackElement) {
			MStackElement stackElement = (MStackElement) element;
			if (newValue != null && checkOnTop(stackElement)) {
				stackElement.getTags().add(IPresentationEngine.ON_TOP);
			} else {
				stackElement.getTags().remove(IPresentationEngine.ON_TOP);
			}
		}
	}

	/**
	 * This event listener updates the on top status of {@link MStackElement}
	 * instances by adding or removing the {@link IPresentationEngine#ON_TOP}
	 * tag according to the state.
	 *
	 * @param event
	 *            {@link Event} containing the data of the selection change
	 *            event on element container, like {@link MPerspectiveStack} and
	 *            {@link MPartStack}.
	 */
	@Inject
	@Optional
	public void subscribeTopicSelectedElement(
			@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {

		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
		Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);
		Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);

		if (element instanceof MPerspectiveStack) {
			// stack element's contents can be hidden or shown due to a
			// perspective switch ...
			handlePerspectiveSwitch(oldValue, newValue);
		} else if (element instanceof MPartStack) {
			// ... or by changing the selection in a part stack
			handlePartStacksSelection(oldValue, newValue);
		}

	}

	private void handlePerspectiveSwitch(Object oldValue, Object newValue) {
		handleOldPerspective(oldValue);

		handleNewPerspective(newValue);
	}

	private void handleOldPerspective(Object oldValue) {
		if (oldValue instanceof MPerspective) {
			MPerspective oldPerspective = (MPerspective) oldValue;
			List<MStackElement> elements = modelService.findElements(oldPerspective, null, MStackElement.class,
					Collections.singletonList(IPresentationEngine.ON_TOP), EModelService.IN_ACTIVE_PERSPECTIVE);
			// remove on top tag, since the old perspective is currently not
			// shown in the UI.
			elements.forEach(stackElement -> stackElement.getTags().remove(IPresentationEngine.ON_TOP));
		}
	}

	private void handleNewPerspective(Object newValue) {
		if (newValue instanceof MPerspective) {
			MPerspective newPerspective = (MPerspective) newValue;

			// handle selected elements in a MPartStack
			List<MPartStack> partStacks = modelService.findElements(newPerspective, null, MPartStack.class, null,
					EModelService.IN_ACTIVE_PERSPECTIVE);
			partStacks.stream().filter(partStack -> partStack.getSelectedElement() != null && partStack.isVisible())
					.forEach(partStack -> partStack.getSelectedElement().getTags().add(IPresentationEngine.ON_TOP));

			// handle StackElements that are not part of a MPartStack, which are
			// always visible if the perspective is selected
			List<MStackElement> stackElements = modelService.findElements(newPerspective, null, MStackElement.class,
					null, EModelService.IN_ACTIVE_PERSPECTIVE);
			stackElements.stream().filter(this::hasNoPartStackContainer)
					.forEach(stackElement -> stackElement.getTags().add(IPresentationEngine.ON_TOP));
		}
	}

	private boolean checkOnTop(MUIElement uiElement) {
		java.util.Optional<MPartStack> partStackContainer = getPartStackContainer(uiElement);
		if (partStackContainer.isPresent()) {
			// check if the MUIElement is not on top of the part stack...
			if (!uiElement.equals(partStackContainer.get().getSelectedElement())) {
				return false;
			}
		}

		// ... otherwise it must be on top in the UI.
		return true;
	}

	private boolean hasNoPartStackContainer(MUIElement uiElement) {
		MWindow topLevelWindow = modelService.getTopLevelWindowFor(uiElement);
		// shared elements are defined on window level and aren't shown in the
		// UI directly
		if (topLevelWindow.getSharedElements().contains(uiElement)) {
			return false;
		}

		return !getPartStackContainer(uiElement).isPresent();
	}

	private java.util.Optional<MPartStack> getPartStackContainer(MUIElement uiElement) {
		MUIElement parent = modelService.getContainer(uiElement);
		if (parent instanceof MPartStack) {
			return java.util.Optional.of((MPartStack) parent);
		} else if (parent instanceof MWindow) {
			// reached window level so there is no MPartStack as parent
			return java.util.Optional.empty();
		}

		return getPartStackContainer(parent);
	}

	private void handlePartStacksSelection(Object oldValue, Object newValue) {
		if (oldValue instanceof MApplicationElement) {
			MApplicationElement stackElement = (MApplicationElement) oldValue;
			stackElement.getTags().remove(IPresentationEngine.ON_TOP);
		}

		if (newValue instanceof MApplicationElement) {
			MApplicationElement stackElement = (MApplicationElement) newValue;
			stackElement.getTags().add(IPresentationEngine.ON_TOP);
		}
	}

}
