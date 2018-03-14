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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public abstract class ContributionManagerRenderer<Model extends MUIElement, ModelEl extends MUIElement, Manager, ContrRec>
		extends SWTPartRenderer {
	private final String MANAGER_FOR_MODEL = getClass().getName() + ".managerForModel"; //$NON-NLS-1$

	private final String CONTRIBUTION_FOR_MODEL = getClass().getName() + ".contributionForModel"; //$NON-NLS-1$

	private Map<Manager, WeakReference<Model>> managerToModel = new WeakHashMap<Manager, WeakReference<Model>>();

	private Map<IContributionItem, WeakReference<ModelEl>> contributionToModel = new WeakHashMap<IContributionItem, WeakReference<ModelEl>>();

	private Map<ModelEl, ContrRec> modelContributionToRecord = new WeakHashMap<ModelEl, ContrRec>();

	private Map<ModelEl, ArrayList<ContrRec>> sharedElementToRecord = new WeakHashMap<ModelEl, ArrayList<ContrRec>>();

	@Inject
	protected Logger logger;

	@Inject
	protected IEventBroker eventBroker;

	@Inject
	protected EModelService modelService;

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
				logCacheState("Before clean up"); //$NON-NLS-1$
				cleanUpCaches(window);
				logCacheState("After clean up"); //$NON-NLS-1$
			}
		}

		private void cleanUpCaches(MWindow window) {
			for (Iterator<WeakReference<Model>> iter = managerToModel.values()
					.iterator(); iter.hasNext();) {
				Model model = iter.next().get();
				if (model != null) {
					MWindow parentWindow = modelService
							.getTopLevelWindowFor(model
						.getParent());
					if (window == parentWindow) {
						cleanUp(model);
					}
				}
			}

			for (ModelEl modelEl : new ArrayList<ModelEl>(
					sharedElementToRecord.keySet())) {
				MWindow parentWindow = modelService
						.getTopLevelWindowFor(modelEl.getParent());
				if (window == parentWindow) {
					sharedElementToRecord.remove(modelEl);
				}
			}
		}

		private void logCacheState(String phaseName) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"{0}: managerToModel = {1}", new Object[] { phaseName, managerToModel.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: contributionToModel = {1}", new Object[] { phaseName, contributionToModel.size() }); //$NON-NLS-1$
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

	public Model getModel(Manager manager) {
		WeakReference<Model> model = managerToModel.get(manager);
		return model != null ? model.get() : null;
	}

	public ModelEl getModelElement(IContributionItem item) {
		WeakReference<ModelEl> modelEl = contributionToModel.get(item);
		return modelEl != null ? modelEl.get() : null;
	}

	public void linkModelToManager(Model model, Manager manager) {
		model.getTransientData().put(MANAGER_FOR_MODEL, manager);
		managerToModel.put(manager, new WeakReference<Model>(model));
	}

	public void clearModelToManager(Model model, Manager manager) {
		model.getTransientData().remove(MANAGER_FOR_MODEL);
		managerToModel.remove(manager);
	}

	@SuppressWarnings("unchecked")
	public Manager getManager(Model model) {
		return (Manager) model.getTransientData().get(MANAGER_FOR_MODEL);
	}

	public void linkModelToContribution(ModelEl modelEl, IContributionItem item) {
		modelEl.getTransientData().put(CONTRIBUTION_FOR_MODEL, item);
		contributionToModel.put(item, new WeakReference<ModelEl>(modelEl));
	}

	public IContributionItem getContribution(ModelEl modelEl) {
		return (IContributionItem) modelEl.getTransientData().get(
				CONTRIBUTION_FOR_MODEL);
	}

	public void clearModelToContribution(ModelEl modelEl, IContributionItem item) {
		modelEl.getTransientData().remove(CONTRIBUTION_FOR_MODEL);
		contributionToModel.remove(item);
	}

	public ContrRec getContributionRecord(ModelEl element) {
		return modelContributionToRecord.get(element);
	}

	public void linkElementToContributionRecord(ModelEl element, ContrRec record) {
		modelContributionToRecord.put(element, record);
	}

	/**
	 * Search the records for testing. Look, but don't touch!
	 *
	 * @return the array of active ContributionRecords.
	 */
	@SuppressWarnings("unchecked")
	public ContrRec[] getContributionRecords() {
		HashSet<ContrRec> records = new HashSet<ContrRec>(
				modelContributionToRecord.values());
		return (ContrRec[]) records.toArray();
	}

	protected Map<ModelEl, ContrRec> getModelContributionToRecord() {
		return modelContributionToRecord;
	}

	public List<ContrRec> getList(ModelEl item) {
		ArrayList<ContrRec> tmp = sharedElementToRecord.get(item);
		if (tmp == null) {
			tmp = new ArrayList<ContrRec>();
			sharedElementToRecord.put(item, tmp);
		}
		return tmp;
	}

	public void addRecord(ModelEl item, ContrRec rec) {
		getList(item).add(rec);
	}

	public void removeRecord(ModelEl item, ContrRec rec) {
		ArrayList<ContrRec> tmp = sharedElementToRecord.get(item);
		if (tmp != null) {
			tmp.remove(rec);
			if (tmp.isEmpty()) {
				sharedElementToRecord.remove(item);
			}
		}
	}

}
