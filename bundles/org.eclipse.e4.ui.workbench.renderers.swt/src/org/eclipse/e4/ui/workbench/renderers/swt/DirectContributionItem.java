/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joseph Carroll <jdsalingerjr@gmail.com> - Bug 385414 Contributing wizards
 *     to toolbar always displays icon and text
 *     Bruce Skingle <Bruce.Skingle@immutify.com> - Bug 443092
 *     Jonas Helming - Bug 410087
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A contribution item for direct contributions (without a command)
 */
public class DirectContributionItem extends AbstractContributionItem {


	private static final String DCI_STATIC_CONTEXT = "DCI-staticContext"; //$NON-NLS-1$

	private static final Object missingExecute = new Object();

	private MItem model;

	private IEclipseContext infoContext;

	@Inject
	private IContributionFactory contribFactory;

	@Override
	public void setModel(MItem item) {
		super.setModel(item);
		model = item;
		setId(model.getElementId());
		updateVisible();
	}

	@Override
	protected void updateMenuItem() {
		MenuItem item = (MenuItem) widget;
		String text = model.getLocalizedLabel();
		if (text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	@Override
	protected void updateToolItem() {
		ToolItem item = (ToolItem) widget;
		final String text = model.getLocalizedLabel();
		Image icon = item.getImage();
		boolean mode = model.getTags().contains(FORCE_TEXT);
		if ((icon == null || mode) && text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = model.getLocalizedTooltip();
		item.setToolTipText(tooltip);
		item.setSelection(model.isSelected());
		item.setEnabled(model.isEnabled());
	}

	@Override
	protected void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
			widget = null;
			Object obj = model.getTransientData().get(DISPOSABLE);
			if (obj instanceof Runnable) {
				((Runnable) obj).run();
			}
			model.setWidget(null);
			disposeOldImages();
		}
	}

	@Override
	public void dispose() {
		if (widget != null) {
			widget.dispose();
			widget = null;
			model.setWidget(null);
		}
	}

	private IEclipseContext getStaticContext(Event event) {
		if (infoContext == null) {
			infoContext = EclipseContextFactory.create(DCI_STATIC_CONTEXT);
			ContributionsAnalyzer.populateModelInterfaces(model, infoContext,
					model.getClass().getInterfaces());
		}
		if (event == null) {
			infoContext.remove(Event.class);
		} else {
			infoContext.set(Event.class, event);
		}
		return infoContext;
	}

	@Override
	protected void executeItem(Event trigger) {
		final IEclipseContext lclContext = getContext(model);
		if (!checkContribution(lclContext)) {
			return;
		}
		MContribution contrib = (MContribution) model;
		IEclipseContext staticContext = getStaticContext(trigger);
		Object result = ContextInjectionFactory.invoke(contrib.getObject(), Execute.class,
				getExecutionContext(lclContext), staticContext, missingExecute);
		if (result == missingExecute && logger != null) {
			logger.error("Contribution is missing @Execute: " + contrib.getContributionURI()); //$NON-NLS-1$
		}
	}

	@Override
	protected boolean canExecuteItem(Event trigger) {
		final IEclipseContext lclContext = getContext(model);
		if (!checkContribution(lclContext)) {
			return false;
		}
		MContribution contrib = (MContribution) model;
		IEclipseContext staticContext = getStaticContext(trigger);
		Boolean result = ((Boolean) ContextInjectionFactory.invoke(
				contrib.getObject(), CanExecute.class,
				getExecutionContext(lclContext), staticContext, Boolean.TRUE));
		return result.booleanValue();
	}

	/**
	 * Return the execution context for the @CanExecute and @Execute methods.
	 * This should be the same as the execution context used by the
	 * EHandlerService.
	 *
	 * @param context
	 *            the context for this item
	 * @return the execution context
	 */
	private IEclipseContext getExecutionContext(IEclipseContext context) {
		if (context == null)
			return null;

		return context.getActiveLeaf();
	}

	private boolean checkContribution(IEclipseContext lclContext) {
		if (!(model instanceof MContribution)) {
			return false;
		}
		MContribution contrib = (MContribution) model;
		if (contrib.getObject() == null) {
			contrib.setObject(contribFactory.create(
					contrib.getContributionURI(), lclContext));
		}
		return contrib.getObject() != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.workbench.renderers.swt.AbstractContributionItem#
	 * handleHelpRequest()
	 */
	@Override
	protected void handleHelpRequest() {
		// Do nothing, as direct items have currently no help ID

	}

}
