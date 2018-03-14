/*******************************************************************************
 * Copyright (c) 2015 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wayne Beaton (The Eclipse Foundation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.quickaccess.IQuickAccessElement;
import org.eclipse.ui.quickaccess.IQuickAccessProvider;

/**
 * This class provides a means of talking to a quick access provider contributed
 * via extension.
 *
 * @since 3.5
 *
 */
public class ExtensionQuickAccessProvider extends QuickAccessProvider {

	static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ALWAYS_PRESENT = "alwaysPresent"; //$NON-NLS-1$
	private static final String ATTRIBUTE_IMAGE = "image"; //$NON-NLS-1$

	private IConfigurationElement configurationElement;
	private IEclipseContext context;
	private IQuickAccessProvider provider;
	private IQuickAccessElement[] rawElements;
	private QuickAccessElement[] wrappedElements;
	private Map<String, QuickAccessElement> idToElement;

	/**
	 * @param element
	 * @param context
	 */
	public ExtensionQuickAccessProvider(IConfigurationElement element, IEclipseContext context) {
		this.configurationElement = element;
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#getId()
	 */
	@Override
	public String getId() {
		String namespace = configurationElement.getNamespaceIdentifier();
		return namespace + "." + configurationElement.getAttribute(ATTRIBUTE_ID); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#getName()
	 */
	@Override
	public String getName() {
		return configurationElement.getAttribute(ATTRIBUTE_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		String path = configurationElement.getAttribute(ATTRIBUTE_IMAGE);
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				configurationElement.getNamespaceIdentifier(), path);
		return descriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#getElements()
	 */
	@Override
	public QuickAccessElement[] getElements() {
		IQuickAccessElement[] elements = getProvider().getElements();
		// If what we get back from the provider is different from what
		// we already have cached, recache.
		if (elements != rawElements) {
			rawElements = elements;
			wrappedElements = new QuickAccessElement[elements.length];
			idToElement = new HashMap<>();
			if (elements != null)
				for (int index = 0; index < elements.length; index++) {
					QuickAccessElement wrapped = new ExtensionQuickAccessElement(this, elements[index]);
					idToElement.put(wrapped.getId(), wrapped);
					wrappedElements[index] = wrapped;
				}
		}
		return wrappedElements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#getElementForId(java.lang.String)
	 */
	@Override
	public QuickAccessElement getElementForId(String id) {
		getElements();
		return idToElement.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#isAlwaysPresent()
	 */
	@Override
	public boolean isAlwaysPresent() {
		// TODO There must be some API for this conversion.
		String value = configurationElement.getAttribute(ATTRIBUTE_ALWAYS_PRESENT);
		return value == null ? false : Boolean.valueOf(value);
	}

	/**
	 * Answers the real provider implementation. This method always returns a
	 * non-<code>null</code> value. If, for some reason, the provider cannot be
	 * created from the information in the extension point, an instance of
	 * {@link NullQuickAccessProvider} is answered.
	 *
	 * @return the real provider
	 */
	public IQuickAccessProvider getProvider() {
		if (provider != null)
			return provider;
		synchronized (this) {
			if (provider != null)
				return provider;
			try {
				Object object = configurationElement.createExecutableExtension(ATTRIBUTE_CLASS);
				if (object instanceof IQuickAccessProvider) {
					provider = (IQuickAccessProvider) object;
					provider.setContext(context);
				}
			} catch (CoreException e) {
				WorkbenchPlugin.log(
						"Unable to Quick Access extension from: " + configurationElement.getNamespaceIdentifier(), e);//$NON-NLS-1$
				provider = new NullQuickAccessProvider();
			}
		}
		return provider;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.quickaccess.QuickAccessProvider#doReset()
	 */
	@Override
	protected void doReset() {
		wrappedElements = null;
		idToElement = null;
		getProvider().reset();
	}
}

class NullQuickAccessProvider implements IQuickAccessProvider {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#getElements()
	 */
	@Override
	public IQuickAccessElement[] getElements() {
		return new IQuickAccessElement[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.quickaccess.IQuickAccessProvider#getElementForId(java.
	 * lang.String)
	 */
	@Override
	public IQuickAccessElement getElementForId(String id) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.quickaccess.IQuickAccessProvider#reset()
	 */
	@Override
	public void reset() {

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.quickaccess.IQuickAccessProvider#setContext(org.eclipse
	 * .e4.core.contexts.IEclipseContext)
	 */
	@Override
	public void setContext(IEclipseContext context) {

	}

}