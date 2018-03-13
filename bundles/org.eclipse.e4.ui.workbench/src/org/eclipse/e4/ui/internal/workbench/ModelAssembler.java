/*******************************************************************************
 * Copyright (c) 2010, 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
 */
public class ModelAssembler {
	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$

	/**
	 * Process the model
	 */
	public void processModel() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = topoSort(extPoint.getExtensions());

		List<MApplicationElement> imports = new ArrayList<MApplicationElement>();
		List<MApplicationElement> addedElements = new ArrayList<MApplicationElement>();

		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();
		ResourceSet resourceSet = applicationResource.getResourceSet();

		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if (!"processor".equals(ce.getName()) || !Boolean.parseBoolean(ce.getAttribute("beforefragment"))) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				runProcessor(ce);
			}
		}

		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if (!"fragment".equals(ce.getName())) { //$NON-NLS-1$
					continue;
				}
				IContributor contributor = ce.getContributor();
				String attrURI = ce.getAttribute("uri"); //$NON-NLS-1$
				if (attrURI == null) {
					logger.warn("Unable to find location for the model extension \"{0}\"", //$NON-NLS-1$
							contributor.getName());
					continue;
				}

				URI uri;

				try {
					// check if the attrURI is already a platform URI
					if (URIHelper.isPlatformURI(attrURI)) {
						uri = URI.createURI(attrURI);
					} else {
						String bundleName = contributor.getName();
						String path = bundleName + '/' + attrURI;
						uri = URI.createPlatformPluginURI(path, false);
					}
				} catch (RuntimeException e) {
					logger.warn(e, "Model extension has invalid location"); //$NON-NLS-1$
					continue;
				}

				String contributorURI = URIHelper.constructPlatformURI(contributor);
				Resource resource;
				try {
					resource = resourceSet.getResource(uri, true);
				} catch (RuntimeException e) {
					logger.warn(e, "Unable to read model extension from " + uri.toString()); //$NON-NLS-1$
					continue;
				}

				EList<?> contents = resource.getContents();
				if (contents.isEmpty()) {
					continue;
				}

				Object extensionRoot = contents.get(0);

				if (!(extensionRoot instanceof MModelFragments)) {
					logger.warn("Unable to create model extension \"{0}\"", //$NON-NLS-1$
							contributor.getName());
					continue;
				}

				MModelFragments fragmentsContainer = (MModelFragments) extensionRoot;
				List<MModelFragment> fragments = fragmentsContainer.getFragments();
				boolean evalImports = false;
				for (MModelFragment fragment : fragments) {
					List<MApplicationElement> elements = fragment.getElements();
					if (elements.size() == 0) {
						continue;
					}

					for (MApplicationElement el : elements) {
						EObject o = (EObject) el;

						E4XMIResource r = (E4XMIResource) o.eResource();
						applicationResource.setID(o, r.getID(o));

						if (contributorURI != null)
							el.setContributorURI(contributorURI);

						// Remember IDs of subitems
						TreeIterator<EObject> treeIt = EcoreUtil.getAllContents(o, true);
						while (treeIt.hasNext()) {
							EObject eObj = treeIt.next();
							r = (E4XMIResource) eObj.eResource();
							if (contributorURI != null && (eObj instanceof MApplicationElement))
								((MApplicationElement) eObj).setContributorURI(contributorURI);
							applicationResource.setID(eObj, r.getInternalId(eObj));
						}
					}

					List<MApplicationElement> merged = fragment.merge(application);

					if (merged.size() > 0) {
						evalImports = true;
						addedElements.addAll(merged);
					} else {
						logger.info("Nothing to merge for \"{0}\"", uri); //$NON-NLS-1$				
					}
				}

				if (evalImports) {
					List<MApplicationElement> localImports = fragmentsContainer.getImports();
					if (localImports != null) {
						imports.addAll(localImports);
					}
				}
			}
		}

		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if (!"processor".equals(ce.getName()) || Boolean.parseBoolean(ce.getAttribute("beforefragment"))) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}

				runProcessor(ce);
			}
		}

		resolveImports(imports, addedElements);
	}

	private void runProcessor(IConfigurationElement ce) {
		IEclipseContext localContext = EclipseContextFactory.create();
		IContributionFactory factory = context.get(IContributionFactory.class);

		for (IConfigurationElement ceEl : ce.getChildren("element")) { //$NON-NLS-1$
			String id = ceEl.getAttribute("id"); //$NON-NLS-1$

			if (id == null) {
				logger.warn("No element id given"); //$NON-NLS-1$
				continue;
			}

			String key = ceEl.getAttribute("contextKey"); //$NON-NLS-1$
			if (key == null) {
				key = id;
			}

			MApplicationElement el = findElementById(application, id);
			if (el == null) {
				logger.warn("Could not find element with id '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			localContext.set(key, el);
		}

		try {
			Object o = factory
					.create("bundleclass://" + ce.getContributor().getName() + "/" + ce.getAttribute("class"), //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
							context, localContext);
			if (o == null) {
				logger.warn("Unable to create processor " + ce.getAttribute("class") + " from " + ce.getContributor().getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				ContextInjectionFactory.invoke(o, Execute.class, context, localContext);
			}
		} catch (Exception e) {
			logger.warn(e, "Could not run processor"); //$NON-NLS-1$
		}
	}

	private void resolveImports(List<MApplicationElement> imports,
			List<MApplicationElement> addedElements) {
		if (imports.isEmpty())
			return;
		// now that we have all components loaded, resolve imports
		Map<MApplicationElement, MApplicationElement> importMaps = new HashMap<MApplicationElement, MApplicationElement>();
		for (MApplicationElement importedElement : imports) {
			MApplicationElement realElement = findElementById(application,
					importedElement.getElementId());
			if (realElement == null) {
				logger.warn("Could not resolve an import element for '" + realElement + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			importMaps.put(importedElement, realElement);
		}

		TreeIterator<EObject> it = EcoreUtil.getAllContents(addedElements);
		List<Runnable> commands = new ArrayList<Runnable>();

		// TODO Probably use EcoreUtil.UsageCrossReferencer
		while (it.hasNext()) {
			EObject o = it.next();

			EContentsEList.FeatureIterator<EObject> featureIterator = (EContentsEList.FeatureIterator<EObject>) o
					.eCrossReferences().iterator();
			while (featureIterator.hasNext()) {
				EObject importObject = featureIterator.next();
				if (importObject.eContainmentFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					EStructuralFeature feature = featureIterator.feature();

					MApplicationElement el = importMaps.get(importObject);
					if (el == null) {
						logger.warn("Could not resolve import for " + el); //$NON-NLS-1$
					}

					final EObject interalTarget = o;
					final EStructuralFeature internalFeature = feature;
					final MApplicationElement internalElment = el;
					final EObject internalImportObject = importObject;

					commands.add(new Runnable() {

						public void run() {
							if (internalFeature.isMany()) {
								System.err.println("Replacing"); //$NON-NLS-1$
								List<Object> l = (List<Object>) interalTarget.eGet(internalFeature);
								int index = l.indexOf(internalImportObject);
								if (index >= 0) {
									l.set(index, internalElment);
								}
							} else {
								interalTarget.eSet(internalFeature, internalElment);
							}
						}
					});
				}
			}
		}

		for (Runnable cmd : commands) {
			cmd.run();
		}
	}

	/**
	 * Sort the provided extensions by the dependencies of their contributors. Note that sorting is
	 * done in-place.
	 * 
	 * @param extensions
	 *            the list of extensions to be sorted
	 * @return the same list of extensions in a topologically-sorted order
	 */
	private IExtension[] topoSort(IExtension[] extensions) {
		if (extensions.length <= 1) { // empty array or an array with one element are always sorted
			return extensions;
		}

		// enrich information about extension (which bundle provides which extensions)
		Map<String, TopoSortValue> contributorMapping = new HashMap<String, TopoSortValue>();
		for (IExtension extension : extensions) {
			String bundleSymName = extension.getContributor().getName();
			TopoSortValue value = contributorMapping.get(bundleSymName);
			if (value == null) {
				contributorMapping.put(bundleSymName, value = new TopoSortValue(bundleSymName));
			}
			value.getExtensions().add(extension);
		}

		// list of bundles which provide model contributions (those are the one of interest in the
		// dependency detection/counting method, all other dependencies are irrelevant)
		Set<String> relevantBundleNames = contributorMapping.keySet();

		/*
		 * Loop through the list of contributing bundles and resolve their dependency to the other
		 * contributing bundles. This gives us the chance to load extensions form bundles with less
		 * dependencies before loading the extensions of those with a lot of dependencies.
		 * 
		 * Note this implementation assumes direct dependencies: if any of the bundles are dependent
		 * through a third bundle, then the ordering will fail. To prevent this would require
		 * recording the entire dependency subgraph for all contributors of the {@code extensions}.
		 */
		for (Map.Entry<String, TopoSortValue> entry : contributorMapping.entrySet()) {
			entry.getValue().addDependencies(resolveRequires(entry.getKey(), relevantBundleNames));
		}

		// sort by out-degree (extensions coming from a bundle with less dependencies will be listed
		// at the beginning)
		List<TopoSortValue> sortedOrder = new ArrayList<TopoSortValue>(contributorMapping.values());
		Collections.sort(sortedOrder);

		// reorder the original IExtensions array
		int i = 0;

		// reverser iteration, because the remove()-method doesn't have to do an array copy
		for (int tsvIndex = sortedOrder.size() - 1; tsvIndex >= 0; tsvIndex--) {
			TopoSortValue tsv = sortedOrder.get(tsvIndex);
			if (tsv.hasUnresolvedDependencies()) {
				// reorder required, because there should be some elements
				// which do not have any dependencies anymore
				Collections.sort(sortedOrder);
			}

			tsv = sortedOrder.remove(tsvIndex);
			// this element shouldn't have any dependencies anymore, otherwise
			// we probably have a cycle in the dependencies
			if (tsv.hasUnresolvedDependencies()) {
				logger.warn(
						"Extensions in '{0}' have a cycle to {1}!", new Object[] { tsv.getSymbolicNameOfProvider(), tsv.requiredBundlesToStirng() }); //$NON-NLS-1$
			}

			for (IExtension ext : tsv.getExtensions()) {
				extensions[i++] = ext;
			}

			for (TopoSortValue leftElements : sortedOrder) {
				// mark the dependency as resolved in the still
				// left TopoSortValues
				leftElements.resolvedDependency(tsv.getSymbolicNameOfProvider());
			}
		}

		assert i == extensions.length;

		return extensions;
	}

	/**
	 * Resolves the direct dependencies from a bundle to the bundles named in the given set and
	 * returns the amount of those dependencies.
	 * 
	 * @param bundleSymName
	 *            the symbolic name of a bundle for which the dependencies to other bundles should
	 *            resolved
	 * @param relevantBundleNames
	 *            a filter to count only the dependencies to the bundles mentioned in it
	 * @return the number of direct dependencies to bundles mentioned in the given set
	 */
	private static Set<String> resolveRequires(String bundleSymName, Set<String> relevantBundleNames) {
		Bundle bundle = Activator.getDefault().getBundleForName(bundleSymName);

		// a set to remove duplicate symbolic name entries
		Set<String> bundleDep = new HashSet<String>();

		if (bundle != null) {
			BundleWiring wiring = bundle.adapt(BundleWiring.class);
			if (wiring != null) {
				// all dependency wires (i.e. required bundles and import packages)
				List<BundleWire> requiredBundles = wiring
						.getRequiredWires(BundleNamespace.BUNDLE_NAMESPACE);
				List<BundleWire> wires = new ArrayList<BundleWire>(requiredBundles);
				wires.addAll(wiring.getRequiredWires(PackageNamespace.PACKAGE_NAMESPACE));

				resolveReExports(requiredBundles, wires);

				String providerSymName;

				for (BundleWire requiredBundleWire : wires) {
					// check if the provider is relevant (which means does the provider also
					// contributes a model extension)
					if (relevantBundleNames.contains(providerSymName = requiredBundleWire
							.getProvider().getSymbolicName())) {
						bundleDep.add(providerSymName);
					}
				}
			}
		}

		return bundleDep; // the relevant dependencies
	}

	/**
	 * Resolves the Re-Export entries in the list of given required bundle wires.
	 * 
	 * @param wiresToCheck
	 *            a list of required bundle wires
	 * @param wires
	 *            the list to enrich with the Re-Export entries
	 */
	private static void resolveReExports(List<BundleWire> wiresToCheck, List<BundleWire> wires) {
		Set<BundleRevision> alreadyChecked = new HashSet<BundleRevision>();

		// use stack to remove recursive call
		Queue<BundleRevision> toCheck = new LinkedList<BundleRevision>();

		// prepare stack
		for (BundleWire wireToCheck : wiresToCheck) {
			toCheck.offer(wireToCheck.getProvider());
		}

		BundleRevision bundleRev;
		while (!toCheck.isEmpty()) {
			bundleRev = toCheck.poll();

			// if add returns true this means that the element wasn't in the set so far
			if (alreadyChecked.add(bundleRev)) {
				for (BundleWire requiredBundle : bundleRev.getWiring().getRequiredWires(
						BundleNamespace.BUNDLE_NAMESPACE)) {
					if (requiredBundle.getRequirement().getDirectives()
							.get(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE) == BundleNamespace.VISIBILITY_REEXPORT) {
						wires.add(requiredBundle); // add the wire because it's a Re-exported one

						// put on stack to check its Re-Exports too
						toCheck.offer(requiredBundle.getProvider());
					}
				}
			}
		}
	}

	/**
	 * Holder/Helper class which holds the list of extensions per bundle and also the dependencies
	 * to other relevant bundles.
	 * 
	 * <p>
	 * Relevant bundles will be bundles which also provide a model contribution. The
	 * {@link #compareTo(TopoSortValue)} method will order elements with less relevant dependencies
	 * after elements with more relevant dependencies.
	 * </p>
	 */
	private static class TopoSortValue implements Comparable<TopoSortValue> {
		/** List of extensions provided by a bundle. */
		private final List<IExtension> extensions = new ArrayList<IExtension>();

		/** List of required bundle dependencies. */
		private final Set<String> requiredBundles = new HashSet<String>();

		/** Name of the bundle which provides the extensions. */
		private final String symbolicNameOfProvider;

		/**
		 * Sole Constructor.
		 * 
		 * @param symbolicNameOfProvider
		 *            name of the bundle which provides an extension
		 * @throws IllegalArgumentException
		 *             if the given symbolicNameOfProvider is <code>null</code>
		 */
		public TopoSortValue(String symbolicNameOfProvider) {
			if (symbolicNameOfProvider == null)
				throw new IllegalArgumentException("No symbolic name of providing bundle given!"); //$NON-NLS-1$

			this.symbolicNameOfProvider = symbolicNameOfProvider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(TopoSortValue o) {
			return o.requiredBundles.size() - requiredBundles.size();
		}

		/**
		 * @return the symbolic name of the bundle which provides extensions
		 */
		public String getSymbolicNameOfProvider() {
			return symbolicNameOfProvider;
		}

		/**
		 * Checks if there are unresolved dependencies.
		 * 
		 * @return true if the extensions in this object have dependencies which aren't resolved so
		 *         far; false otherwise
		 */
		public boolean hasUnresolvedDependencies() {
			return !requiredBundles.isEmpty();
		}

		/**
		 * Mark the dependency with the given symbolic name as resolved.
		 * 
		 * @param symbolicNameOfDependency
		 *            the name of the dependency which has been solved
		 */
		public void resolvedDependency(String symbolicNameOfDependency) {
			requiredBundles.remove(symbolicNameOfDependency);
		}

		/**
		 * Add a list of dependencies which have to be resolved before the extensions in this object
		 * can be resolved.
		 * 
		 * @param dependencies
		 *            a list of dependencies which need to be resolved before
		 */
		public void addDependencies(Set<String> dependencies) {
			requiredBundles.addAll(dependencies);
		}

		/**
		 * A list of {@link IExtension}s to which new {@link IExtension}s can be added.
		 * 
		 * @return the extension a list of {@link IExtension}s per bundle, never {@code null}
		 */
		public List<IExtension> getExtensions() {
			return extensions;
		}

		/**
		 * @return a string representation of all required bundles (for debug or logging)
		 */
		public String requiredBundlesToStirng() {
			return requiredBundles.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TSV - " + symbolicNameOfProvider + " --> " + requiredBundles + " (extensions: " + extensions.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	// FIXME Should we not reuse ModelUtils???
	private static MApplicationElement findElementById(MApplicationElement element, String id) {
		if (id == null || id.length() == 0)
			return null;
		// is it me?
		if (id.equals(element.getElementId()))
			return element;
		// Recurse if this is a container
		EList<EObject> elements = ((EObject) element).eContents();
		for (EObject childElement : elements) {
			if (!(childElement instanceof MApplicationElement))
				continue;
			MApplicationElement result = findElementById((MApplicationElement) childElement, id);
			if (result != null)
				return result;
		}
		return null;
	}
}
