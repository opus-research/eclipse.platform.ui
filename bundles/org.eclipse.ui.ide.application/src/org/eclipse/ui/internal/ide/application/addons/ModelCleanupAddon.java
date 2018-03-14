/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 445663
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application.addons;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.service.event.Event;

/**
 * @since 3.3
 *
 */
public class ModelCleanupAddon {

	private static String COMPATIBILITY_EDITOR_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"; //$NON-NLS-1$
	private static String COMPATIBILITY_VIEW_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$

	@Inject
	@Optional
	private MApplication application;

	/**
	 * This addon listens to the {@link UILifeCycle#APP_STARTUP_COMPLETE} event.
	 *
	 * @param event
	 *            {@link Event}
	 */
	@Inject
	@Optional
	public void applicationStartUp(@EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		List<MPartDescriptor> descriptors = application.getDescriptors();
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		for (Iterator<MPartDescriptor> iterator = descriptors.iterator(); iterator.hasNext();) {
			MPartDescriptor partDescriptor = iterator.next();
			String contributionURI = partDescriptor.getContributionURI();
			if (!(COMPATIBILITY_EDITOR_URI.equals(contributionURI))
					&& !(COMPATIBILITY_VIEW_URI.equals(contributionURI))) {
				String substring = contributionURI.substring(14);
				String[] split = substring.split("/"); //$NON-NLS-1$
				int lastIndexOf = split[1].lastIndexOf(".");//$NON-NLS-1$

				String packagename = split[1].substring(0, lastIndexOf);

				BundleCapability findPackage = findPackage(packagename, split[0], bundle.getBundleContext());
				if (findPackage != null) {
					Class<?> findClass = findClass(split[1], findPackage);
					if (null == findClass) {
						iterator.remove();
						System.err.println("Class not found: " + partDescriptor.getContributionURI()); //$NON-NLS-1$
					}
				} else {
					iterator.remove();
					System.err.println("Package not found: " + partDescriptor.getContributionURI()); //$NON-NLS-1$
				}
			} else {
				if (partDescriptor instanceof CompatibilityPart) {
					// WorkbenchPartReference reference = ((CompatibilityPart)
					// partDescriptor).getReference();
					// TODO also remove compability part
				}
			}

		}
	}

	private BundleCapability findPackage(final String packageName, String bundleSymbolicName,
			BundleContext bundleContext) {
		Requirement req = new Requirement() {
			@Override
			public Resource getResource() {
				// no resource
				return null;
			}

			@Override
			public String getNamespace() {
				return PackageNamespace.PACKAGE_NAMESPACE;
			}

			@Override
			public Map<String, String> getDirectives() {
				return Collections.singletonMap(Namespace.REQUIREMENT_FILTER_DIRECTIVE, "(" //$NON-NLS-1$
						+ PackageNamespace.PACKAGE_NAMESPACE + "=" + packageName + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			public Map<String, Object> getAttributes() {
				return Collections.emptyMap();
			}
		};
		Collection<BundleCapability> packages = bundleContext.getBundle(Constants.SYSTEM_BUNDLE_LOCATION)
				.adapt(FrameworkWiring.class).findProviders(req);
		for (BundleCapability pkg : packages) {
			if (bundleSymbolicName.equals(pkg.getRevision().getSymbolicName())) {
				return pkg;
			}
		}
		return null;
	}

	private Class<?> findClass(String className, BundleCapability pkg) {
		BundleRevision revision = pkg.getRevision();
		BundleWiring wiring = revision.getWiring();
		if (wiring == null) {
			return null;
		}
		if ((revision.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			// fragment case; need to get the host wiring
			wiring = wiring.getRequiredWires(HostNamespace.HOST_NAMESPACE).get(0).getProviderWiring();
		}
		try {
			return wiring.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
