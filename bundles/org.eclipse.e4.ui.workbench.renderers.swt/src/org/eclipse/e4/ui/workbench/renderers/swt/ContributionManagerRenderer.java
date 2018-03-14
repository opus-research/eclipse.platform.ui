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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
@SuppressWarnings("javadoc")
public abstract class ContributionManagerRenderer<Model, ModelEl, Manager, ContrRec>
extends SWTPartRenderer {
	@Inject
	protected Logger logger;

	@Inject
	protected IEventBroker eventBroker;

	@Inject
	protected MApplication application;

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
				logCacheState("Before clean up", window); //$NON-NLS-1$

				clearModelContributionToRecordCache(window);
				clearModelToManagerCache(window);
				clearModelToContributionCache(window);

				logCacheState("After clean up", window); //$NON-NLS-1$
			}
		}

		private void clearModelContributionToRecordCache(MWindow window) {
			Map<Model, Manager> modelToManager = getModelToManager(window);
			if (modelToManager == null) {
				return;
			}
			for (Model model : new ArrayList<Model>(modelToManager.keySet())) {
				cleanUp(model, window);
			}
			Map<ModelEl, ContrRec> modelContributionToRecord = getModelContributionToRecord(window);
			if (modelContributionToRecord != null) {
				modelContributionToRecord.clear();
			}
			Map<ModelEl, ArrayList<ContrRec>> sharedElementToRecord = getSharedElementToRecord(window);
			if (sharedElementToRecord != null) {
				sharedElementToRecord.clear();
			}
		}

		private void clearModelToManagerCache(MWindow window) {
			Map<Model, Manager> modelToManager = getModelToManager(window);
			if (modelToManager != null) {
				modelToManager.clear();
			}
			Map<Manager, Model> managerToMode = getManagerToModel(window);
			if (managerToMode != null) {
				managerToMode.clear();
			}
		}

		private void clearModelToContributionCache(MWindow window) {
			Map<ModelEl, IContributionItem> modelToContribution = getModelToContribution(window);
			if (modelToContribution != null) {
				modelToContribution.clear();
			}

			Map<IContributionItem, ModelEl> contributionToModel = getContributionToModel(window);
			if (contributionToModel != null) {
				contributionToModel.clear();
			}
		}

		private void logCacheState(String phaseName, MWindow window) {
			if (logger.isDebugEnabled()) {
				String[] cacheNames = { getModelToManagerMapName(),
						getManagerToModelMapName(),
						getModelToContributionMapName(),
						getContributionToModelMapName(),
						getModelContributionToRecordMapName(),
						getSharedElementToRecordMapName() };
				for (String cacheName : cacheNames) {
					Object[] args = new Object[] {
							phaseName,
							cacheName,
							((Map<?, ?>) getTransientMap(window, cacheName))
							.size() };
					logger.debug("{0}: {1} = {2}", args); //$NON-NLS-1$
				}
			}
		}
	};

	public void init() {
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET,
				closedWindowHandler);
	}

	public void contextDisposed() {
		eventBroker.unsubscribe(closedWindowHandler);
	}

	protected abstract void cleanUp(Model model, MWindow window);

	protected abstract String getModelToManagerMapName();

	protected abstract String getManagerToModelMapName();

	protected abstract String getModelToContributionMapName();

	protected abstract String getContributionToModelMapName();

	protected abstract String getModelContributionToRecordMapName();

	protected abstract String getSharedElementToRecordMapName();

	protected MWindow getWindow() {
		return application.getSelectedElement();
	}

	protected Map<Model, Manager> getModelToManager(
			MWindow window) {
		Map<Model, Manager> modelToManager = getTransientMap(window,
				getModelToManagerMapName());
		if (modelToManager == null) {
			modelToManager = storeTransientMap(window,
					getModelToManagerMapName(),
					new HashMap<Model, Manager>());
		}
		return modelToManager;
	}

	protected Map<Manager, Model> getManagerToModel(
			MWindow window) {
		Map<Manager, Model> managerToModel = getTransientMap(window,
				getManagerToModelMapName());
		if (managerToModel == null) {
			managerToModel = storeTransientMap(window,
					getManagerToModelMapName(),
					new HashMap<Manager, Model>());
		}
		return managerToModel;
	}

	protected Map<ModelEl, IContributionItem> getModelToContribution(
			MWindow window) {
		Map<ModelEl, IContributionItem> modelToContribution = getTransientMap(
				window, getModelToContributionMapName());
		if (modelToContribution == null) {
			modelToContribution = storeTransientMap(window,
					getModelToContributionMapName(),
					new HashMap<ModelEl, IContributionItem>());
		}
		return modelToContribution;
	}

	protected Map<IContributionItem, ModelEl> getContributionToModel(
			MWindow window) {
		Map<IContributionItem, ModelEl> contributionToModel = getTransientMap(
				window, getContributionToModelMapName());
		if (contributionToModel == null) {
			contributionToModel = storeTransientMap(window,
					getContributionToModelMapName(),
					new HashMap<IContributionItem, ModelEl>());
		}
		return contributionToModel;
	}

	protected Map<ModelEl, ContrRec> getModelContributionToRecord(
			MWindow window) {
		Map<ModelEl, ContrRec> modelContributionToRecord = getTransientMap(
				window, getModelContributionToRecordMapName());
		if (modelContributionToRecord == null) {
			modelContributionToRecord = storeTransientMap(window,
					getModelContributionToRecordMapName(),
					new HashMap<ModelEl, ContrRec>());
		}
		return modelContributionToRecord;
	}

	protected Map<ModelEl, ArrayList<ContrRec>> getSharedElementToRecord(
			MWindow window) {
		Map<ModelEl, ArrayList<ContrRec>> sharedElementToRecord = getTransientMap(
				window, getSharedElementToRecordMapName());
		if (sharedElementToRecord == null) {
			sharedElementToRecord = storeTransientMap(
					window, getSharedElementToRecordMapName(),
					new HashMap<ModelEl, ArrayList<ContrRec>>());
		}
		return sharedElementToRecord;
	}

	@SuppressWarnings("unchecked")
	private <T> T getTransientMap(MWindow window, String name) {
		if (window != null) {
			return (T) window.getTransientData().get(name);
		}
		return (T) Collections.emptyMap();
	}

	private <T> T storeTransientMap(MWindow window, String name, T value) {
		window.getTransientData().put(name, value);
		return value;
	}
}
