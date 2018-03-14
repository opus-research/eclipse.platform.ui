/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     René Brandstetter - Bug 404231 - resetPerspectiveModel() does not reset the perspective
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.services.internal.events.EventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.ElementContainer;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * An EventHandler which takes care of perspective switches.
 * 
 * <p>
 * Whenever a perspective is activated it will check if there is already a snippet of it available.
 * If none is found it will create one so it can be used to reset the perspective to it's original
 * state.
 * </p>
 */
@SuppressWarnings("restriction")
/* IEventBroker which is needed. */
public class E4PerspectiveKeeper implements EventHandler {

	/**
	 * The context used to find the required objects {@link EModelService} and {@link MApplication}.
	 */
	private final IEclipseContext context;

	/**
	 * Sole constructor.
	 * 
	 * @param context
	 *            the context to find the required objects ({@link EModelService} and
	 *            {@link MApplication})
	 */
	private E4PerspectiveKeeper(IEclipseContext context) {
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will only react on {@link MPerspectiveStack} sets.
	 * </p>
	 */
	// TODO: adapt implementation (and javadoc) if Bug 408681 is fixed
	public void handleEvent(Event event) {
		// shortcuts to leave the method a soon as possible
		if (!UIEvents.isSET(event))
			return;
		if (!(event.getProperty(EventTags.ELEMENT) instanceof MPerspectiveStack))
			return;

		Object perspectiveToDisp = event.getProperty(EventTags.NEW_VALUE);

		if (perspectiveToDisp instanceof MPerspective) {
			MPerspective perspective = (MPerspective) perspectiveToDisp;

			EModelService modelService = context.get(EModelService.class);
			if (modelService == null)
				return;

			MApplication application = context.get(MApplication.class);
			if (application == null)
				return;

			if (modelService.findSnippet(application, perspective.getElementId()) == null) {
				// no snippet exists so far, create a new one
				modelService.cloneElement(perspective, application, false);
			}
		}
	}

	/**
	 * Registers a new instance of the {@link E4PerspectiveKeeper} on the {@link EventBroker} found
	 * in the given {@link IEclipseContext}.
	 * 
	 * <p>
	 * The topic used for the registration: {@link ElementContainer#TOPIC_SELECTEDELEMENT}
	 * </p>
	 * 
	 * @param context
	 *            the context used to find all required services (will also be used in the returned
	 *            {@link E4PerspectiveKeeper} instance
	 * @return the {@link E4PerspectiveKeeper} event handler registered in the EventBroker
	 * @throws IllegalArgumentException
	 *             if either the context is <code>null</code> or no {@link EventBroker} can be found
	 *             in the context
	 */
	public static E4PerspectiveKeeper registerNewInstance(IEclipseContext context) {
		return registerNewInstance(null, context);
	}

	/**
	 * Registers a new instance of the {@link E4PerspectiveKeeper} on the {@link EventBroker} found
	 * in the given {@link IEclipseContext}.
	 * 
	 * <p>
	 * The topic used for the registration: {@link ElementContainer#TOPIC_SELECTEDELEMENT}
	 * </p>
	 * 
	 * @param eventBroker
	 *            the EventBroker to register the {@link E4PerspectiveKeeper} to (can be
	 *            <code>null</code> which will try to find an {@link EventBroker} in the given
	 *            context)
	 * @param context
	 *            the context used to find all required services (will also be used in the returned
	 *            {@link E4PerspectiveKeeper} instance
	 * @return the {@link E4PerspectiveKeeper} event handler registered in the EventBroker
	 * @throws IllegalArgumentException
	 *             if either the context is <code>null</code> or no {@link EventBroker} was
	 *             specified and afterwards can't be found in the given context
	 */
	public static E4PerspectiveKeeper registerNewInstance(IEventBroker eventBroker,
			IEclipseContext context) {

		if (context == null)
			throw new IllegalArgumentException("No IEclipseContext given!"); //$NON-NLS-1$

		if (eventBroker == null) {
			eventBroker = context.get(IEventBroker.class);
			if (eventBroker == null) {
				throw new IllegalArgumentException("No IEventBroker for registration given!"); //$NON-NLS-1$
			}
		}

		E4PerspectiveKeeper perspKeeper = new E4PerspectiveKeeper(context);
		// TODO: switch topic if Bug 408681 is fixed
		eventBroker.subscribe(ElementContainer.TOPIC_SELECTEDELEMENT, perspKeeper);
		return perspKeeper;
	}
}
