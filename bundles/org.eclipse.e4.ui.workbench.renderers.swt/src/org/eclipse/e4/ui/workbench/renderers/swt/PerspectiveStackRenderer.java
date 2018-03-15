/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class PerspectiveStackRenderer extends LazyStackRenderer {

	@Inject
	private IPresentationEngine renderer;

	@Inject
	private IEventBroker eventBroker;

	@PostConstruct
	public void init() {
		super.init(eventBroker);
	}

	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPerspectiveStack)
				|| !(parent instanceof Composite))
			return null;

		Composite perspStack = new Composite((Composite) parent, SWT.NONE);
		IStylingEngine stylingEngine = (IStylingEngine) getContext(element)
				.get(IStylingEngine.SERVICE_NAME);
		stylingEngine.setClassname(perspStack, "perspectiveLayout"); //$NON-NLS-1$
		perspStack.setLayout(new StackLayout());

		return perspStack;
	}

	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);

		MPerspectiveStack ps = (MPerspectiveStack) element;
		if (ps.getSelectedElement() != null
				&& ps.getSelectedElement().getWidget() != null) {
			Control ctrl = (Control) ps.getSelectedElement().getWidget();
			Composite psComp = (Composite) ps.getWidget();
			StackLayout sl = (StackLayout) psComp.getLayout();
			sl.topControl = ctrl;
			psComp.layout();
		}
	}

	@Override
	protected void showTab(final MUIElement tabElement) {
		MPerspective persp = (MPerspective) tabElement;

		final MPerspective oldPersp = (MPerspective) persp.getTransientData().get(LazyStackRenderer.SELECTED_BEFORE);
		setPerspectiveChangeHelper(oldPersp);

		Control ctrl = (Control) persp.getWidget();
		if (ctrl == null) {
			ctrl = (Control) renderer.createGui(persp);
		} else if (ctrl.getParent() != persp.getParent().getWidget()) {
			Composite parent = (Composite) persp.getParent().getWidget();
			ctrl.setParent(parent);
		}

		super.showTab(persp);

		// relayout the perspective
		final Composite psComp = ctrl.getParent();
		StackLayout sl = (StackLayout) psComp.getLayout();
		if (sl != null) {
			sl.topControl = ctrl;
			psComp.layout();
		}

		ctrl.moveAbove(null);

		unsetPerspectiveChangeHelper(oldPersp);

		// Force a context switch
		final IEclipseContext context = persp.getContext();
		context.get(EPartService.class).switchPerspective(persp);

		// Move any other controls to 'limbo'
		Control[] kids = psComp.getChildren();
		Shell limbo = (Shell) context.get("limbo"); //$NON-NLS-1$
		for (Control child : kids) {
			if (child != ctrl) {
				child.setParent(limbo);
			}
		}
	}

	private void setPerspectiveChangeHelper(MPerspective oldPersp) {
		if (oldPersp == null) {
			return;
		}
		final IEclipseContext context = oldPersp.getContext();
		final MPart activePart = context.get(EPartService.class).getActivePart();

		// See bug 489335: if we are middle in perspective switch code, the
		// new parts for the next perspective are created in
		// PerspectiveStackRenderer before the previous perspective
		// has a chance to deactivate the old active part via
		// switchPerspective().
		// If now the client code in the new perspective asks for an active
		// part, we should not return the view part from the old perspective,
		// even if it is still "active" here.
		ContextFunction helper = new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (activePart == null) {
					return null;
				}
				boolean isView = activePart.getTags().contains("View"); //$NON-NLS-1$
				if (isView) {
					return null;
				}
				return activePart;
			}
		};
		oldPersp.getTransientData().put(PartServiceImpl.PERSPECTIVE_CHANGE_HELPER, helper);
	}

	private void unsetPerspectiveChangeHelper(MPerspective oldPersp) {
		if (oldPersp != null) {
			oldPersp.getTransientData().remove(PartServiceImpl.PERSPECTIVE_CHANGE_HELPER);
		}
	}
}
