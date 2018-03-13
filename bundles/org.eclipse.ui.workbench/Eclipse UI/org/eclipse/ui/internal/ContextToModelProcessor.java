/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.internal.contexts.ContextPersistence;

/**
 * @since 3.5
 *
 */
public class ContextToModelProcessor {
	private Map<String, MBindingContext> contexts = new HashMap<String, MBindingContext>();
	private Set<MBindingContext> contextFromModel = new HashSet<MBindingContext>();

	@Execute
	void process(MApplication application, IEclipseContext context, EModelService modelService) {
		gatherContexts(application.getRootContext());
		ContextManager contextManager = context.get(ContextManager.class);
		if (contextManager == null) {
			contextManager = new ContextManager();
			context.set(ContextManager.class, contextManager);
		}
		ContextPersistence cp = new ContextPersistence(contextManager);
		cp.reRead();
		collectContexts(application.getBindingContexts());
		generateContexts(application, contextManager, cp, modelService);
		cp.dispose();
		contexts.clear();
		contextFromModel.clear();
	}

	/**
	 * @param bindingContexts
	 */
	private void collectContexts(List<MBindingContext> bindingContexts) {
		for (MBindingContext ctx : bindingContexts) {
			contextFromModel.add(ctx);
			collectContexts(ctx.getChildren());
		}
	}

	/**
	 * @param application
	 * @param contextManager
	 * @param modelService
	 * @param cp
	 */
	private void generateContexts(MApplication application, ContextManager contextManager,
			ContextPersistence cp, EModelService modelService) {
		Set<MBindingContext> definedContexts = new HashSet<MBindingContext>();
		for (Context ctx : contextManager.getDefinedContexts()) {
			try {
				MBindingContext contextModel = contexts.get(ctx.getId());
				if (contextModel == null) {
					contextModel = modelService.createModelElement(MBindingContext.class);
					contexts.put(ctx.getId(), contextModel);
				}
				definedContexts.add(contextModel);
				contextModel.setElementId(ctx.getId());
				contextModel.setName(ctx.getName());
				contextModel.setDescription(ctx.getDescription());
				IConfigurationElement cfg = cp.getContextsToConfiguration().get(ctx);
				if (cfg != null) {
					contextModel.setContributorURI(URIHelper.constructPlatformURI(cfg
							.getContributor()));
				}

			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (MBindingContext mctx : contextFromModel) {
			if (!definedContexts.contains(mctx) && mctx.getContributorURI() != null
					&& mctx.getContributorURI().startsWith("platform:")) { //$NON-NLS-1$
				EObject parent = ((EObject) mctx).eContainer();
				if (parent instanceof MBindingContext) {
					((MBindingContext) parent).getChildren().remove(mctx);
				} else if (parent instanceof MApplication) {
					((MApplication) parent).getBindingContexts().remove(mctx);
				}
			}
		}
		for (Context ctx : contextManager.getDefinedContexts()) {
			try {
				MBindingContext contextModel = contexts.get(ctx.getId());
				String parentId = ctx.getParentId();
				if (parentId == null) {
					if (!application.getRootContext().contains(contextModel)) {
						application.getRootContext().add(contextModel);
					}
				} else {
					MBindingContext parentContextModel = contexts.get(parentId);
					if (parentContextModel == null) {
						System.err.println("Could not find parent " + parentId + " for child " //$NON-NLS-1$ //$NON-NLS-2$
								+ ctx.getId());
					} else {
						if (!parentContextModel.getChildren().contains(contextModel)) {
							parentContextModel.getChildren().add(contextModel);
						}
					}
				}
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param contextList
	 */
	private void gatherContexts(List<MBindingContext> contextList) {
		for (MBindingContext ctx : contextList) {
			gatherContexts(ctx);
		}
	}

	/**
	 * @param ctx
	 */
	private void gatherContexts(MBindingContext ctx) {
		if (ctx == null) {
			return;
		}
		contexts.put(ctx.getElementId(), ctx);
		gatherContexts(ctx.getChildren());
	}

}
