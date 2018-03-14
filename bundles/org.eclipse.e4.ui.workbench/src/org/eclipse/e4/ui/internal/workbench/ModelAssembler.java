/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430075, 430080
 *     René Brandstetter - Bug 419749 - [Workbench] [e4 Workbench] - Remove the deprecated PackageAdmin
 *     Wim Jongman - Bug 376486 - IDE not extendable via fragments or processors
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
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
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
 */
public class ModelAssembler {

	/**
	 * The name of the extension point attribute.
	 */
	private static final String POST_MODEL_CREATION = "postmodelcreation"; //$NON-NLS-1$

	/**
	 * Indicates that the legacy workbench model is not yet created.
	 */
	public static final int DURING_MODEL_CREATION = 0;

	/**
	 * Indicates that the workbench model has been created.
	 */
	public static final int AFTER_MODEL_CREATION = 1;

	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	@Inject
	private IExtensionRegistry registry;

	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$

	/**
	 * Process the model based on the step field which can be {@link #PURE_E4},
	 * {@link #LEGACY_E4STEP} or {@link #LEGACY_E3STEP}.
	 *
	 * @param step
	 * @see #PURE_E4
	 * @see #LEGACY_E4STEP
	 * @see #LEGACY_E3STEP
	 */
	public void processModel(int step) {

		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = topoSort(extPoint.getExtensions());

		List<MApplicationElement> imports = new ArrayList<MApplicationElement>();
		List<MApplicationElement> addedElements = new ArrayList<MApplicationElement>();

		// run processors which are marked to run before fragments
		runProcessors(extensions, false, step);
		processFragments(extensions, imports, addedElements, step);
		// run processors which are marked to run after fragments
		runProcessors(extensions, true, step);

		resolveImports(imports, addedElements);
	}

	/**
	 * @param extensions
	 * @param imports
	 * @param addedElements
	 * @param step
	 */
	private void processFragments(IExtension[] extensions, List<MApplicationElement> imports,
			List<MApplicationElement> addedElements, int step) {

		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if ("fragment".equals(ce.getName())) { //$NON-NLS-1$
					if (isCorrectPhase(step, ce)) {
						processFragment(ce, imports, addedElements);
					}
				}
			}
		}
	}

	private void processFragment(IConfigurationElement ce, List<MApplicationElement> imports,
			List<MApplicationElement> addedElements) {
		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();
		ResourceSet resourceSet = applicationResource.getResourceSet();
		IContributor contributor = ce.getContributor();
		String attrURI = ce.getAttribute("uri"); //$NON-NLS-1$
		if (attrURI == null) {
			logger.warn("Unable to find location for the model extension \"{0}\"", //$NON-NLS-1$
					contributor.getName());
			return;
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
			return;
		}

		String contributorURI = URIHelper.constructPlatformURI(contributor);
		Resource resource;
		try {
			resource = resourceSet.getResource(uri, true);
		} catch (RuntimeException e) {
			logger.warn(e, "Unable to read model extension from " + uri.toString()); //$NON-NLS-1$
			return;
		}

		EList<?> contents = resource.getContents();
		if (contents.isEmpty()) {
			return;
		}

		Object extensionRoot = contents.get(0);

		if (!(extensionRoot instanceof MModelFragments)) {
			logger.warn("Unable to create model extension \"{0}\"", //$NON-NLS-1$
					contributor.getName());
			return;
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

	/**
	 * @param extensions
	 * @param afterFragments
	 */
	private void runProcessors(IExtension[] extensions, Boolean afterFragments, int step) {
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				boolean parseBoolean = Boolean.parseBoolean(ce.getAttribute("beforefragment")); //$NON-NLS-1$
				if ("processor".equals(ce.getName()) && !afterFragments.equals(parseBoolean)) { //$NON-NLS-1$
					if (isCorrectPhase(step, ce)) {
						runProcessor(ce);
					}
				}
			}
		}
	}

	/**
	 * Check if this configuration element must be run now based on the
	 * <code>postmodelcreation</code> attribute which can be {@link #DURING_MODEL_CREATION} or
	 * {@link #AFTER_MODEL_CREATION}.
	 *
	 * @param step
	 * @param ce
	 * @return true if the element must be processed now.
	 */
	private boolean isCorrectPhase(int step, IConfigurationElement ce) {

		// Running before the workbench model is created
		if (step == DURING_MODEL_CREATION) {
			if (ce.getAttribute(POST_MODEL_CREATION) == null) {
				return true;
			}
			return Boolean.FALSE.toString().equals(ce.getAttribute(POST_MODEL_CREATION));
		}

		// Running after the workbench model has been created
		else if (step == AFTER_MODEL_CREATION) {
			if (ce.getAttribute(POST_MODEL_CREATION) == null) {
				return false;
			}
			return Boolean.TRUE.toString().equals(ce.getAttribute(POST_MODEL_CREATION));
		}

		return false;
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
								@SuppressWarnings("unchecked")
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
		if (extensions.length == 0) {
			return extensions;
		}

		final Map<String, Collection<IExtension>> mappedExtensions = new HashMap<String, Collection<IExtension>>();
		// Captures the bundles that are listed as requirements for a particular bundle.
		final Map<String, Collection<String>> requires = new HashMap<String, Collection<String>>();
		// Captures the bundles that list a particular bundle as a requirement
		final Map<String, Collection<String>> depends = new HashMap<String, Collection<String>>();

		// {@code requires} and {@code depends} define a graph where the vertices are
		// bundleIds and the edges are the requires-relation. {@code requires} defines
		// the out-edges for a vertex, and {@code depends} defines the in-edges for a vertex.
		//
		// Description of the algorithm:
		// (1) build up the graph: we only record the bundles actually being considered
		// (i.e., those that are contributors of {@code extensions})
		// (2) sort the list of bundles by their out-degree: the bundles with the least
		// out-edges are those that are depend on the fewest. If there is no bundles
		// with 0 out-edges, then we must have a cycle; oh well, can't win them all.
		// (3) take the bundle with lowest out-degree and add its extensions to the list.
		// Remove the bundle from the list, and remove it from all of its dependents'
		// required lists. This may require that the bundle list be resorted.
		//
		// Note this implementation assumes direct dependencies: if any of the bundles
		// are dependent through a third bundle, then the ordering will fail. To prevent
		// this would require recording the entire dependency subgraph for all contributors
		// of the {@code extensions}.

		// first build up the list of bundles actually being considered
		for (IExtension extension : extensions) {
			IContributor contributor = extension.getContributor();
			Collection<IExtension> exts = mappedExtensions.get(contributor.getName());
			if (exts == null) {
				mappedExtensions.put(contributor.getName(), exts = new ArrayList<IExtension>());
			}
			exts.add(extension);
			requires.put(contributor.getName(), new HashSet<String>());
			depends.put(contributor.getName(), new HashSet<String>());
		}

		// now populate the dependency graph
		for (String bundleId : mappedExtensions.keySet()) {
			assert requires.containsKey(bundleId) && depends.containsKey(bundleId);

			// can only be one, because ExtensionPoints require the singleton setting
			// on a bundle
			Bundle requiredBundle = Activator.getDefault().getBundleForName(bundleId);
			if (requiredBundle != null) {
				assert requiredBundle.getSymbolicName().equals(bundleId);
				for (Bundle dependentBundle : resolveRequiringBundle(requiredBundle)) {
					if (!mappedExtensions.containsKey(dependentBundle.getSymbolicName())) {
						// not a contributor of an extension
						continue;
					}
					String depBundleId = dependentBundle.getSymbolicName();
					Collection<String> depBundleReqs = requires.get(depBundleId);
					depBundleReqs.add(bundleId);
					Collection<String> bundleDeps = depends.get(bundleId);
					assert bundleDeps != null;
					bundleDeps.add(depBundleId);
				}
			}
		}

		int resultIndex = 0;

		// sort by out-degree ({@code depends})
		// I suppose we could make {@code depends} a SortedMap, but we'd still need
		// to explicitly resort anyways
		List<String> sortedByOutdegree = new ArrayList<String>(requires.keySet());
		Comparator<String> outdegreeSorter = new Comparator<String>() {
			public int compare(String o1, String o2) {
				assert requires.containsKey(o1) && requires.containsKey(o2);
				return requires.get(o1).size() - requires.get(o2).size();
			}
		};
		Collections.sort(sortedByOutdegree, outdegreeSorter);
		if (!requires.get(sortedByOutdegree.get(0)).isEmpty()) {
			logger.warn("Extensions have a cycle"); //$NON-NLS-1$
		}

		while (!sortedByOutdegree.isEmpty()) {
			// don't sort unnecessarily: the current ordering is fine providing
			// item #0 still has no dependencies
			if (!requires.get(sortedByOutdegree.get(0)).isEmpty()) {
				Collections.sort(sortedByOutdegree, outdegreeSorter);
			}
			String bundleId = sortedByOutdegree.remove(0);
			assert depends.containsKey(bundleId) && requires.containsKey(bundleId);
			for (IExtension ext : mappedExtensions.get(bundleId)) {
				extensions[resultIndex++] = ext;
			}
			assert requires.get(bundleId).isEmpty();
			requires.remove(bundleId);
			for (String depId : depends.get(bundleId)) {
				requires.get(depId).remove(bundleId);
			}
			depends.remove(bundleId);
		}
		assert resultIndex == extensions.length;
		return extensions;
	}

	/**
	 * Returns the bundles that currently require the given bundle.
	 * <p>
	 * If this required bundle is required and then re-exported by another bundle then all the
	 * requiring bundles of the re-exporting bundle are included in the returned array.
	 * </p>
	 * 
	 * @return An unmodifiable {@link Iterable} set of bundles currently requiring this required
	 *         bundle. An empty {@link Iterable} will be returned if the given {@code Bundle} object
	 *         has become stale or no bundles require the given bundle.
	 * @throws NullPointerException
	 *             if the given bundle is <code>null</code>
	 */
	private static Iterable<Bundle> resolveRequiringBundle(Bundle bundle) {
		BundleWiring providerWiring = bundle.adapt(BundleWiring.class);
		if (!providerWiring.isInUse()) {
			return Collections.emptySet();
		}

		Set<Bundle> requiring = new HashSet<Bundle>();

		addRequirers(requiring, providerWiring);
		return Collections.unmodifiableSet(requiring);
	}

	/**
	 * Recursively collects all bundles which depend-on/require the given {@link BundleWiring}.
	 * <p>
	 * All re-exports will be followed and also be contained in the result.
	 * </p>
	 * 
	 * @param requiring
	 *            the result which will contain all the bundles which require the given
	 *            {@link BundleWiring}
	 * @param providerWiring
	 *            the {@link BundleWiring} for which the requirers should be resolved
	 * @throws NullPointerException
	 *             if either the requiring or the providerWiring is <code>null</code>
	 */
	private static void addRequirers(Set<Bundle> requiring, BundleWiring providerWiring) {
		List<BundleWire> requirerWires = providerWiring
				.getProvidedWires(BundleNamespace.BUNDLE_NAMESPACE);
		if (requirerWires == null) {
			// we don't hold locks while checking the graph, just return if no longer isInUse
			return;
		}
		for (BundleWire requireBundleWire : requirerWires) {
			Bundle requirer = requireBundleWire.getRequirer().getBundle();
			if (requiring.contains(requirer)) {
				continue;
			}
			requiring.add(requirer);
			String reExport = requireBundleWire.getRequirement().getDirectives()
					.get(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE);
			if (BundleNamespace.VISIBILITY_REEXPORT.equals(reExport)) {
				addRequirers(requiring, requireBundleWire.getRequirerWiring());
			}
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
