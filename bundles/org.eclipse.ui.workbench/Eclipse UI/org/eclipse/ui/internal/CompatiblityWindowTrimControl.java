package org.eclipse.ui.internal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.layout.IWindowTrim;

/**
 * An E4 wrapper to host an {@link IWindowTrim} element as an
 * {@link MToolControl}. Assumes the trim element is located in the hosting
 * {@linkplain MToolControl#getTransientData()} under key of
 * {@link IWindowTrim}. The trim element's control is reparented to this
 * element.
 *
 * Exists solely to host the Intro LaunchBar element. May be removed without
 * notice.
 * 
 * @since 3.5
 * @noreference
 */
public class CompatiblityWindowTrimControl {
	static final String BUNDLECLASS_URI = "bundleclass://org.eclipse.ui.workbench/" //$NON-NLS-1$
			+ CompatiblityWindowTrimControl.class.getName();

	@Inject
	MToolControl container;

	@PostConstruct
	void init(Composite parent) {
		IWindowTrim trim = (IWindowTrim) container.getTransientData().get(IWindowTrim.class.getName());
		trim.getControl().setParent(parent);
		parent.requestLayout();
	}
}
