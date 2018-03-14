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

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
@SuppressWarnings("javadoc")
public abstract class ContributionManagerRenderer<Model extends MUIElement, ModelEl extends MUIElement, Manager, ContrRec>
extends SWTPartRenderer {
	protected Map<Model, Manager> modelToManager = new HashMap<Model, Manager>();
	protected Map<Manager, Model> managerToModel = new HashMap<Manager, Model>();

	protected Map<ModelEl, IContributionItem> modelToContribution = new HashMap<ModelEl, IContributionItem>();
	protected Map<IContributionItem, ModelEl> contributionToModel = new HashMap<IContributionItem, ModelEl>();

	protected Map<ModelEl, ContrRec> modelContributionToRecord = new HashMap<ModelEl, ContrRec>();
	protected Map<ModelEl, ArrayList<ContrRec>> sharedElementToRecord = new HashMap<ModelEl, ArrayList<ContrRec>>();

	@Inject
	protected Logger logger;

	@Inject
	protected IEventBroker eventBroker;

	@Inject
	protected EModelService modelService;

	private EventHandler closedWindowHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			MUIElement changedElement = (MUIElement) event
					.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MWindow)) {
				return;
			}

			MWindow window = (MWindow) changedElement;
			Shell shell = (Shell) window.getWidget();

			if (shell == null || shell.isDisposed()) {
				logCacheState("Before clean up"); //$NON-NLS-1$
				cleanUpCaches(window);
				logCacheState("After clean up"); //$NON-NLS-1$
			}
		}

		private void cleanUpCaches(MWindow window) {
			for (Model model : new ArrayList<Model>(modelToManager.keySet())) {
				MWindow parentWindow = modelService.getTopLevelWindowFor(model
						.getParent());
				if (window == parentWindow) {
					cleanUp(model);
				}
			}
		}

		private void logCacheState(String phaseName) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"{0}: modelToManager = {1}", new Object[] { phaseName, modelToManager.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: managerToModel = {1}", new Object[] { phaseName, managerToModel.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: modelToContribution = {1}", new Object[] { phaseName, modelToContribution.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: contributionToModel = {1}", new Object[] { phaseName, contributionToModel.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: modelContributionToRecord = {1}", new Object[] { phaseName, modelContributionToRecord.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: sharedElementToRecord = {1}", new Object[] { phaseName, sharedElementToRecord.size() }); //$NON-NLS-1$
			}
		}
	};

	protected void init() {
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET,
				closedWindowHandler);
	}

	protected void contextDisposed() {
		eventBroker.unsubscribe(closedWindowHandler);
	}

	protected abstract void cleanUp(Model model);
}
