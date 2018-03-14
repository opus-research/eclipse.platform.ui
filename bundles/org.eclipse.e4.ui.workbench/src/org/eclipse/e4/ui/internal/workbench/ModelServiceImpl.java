/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPlaceholderResolver;
import org.eclipse.e4.ui.workbench.modeling.ElementMatcher;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
public class ModelServiceImpl implements EModelService {
	private static String HOSTED_ELEMENT = "HostedElement"; //$NON-NLS-1$

	private IEclipseContext appContext;

	/** Factory which is able to create {@link MApplicationElement}s in a generic way. */
	private GenericMApplicationElementFactoryImpl mApplicationElementFactory;

	// Cleans up after a hosted element is disposed
	private EventHandler hostedElementHandler = new EventHandler() {

		@Override
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!changedElement.getTags().contains(HOSTED_ELEMENT)) {
				return;
			}

			if (changedElement.getWidget() != null) {
				return;
			}

			EObject eObj = (EObject) changedElement;
			if (!(eObj.eContainer() instanceof MWindow)) {
				return;
			}

			MWindow hostingWindow = (MWindow) eObj.eContainer();
			hostingWindow.getSharedElements().remove(changedElement);
			changedElement.getTags().remove(HOSTED_ELEMENT);
		}
	};

	/**
	 * This is a singleton service. One instance is used throughout the running application
	 *
	 * @param appContext
	 *            The applicationContext to get the eventBroker from
	 *
	 * @throws NullPointerException
	 *             if the given appContext is <code>null</code>
	 */
	public ModelServiceImpl(IEclipseContext appContext) {
		if (appContext == null)
		 {
			throw new NullPointerException("No application context given!"); //$NON-NLS-1$
		}

		this.appContext = appContext;
		IEventBroker eventBroker = appContext.get(IEventBroker.class);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, hostedElementHandler);

		mApplicationElementFactory = new GenericMApplicationElementFactoryImpl(
				appContext.get(IExtensionRegistry.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T extends MApplicationElement> T createModelElement(Class<T> elementType) {
		if (elementType == null) {
			throw new NullPointerException("Argument cannot be null."); //$NON-NLS-1$
		}

		T back = (T) mApplicationElementFactory.createEObject(elementType);
		if (back != null) {
			return back;
		}

		throw new IllegalArgumentException(
				"Unsupported model object type: " + elementType.getCanonicalName()); //$NON-NLS-1$
	}

	private <T> void findElementsRecursive(MApplicationElement searchRoot, Class<T> clazz,
			Selector matcher, List<T> elements, int searchFlags) {
		Assert.isLegal(searchRoot != null);
		if (searchFlags == 0) {
			return;
		}

		// are *we* a match ?
		if (matcher.select(searchRoot)) {
			if (!elements.contains(searchRoot)) {
				elements.add((T) searchRoot);
			}
		}

		if (searchRoot instanceof MApplication && (searchFlags == ANYWHERE)) {
			MApplication app = (MApplication) searchRoot;

			for (MHandler child : app.getHandlers()) {
				findElementsRecursive(child, clazz, matcher, elements, searchFlags);
			}

			for (MCommand command : app.getCommands()) {
				findElementsRecursive(command, clazz, matcher, elements, searchFlags);
			}
		}
		
		// Check regular containers
		if (searchRoot instanceof MElementContainer<?>) {
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;

			if (searchRoot instanceof MPerspectiveStack) {
				if ((searchFlags & IN_ANY_PERSPECTIVE) != 0) {
					// Search *all* the perspectives
					for (MUIElement child : container.getChildren()) {
						findElementsRecursive(child, clazz, matcher, elements, searchFlags);
					}
				} else if ((searchFlags & IN_ACTIVE_PERSPECTIVE) != 0) {
					// Only search the currently active perspective, if any
					MPerspective active = ((MPerspectiveStack) searchRoot).getSelectedElement();
					if (active != null) {
						findElementsRecursive(active, clazz, matcher, elements, searchFlags);
					}
				} else if ((searchFlags & IN_SHARED_AREA) != 0 && searchRoot instanceof MUIElement) {
					// Only recurse through the shared areas
					List<MArea> areas = findElements((MUIElement) searchRoot, null, MArea.class,null);
					for (MArea area : areas) {
						findElementsRecursive(area, clazz, matcher, elements, searchFlags);
					}
				} 
//				else if ((searchFlags & IN_PART) != 0) {
//					List<MPart> parts = findElements((MUIElement) searchRoot, null, MPart.class,null);
//					for (MPart part : parts) {
//						findElementsRecursive(part, clazz, matcher, elements, searchFlags);
//					}
//				}
			} else {

				for (MUIElement child : container.getChildren()) {
					findElementsRecursive(child, clazz, matcher, elements, searchFlags);
				}
			}
		}

		// Search Trim
		if (searchRoot instanceof MTrimmedWindow && (searchFlags & IN_TRIM) != 0) {
			MTrimmedWindow tw = (MTrimmedWindow) searchRoot;
			List<MTrimBar> bars = tw.getTrimBars();
			for (MTrimBar bar : bars) {
				findElementsRecursive(bar, clazz, matcher, elements, searchFlags);
			}
		}

		// Search Detached Windows
		if (searchRoot instanceof MWindow) {
			MWindow window = (MWindow) searchRoot;
			for (MWindow dw : window.getWindows()) {
				findElementsRecursive(dw, clazz, matcher, elements, searchFlags);
			}

			MMenu menu = window.getMainMenu();
			if (menu != null && (searchFlags & IN_MAIN_MENU) != 0) {
				findElementsRecursive(menu, clazz, matcher, elements, searchFlags);
			}

			// Check for Handlers
			if (searchFlags == ANYWHERE) {
				for (MHandler child : window.getHandlers()) {
					findElementsRecursive(child, clazz, matcher, elements, searchFlags);
				}
			}
		}

		if (searchRoot instanceof MPerspective) {
			MPerspective persp = (MPerspective) searchRoot;
			for (MWindow dw : persp.getWindows()) {
				findElementsRecursive(dw, clazz, matcher, elements, searchFlags);
			}
		}
		// Search shared elements
		if (searchRoot instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) searchRoot;

			// Don't search in shared areas unless the flag is set
			if (ph.getRef() != null
					&& (!(ph.getRef() instanceof MArea) || (searchFlags & IN_SHARED_AREA) != 0)) {
				findElementsRecursive(ph.getRef(), clazz, matcher, elements, searchFlags);
			}
		}

		if (searchRoot instanceof MPart){
			MPart part = (MPart) searchRoot;
			if ( (searchFlags & IN_PART) != 0) {

				for (MMenu menu : part.getMenus()) {
					findElementsRecursive(menu, clazz, matcher, elements, searchFlags);
				}

				MToolBar toolBar = part.getToolbar();
				if (toolBar != null) {
					findElementsRecursive(toolBar, clazz, matcher, elements, searchFlags);
				}
			}
			// Check for Handlers
			for (MHandler child : part.getHandlers()) {
				findElementsRecursive(child, clazz, matcher, elements, searchFlags);
			}
		}

	}

	@Override
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch) {
		ElementMatcher matcher = new ElementMatcher(id, clazz, tagsToMatch);
		return findElements(searchRoot, clazz, ANYWHERE, matcher);
	}

	@Override
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch, int searchFlags) {
		ElementMatcher matcher = new ElementMatcher(id, clazz, tagsToMatch);
		return findElements(searchRoot, clazz, searchFlags, matcher);
	}

	@Override
	public <T> List<T> findElements(MApplicationElement searchRoot, Class<T> clazz,
			int searchFlags, Selector matcher) {
		List<T> elements = new ArrayList<T>();
		findElementsRecursive(searchRoot, clazz, matcher, elements, searchFlags);
		return elements;
	}

	private <T> List<T> findPerspectiveElements(MUIElement searchRoot, String id,
			Class<T> clazz,
			List<String> tagsToMatch) {
		List<T> elements = new ArrayList<T>();
		ElementMatcher matcher = new ElementMatcher(id, clazz, tagsToMatch);
		findElementsRecursive(searchRoot, clazz, matcher, elements, PRESENTATION);
		return elements;
	}

	@Override
	public MUIElement find(String id, MUIElement searchRoot) {
		if (id == null || id.length() == 0) {
			return null;
		}

		List<MUIElement> elements = findElements(searchRoot, id, MUIElement.class, null);
		if (elements.size() > 0) {
			return elements.get(0);
		}
		return null;
	}

	@Override
	public int countRenderableChildren(MUIElement element) {
		if (!(element instanceof MElementContainer<?>)) {
			return 0;
		}

		MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) element;
		int count = 0;
		List<MUIElement> kids = container.getChildren();
		for (MUIElement kid : kids) {
			if (kid.isToBeRendered()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public IEclipseContext getContainingContext(MUIElement element) {
		return ModelUtils.getContainingContext(element);
	}

	@Override
	public MUIElement cloneElement(MUIElement element, MSnippetContainer snippetContainer) {
		EObject eObj = (EObject) element;
		MUIElement clone = (MUIElement) EcoreUtil.copy(eObj);

		// null out all the references
		List<MPlaceholder> phList = findElements(clone, null, MPlaceholder.class, null);
		for (MPlaceholder ph : phList) {
			// Skip placeholders in the sharedArea
			int location = getElementLocation(ph);
			if ((location & IN_SHARED_AREA) != 0) {
				continue;
			}

			ph.setRef(null);
		}

		if (snippetContainer != null) {
			MUIElement snippet = findSnippet(snippetContainer, element.getElementId());
			if (snippet != null) {
				snippetContainer.getSnippets().remove(snippet);
			}
			snippetContainer.getSnippets().add(clone);
		}

		// Cache the original element in the clone's transientData
		clone.getTransientData().put(CLONED_FROM_KEY, element);

		return clone;
	}

	@Override
	public MUIElement cloneSnippet(MSnippetContainer snippetContainer, String snippetId,
			MWindow refWin) {
		if (snippetContainer == null || snippetId == null || snippetId.length() == 0) {
			return null;
		}

		MApplicationElement elementToClone = null;
		for (MApplicationElement snippet : snippetContainer.getSnippets()) {
			if (snippetId.equals(snippet.getElementId())) {
				elementToClone = snippet;
				break;
			}
		}
		if (elementToClone == null) {
			return null;
		}

		EObject eObj = (EObject) elementToClone;
		MUIElement element = (MUIElement) EcoreUtil.copy(eObj);

		MUIElement appElement = refWin == null ? null : refWin.getParent();
		if (appElement instanceof MApplication) {
			EPlaceholderResolver resolver = ((MApplication) appElement).getContext().get(
					EPlaceholderResolver.class);

			// Re-resolve any placeholder references
			List<MPlaceholder> phList = findElements(element, null, MPlaceholder.class, null);
			for (MPlaceholder ph : phList) {
				resolver.resolvePlaceholderRef(ph, refWin);
			}
		}

		return element;
	}

	@Override
	public MUIElement findSnippet(MSnippetContainer snippetContainer, String id) {
		if (snippetContainer == null || id == null || id.length() == 0) {
			return null;
		}

		List<MUIElement> snippets = snippetContainer.getSnippets();
		for (MUIElement snippet : snippets) {
			if (id.equals(snippet.getElementId())) {
				return snippet;
			}
		}

		return null;
	}

	@Override
	public void bringToTop(MUIElement element) {
		if (element instanceof MApplication) {
			return;
		}

		MWindow window = getTopLevelWindowFor(element);
		if (window == element) {
			if (!element.isToBeRendered()) {
				element.setToBeRendered(true);
			}

			window.getParent().setSelectedElement(window);
		} else {
			showElementInWindow(window, element);
		}
		UIEvents.publishEvent(UIEvents.UILifeCycle.BRINGTOTOP, element);
	}

	private void showElementInWindow(MWindow window, MUIElement element) {
		MUIElement parent = element.getParent();
		if (parent == null) {
			MPlaceholder ph = findPlaceholderFor(window, element);
			if (ph != null) {
				element = ph;
				parent = element.getParent();
			}
		}

		if (parent == null && element instanceof MWindow) {
			// no parent but is a window, could be a detached window then
			parent = (MUIElement) ((EObject) element).eContainer();
			if (parent != null) {
				// Force the element to be rendered
				if (!element.isToBeRendered()) {
					element.setToBeRendered(true);
				}

				if (window != parent) {
					showElementInWindow(window, parent);
				}
			}
		} else if (parent != null) {
			// Force the element to be rendered
			if (!element.isToBeRendered()) {
				element.setToBeRendered(true);
			}

			((MElementContainer<MUIElement>) parent).setSelectedElement(element);
			if (window != parent) {
				showElementInWindow(window, parent);
			}
		}
	}

	@Override
	public MPlaceholder findPlaceholderFor(MWindow window, MUIElement element) {
		List<MPlaceholder> phList = findPerspectiveElements(window, null, MPlaceholder.class, null);
		List<MPlaceholder> elementRefs = new ArrayList<MPlaceholder>();
		for (MPlaceholder ph : phList) {
			if (ph.getRef() == element) {
				elementRefs.add(ph);
			}
		}

		if (elementRefs.size() == 0) {
			return null;
		}

		if (elementRefs.size() == 1) {
			return elementRefs.get(0);
		}

		// If there is more than one placeholder then return the one in the shared area
		for (MPlaceholder refPh : elementRefs) {
			int loc = getElementLocation(refPh);
			if ((loc & IN_SHARED_AREA) != 0) {
				return refPh;
			}
		}

		// Just return the first one
		return elementRefs.get(0);
	}

	@Override
	public void move(MUIElement element, MElementContainer<MUIElement> newParent) {
		move(element, newParent, -1, false);
	}

	@Override
	public void move(MUIElement element, MElementContainer<MUIElement> newParent,
			boolean leavePlaceholder) {
		move(element, newParent, -1, leavePlaceholder);
	}

	@Override
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index) {
		move(element, newParent, index, false);
	}

	@Override
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index,
			boolean leavePlaceholder) {
		// Cache where we were
		MElementContainer<MUIElement> curParent = element.getParent();
		int curIndex = curParent.getChildren().indexOf(element);

		// Move the model element
		newParent.getChildren().add(index, element);

		if (leavePlaceholder) {
			MPlaceholder ph = MAdvancedFactory.INSTANCE.createPlaceholder();
			ph.setRef(element);
			curParent.getChildren().add(curIndex, ph);
		}
	}

	private void combine(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			MPartSashContainer newSash, boolean newFirst, float ratio) {
		MElementContainer<MUIElement> curParent = relTo.getParent();
		int index = curParent.getChildren().indexOf(relTo);
		curParent.getChildren().remove(relTo);
		if (newFirst) {
			newSash.getChildren().add(toInsert);
			newSash.getChildren().add(relTo);
		} else {
			newSash.getChildren().add(relTo);
			newSash.getChildren().add(toInsert);
		}

		// Set up the container data before adding the new sash to the model
		// To raise the granularity assume 100% == 10,000
		int adjustedPct = (int) (ratio * 10000);
		toInsert.setContainerData(Integer.toString(adjustedPct));
		relTo.setContainerData(Integer.toString(10000 - adjustedPct));

		// add the new sash at the same location
		curParent.getChildren().add(index, newSash);
	}

	@Override
	public void insert(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			int where, float ratio) {
		assert (toInsert != null && relTo != null);
		assert (ratio > 0 && ratio < 100);

		MUIElement relToParent = relTo.getParent();

		// determine insertion order
		boolean insertBefore = where == ABOVE || where == LEFT_OF;
		boolean horizontal = where == LEFT_OF || where == RIGHT_OF;

		// Case 1: 'relTo' is already an MPSC, can we just add to it ?
		// Case 2: relTo's parent is an MPSC, can we just add to it ?
		// Case 3: We need to make a new Sash and replace relTo with it after 'combining' toInsert
		// and relTo
		if (relTo instanceof MPartSashContainer
				&& directionsMatch((MPartSashContainer) relTo, horizontal)) {
			MPartSashContainer psc = (MPartSashContainer) relTo;
			int totalVisWeight = 0;
			for (MUIElement child : psc.getChildren()) {
				if (child.isToBeRendered()) {
					totalVisWeight += getWeight(child);
				}
			}
			int insertWeight = (int) ((totalVisWeight * ratio) / (1 - ratio));
			toInsert.setContainerData(Integer.toString(insertWeight));
			if (insertBefore) {
				psc.getChildren().add(0, toInsert);
			} else {
				psc.getChildren().add(toInsert);
			}
		} else if (relToParent instanceof MPartSashContainer && !(relToParent instanceof MArea)
				&& directionsMatch((MPartSashContainer) relToParent, horizontal)) {
			MPartSashContainer psc = (MPartSashContainer) relToParent;
			int relToIndex = psc.getChildren().indexOf(relTo);

			int relToWeight = getWeight(relTo);
			int insertWeight = (int) (relToWeight * ratio);
			toInsert.setContainerData(Integer.toString(insertWeight));
			relTo.setContainerData(Integer.toString(relToWeight - insertWeight));

			if (insertBefore) {
				psc.getChildren().add(relToIndex, toInsert);
			} else {
				int insertIndex = relToIndex + 1;
				if (insertIndex < psc.getChildren().size()) {
					psc.getChildren().add(insertIndex, toInsert);
				} else {
					psc.getChildren().add(toInsert);
				}
			}
		} else {
			MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			newSash.setHorizontal(horizontal);

			// Maintain the existing weight in the new sash
			newSash.setContainerData(relTo.getContainerData());

			combine(toInsert, relTo, newSash, insertBefore, ratio);
		}

		if (relToParent != null) {
			return;
		}

		// We're either relative to an MPSC or to some
		// The only thing we can add sashes to is an MPartSashContainer, an MWindow or an
		// MPerspective find the correct place to start the insertion
		MUIElement insertRoot = relTo.getParent();
		if (insertRoot instanceof MPerspective) {
			insertRoot = relTo;
		}
		while (insertRoot != null && !(insertRoot instanceof MWindow)
				&& !(insertRoot instanceof MPerspective)
				&& !(insertRoot instanceof MPartSashContainer)) {
			relTo = (MPartSashContainerElement) insertRoot;
			insertRoot = insertRoot.getParent();
		}

		if (insertRoot instanceof MWindow || insertRoot instanceof MArea
				|| insertRoot instanceof MPerspective) {
			// OK, we're certainly going to need a new sash
			MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			newSash.setHorizontal(where == LEFT_OF || where == RIGHT_OF);
			combine(toInsert, relTo, newSash, insertBefore, ratio);
		} else if (insertRoot instanceof MGenericTile<?>) {
			MGenericTile<MUIElement> curTile = (MGenericTile<MUIElement>) insertRoot;

			// do we need a new sash or can we extend the existing one?
			if (curTile.isHorizontal() && (where == ABOVE || where == BELOW)) {
				MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
				newSash.setHorizontal(false);
				newSash.setContainerData(relTo.getContainerData());
				combine(toInsert, relTo, newSash, insertBefore, ratio);
			} else if (!curTile.isHorizontal() && (where == LEFT_OF || where == RIGHT_OF)) {
				MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
				newSash.setHorizontal(true);
				newSash.setContainerData(relTo.getContainerData());
				combine(toInsert, relTo, newSash, insertBefore, ratio);
			} else {
				// We just need to add to the existing sash
				int relToIndex = relTo.getParent().getChildren().indexOf(relTo);
				if (insertBefore) {
					curTile.getChildren().add(relToIndex, toInsert);
				} else {
					curTile.getChildren().add(relToIndex + 1, toInsert);
				}

				// Adjust the sash weights by taking the ratio
				int relToWeight = 10000;
				if (relTo.getContainerData() != null) {
					try {
						relToWeight = Integer.parseInt(relTo.getContainerData());
					} catch (NumberFormatException e) {
					}
				}
				int toInsertWeight = (int) ((ratio / 100.0) * relToWeight + 0.5);
				relToWeight = relToWeight - toInsertWeight;
				relTo.setContainerData(Integer.toString(relToWeight));
				toInsert.setContainerData(Integer.toString(toInsertWeight));
			}
		}
	}

	private int getWeight(MUIElement element) {
		int relToWeight = 10000;
		if (element.getContainerData() != null) {
			try {
				relToWeight = Integer.parseInt(element.getContainerData());
			} catch (NumberFormatException e) {
			}
		}
		return relToWeight;
	}

	private boolean directionsMatch(MPartSashContainer psc, boolean horizontal) {
		boolean pscHorizontal = psc.isHorizontal();
		return (pscHorizontal && horizontal) || (!pscHorizontal && !horizontal);
	}

	@Override
	public void detach(MPartSashContainerElement element, int x, int y, int width, int height) {
		// If we're showing through a placehoilder then detach it...
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}

		// Determine the correct parent for the new window
		MWindow window = getTopLevelWindowFor(element);
		MPerspective thePersp = getPerspectiveFor(element);

		MTrimmedWindow newWindow = MBasicFactory.INSTANCE.createTrimmedWindow();

		newWindow.setX(x);
		newWindow.setY(y);
		newWindow.setWidth(width);
		newWindow.setHeight(height);

		element.getParent().getChildren().remove(element);
		MWindowElement uiRoot = wrapElementForWindow(element);
		newWindow.getChildren().add(uiRoot);

		if (thePersp != null) {
			thePersp.getWindows().add(newWindow);
		} else if (window != null) {
			window.getWindows().add(newWindow);
		}
	}

	/**
	 * Wraps an element in a PartStack if it's a MPart or an MPlaceholder that references an MPart
	 *
	 * @param element
	 *            The element to be wrapped
	 * @return The wrapper for the given element
	 */
	private MWindowElement wrapElementForWindow(MPartSashContainerElement element) {
		if (element instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) element).getRef();
			if (ref instanceof MPart) {
				MPartStack newPS = MBasicFactory.INSTANCE.createPartStack();
				newPS.getChildren().add((MPlaceholder) element);
				return newPS;
			}
		} else if (element instanceof MPart) {
			MPartStack newPS = MBasicFactory.INSTANCE.createPartStack();
			newPS.getChildren().add((MPart) element);
			return newPS;
		} else if (element instanceof MWindowElement) {
			return (MWindowElement) element;
		}
		return null;
	}

	@Override
	public MTrimBar getTrim(MTrimmedWindow window, SideValue sv) {
		List<MTrimBar> bars = window.getTrimBars();
		for (MTrimBar bar : bars) {
			if (bar.getSide() == sv) {
				return bar;
			}
		}

		// Didn't find a trim bar for the side, make one
		MTrimBar newBar = BasicFactoryImpl.eINSTANCE.createTrimBar();

		// Assign default ids to the trim bars to match legacy eclipse
		if (sv == SideValue.TOP) {
			newBar.setElementId("org.eclipse.ui.main.menu"); //$NON-NLS-1$
		} else if (sv == SideValue.BOTTOM) {
			newBar.setElementId("org.eclipse.ui.trim.status"); //$NON-NLS-1$
		} else if (sv == SideValue.LEFT) {
			newBar.setElementId("org.eclipse.ui.trim.vertical1"); //$NON-NLS-1$
		} else if (sv == SideValue.RIGHT) {
			newBar.setElementId("org.eclipse.ui.trim.vertical2"); //$NON-NLS-1$
		}

		newBar.setSide(sv);
		window.getTrimBars().add(newBar);
		return newBar;
	}

	@Override
	public MWindow getTopLevelWindowFor(MUIElement element) {
		EObject eObj = (EObject) element;
		while (eObj != null && !(eObj.eContainer() instanceof MApplication)) {
			eObj = eObj.eContainer();
		}

		if (eObj instanceof MWindow) {
			return (MWindow) eObj;
		}

		return null; // Ooops!
	}

	@Override
	public MPerspective getPerspectiveFor(MUIElement element) {

		while (true) {
			// if we have a placeholder, reassign ourselves
			MPlaceholder placeholder = element.getCurSharedRef();
			if (placeholder != null) {
				element = placeholder;
			}
			EObject container = ((EObject) element).eContainer();
			if (container == null || container instanceof MApplication) {
				// climbed to the top and found nothing, return null
				return null;
			} else if (container instanceof MPerspectiveStack) {
				// parent is a perspective stack, we ourselves should be a perspective
				return (MPerspective) element;
			}

			// climb up
			element = (MUIElement) container;
		}
	}

	@Override
	public void resetPerspectiveModel(MPerspective persp, MWindow window) {
		resetPerspectiveModel(persp, window, true);
	}

	private void resetPerspectiveModel(MPerspective persp, MWindow window,
			boolean removeSharedPlaceholders) {
		if (persp == null) {
			return;
		}

		if (removeSharedPlaceholders) {
			// Remove any views (Placeholders) from the shared area
			EPartService ps = window.getContext().get(EPartService.class);
			List<MArea> areas = findElements(window, null, MArea.class, null);
			if (areas.size() == 1) {
				MArea area = areas.get(0);

				// Strip out the placeholders in visible stacks
				List<MPlaceholder> phList = findElements(area, null, MPlaceholder.class, null);
				for (MPlaceholder ph : phList) {
					ps.hidePart((MPart) ph.getRef());
					ph.getParent().getChildren().remove(ph);
				}

				// Prevent shared stacks ids from clashing with the ones in the perspective
				List<MPartStack> stacks = findElements(area, null, MPartStack.class, null);
				for (MPartStack stack : stacks) {
					String generatedId = "PartStack@" + Integer.toHexString(stack.hashCode()); //$NON-NLS-1$
					stack.setElementId(generatedId);
				}

				// Also remove any min/max tags on the area (or its placeholder)
				MUIElement areaPresentation = area;
				if (area.getCurSharedRef() != null) {
					areaPresentation = area.getCurSharedRef();
				}

				areaPresentation.getTags().remove(IPresentationEngine.MAXIMIZED);
				areaPresentation.getTags().remove(IPresentationEngine.MINIMIZED);
				areaPresentation.getTags().remove(IPresentationEngine.MINIMIZED_BY_ZOOM);
			}
		}

		// Remove any minimized stacks for this perspective
		List<MTrimBar> bars = findElements(window, null, MTrimBar.class, null);
		List<MToolControl> toRemove = new ArrayList<MToolControl>();
		for (MTrimBar bar : bars) {
			for (MUIElement barKid : bar.getChildren()) {
				if (!(barKid instanceof MToolControl)) {
					continue;
				}
				String id = barKid.getElementId();
				if (id != null && id.contains(persp.getElementId())) {
					toRemove.add((MToolControl) barKid);
				}
			}
		}

		for (MToolControl toolControl : toRemove) {
			// Close any open fast view
			toolControl.setToBeRendered(false);
			toolControl.getParent().getChildren().remove(toolControl);
		}
	}

	@Override
	public void removePerspectiveModel(MPerspective persp, MWindow window) {
		// pick a new perspective to become active (if any)
		MUIElement psElement = persp.getParent();
		MPerspectiveStack ps = (MPerspectiveStack) psElement;
		boolean foundNewSelection = false;
		if (ps.getSelectedElement() == persp) {
			for (MPerspective p : ps.getChildren()) {
				if (p != persp && p.isToBeRendered()) {
					ps.setSelectedElement(p);
					foundNewSelection = true;
					break;
				}
			}

			if (!foundNewSelection) {
				ps.setSelectedElement(null);
			}
		}

		// Remove transient elements (minimized stacks, detached windows)
		resetPerspectiveModel(persp, window, false);

		// unrender the perspective and remove it
		persp.setToBeRendered(false);
		ps.getChildren().remove(persp);
	}

	@Override
	public MPerspective getActivePerspective(MWindow window) {
		List<MPerspectiveStack> pStacks = findElements(window, null, MPerspectiveStack.class, null);
		if (pStacks.size() == 1) {
			return pStacks.get(0).getSelectedElement();
		}

		return null;
	}

	@Override
	public int toBeRenderedCount(MElementContainer<?> container) {
		int count = 0;
		for (MUIElement child : container.getChildren()) {
			if (child.isToBeRendered()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public MUIElement getContainer(MUIElement element) {
		if (element == null) {
			return null;
		}

		return (MUIElement) ((EObject) element).eContainer();
	}

	@Override
	public int getElementLocation(MUIElement element) {
		if (element == null) {
			return NOT_IN_UI;
		}

		// If the element is shared then use its current placeholder
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}

		MUIElement curElement = element;
		while (curElement != null) {
			MUIElement parent = curElement.getParent();
			if (parent instanceof MPerspective) {
				MElementContainer<MUIElement> perspectiveParent = parent.getParent();
				if (perspectiveParent == null) {
					return NOT_IN_UI;
				} else if (perspectiveParent.getSelectedElement() == parent) {
					return IN_ACTIVE_PERSPECTIVE;
				} else {
					return IN_ANY_PERSPECTIVE;
				}
			} else if (parent instanceof MApplication) {
				return OUTSIDE_PERSPECTIVE;
			} else if (parent instanceof MTrimBar) {
				return IN_TRIM;
			} else if (parent == null) {
				EObject container = ((EObject) curElement).eContainer();

				// DW tests
				if (container instanceof MWindow) {
					MWindow containerWin = (MWindow) container;
					if (containerWin.getSharedElements().contains(curElement)) {
						return IN_SHARED_AREA;
					}

					EObject containerParent = container.eContainer();
					if (containerParent instanceof MPerspective) {
						MElementContainer<MUIElement> perspectiveParent = ((MPerspective) containerParent)
								.getParent();
						if (perspectiveParent == null) {
							return NOT_IN_UI;
						}
						int location = IN_ANY_PERSPECTIVE;
						if (perspectiveParent.getSelectedElement() == containerParent) {
							location |= IN_ACTIVE_PERSPECTIVE;
						}
						return location;
					} else if (containerParent instanceof MWindow) {
						return OUTSIDE_PERSPECTIVE;
					} else {
						return NOT_IN_UI;
					}
				}
			}
			curElement = parent;
		}

		return NOT_IN_UI;
	}

	@Override
	public MPartDescriptor getPartDescriptor(String id) {
		MApplication application = appContext.get(MApplication.class);

		// If the id contains a ':' use the part before it as the descriptor id
		int colonIndex = id == null ? -1 : id.indexOf(':');
		String descId = colonIndex == -1 ? id : id.substring(0, colonIndex);

		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getElementId().equals(descId)) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public void hideLocalPlaceholders(MWindow window, MPerspective perspective) {
		List<MPlaceholder> globals = findElements(window, null, MPlaceholder.class, null,
				OUTSIDE_PERSPECTIVE | IN_SHARED_AREA);

		// Iterate across the perspective(s) removing any 'local' placeholders
		List<MPerspective> persps = new ArrayList<MPerspective>();
		if (perspective != null) {
			persps.add(perspective);
		} else {
			persps = findElements(window, null, MPerspective.class, null);
		}

		for (MPerspective persp : persps) {
			List<MPlaceholder> locals = findElements(persp, null, MPlaceholder.class, null,
					IN_ANY_PERSPECTIVE);
			for (MPlaceholder local : locals) {
				for (MPlaceholder global : globals) {
					if (global.getRef() == local.getRef()) {
						local.setToBeRendered(false);
						MElementContainer<MUIElement> localParent = local.getParent();
						setStackVisibility(localParent);
					}
				}
			}
		}
	}

	/**
	 * @param parent
	 */
	private void setStackVisibility(MElementContainer<MUIElement> parent) {
		for (MUIElement child : parent.getChildren()) {
			if (child.isToBeRendered() && child.isVisible()) {
				parent.setToBeRendered(true);
				return;
			}
		}
		parent.setToBeRendered(false);
		// continue modifying the visibility as the parent's parent may also
		// need to be hidden from the user
		setStackVisibility(parent.getParent());
	}

	@Override
	public boolean isLastEditorStack(MUIElement stack) {
		if (!(stack instanceof MPartStack)) {
			return false;
		}

		// is it in the shared area?
		MUIElement parent = stack.getParent();
		while (parent != null && !(parent instanceof MArea)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			return false;
		}

		// OK, it's in the area, is it the last TBR one ?
		MArea area = (MArea) parent;
		List<MPartStack> stacks = findElements(area, null, MPartStack.class, null);
		int count = 0;
		for (MPartStack aStack : stacks) {
			if (aStack.isToBeRendered()) {
				count++;
			}
		}
		return count < 2 && stack.isToBeRendered();
	}

	@Override
	public void hostElement(MUIElement element, MWindow hostWindow, Object uiContainer,
			IEclipseContext hostContext) {
		// This is subtle; unless the element is hooked into the model it won't fire events
		hostWindow.getSharedElements().add(element);
		element.getTags().add(HOSTED_ELEMENT);

		IPresentationEngine renderer = hostWindow.getContext().get(IPresentationEngine.class);
		renderer.createGui(element, uiContainer, hostContext);
	}

	@Override
	public boolean isHostedElement(MUIElement element, MWindow hostWindow) {
		MUIElement curElement = element;
		while (curElement != null && !curElement.getTags().contains(HOSTED_ELEMENT)) {
			if (curElement.getCurSharedRef() != null) {
				curElement = curElement.getCurSharedRef();
			} else {
				curElement = curElement.getParent();
			}
		}

		if (curElement == null) {
			return false;
		}

		return hostWindow.getSharedElements().contains(curElement);
	}
}
