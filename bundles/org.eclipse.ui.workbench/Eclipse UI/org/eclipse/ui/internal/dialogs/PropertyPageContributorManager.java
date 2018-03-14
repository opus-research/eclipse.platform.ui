/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 219273 
 *     James Blackburn (Broadcom Corp.) - Bug 294628 multiple selection
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.IObjectContributor;
import org.eclipse.ui.internal.ObjectContributorManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;

/**
 * Extends generic object contributor manager by loading property page
 * contributors from the registry.
 */

public class PropertyPageContributorManager extends ObjectContributorManager {
	private static PropertyPageContributorManager sharedInstance = null;

	private class CategorizedPageNode {
		RegistryPageContributor contributor;

		CategorizedPageNode parent;

		CategorizedPageNode(RegistryPageContributor page) {
			contributor = page;
		}

		void setParent(CategorizedPageNode node) {
			parent = node;
		}

	}

	/**
	 * The constructor.
	 */
	public PropertyPageContributorManager() {
		super();
		// load contributions on startup so that getContributors() returns the
		// proper content
		loadContributors();
	}

	/**
	 * Given the object class, this method will find all the registered matching
	 * contributors and sequentially invoke them to contribute to the property
	 * page manager. Matching algorithm will also check subclasses and
	 * implemented interfaces.
	 * 
	 * If object is an IStructuredSelection then attempt to match all the
	 * contained objects.
	 * 
	 * @param manager
	 * @param object
	 * @return true if contribution took place, false otherwise.
	 */
	public boolean contribute(PropertyPageManager manager, Object object) {

		Collection<IObjectContributor> result = null;
		if (object instanceof IStructuredSelection) {
			Object[] objs = ((IStructuredSelection) object).toArray();
			for (int i = 0; i < objs.length; i++) {
				List<IObjectContributor> contribs = getContributors(objs[i]);
				if (result == null)
					result = new LinkedHashSet<IObjectContributor>(contribs);
				else
					result.retainAll(contribs);
			}
		} else
			result = getContributors(object);

		if (result == null || result.size() == 0) {
			return false;
		}

		// Build the category nodes
		List<CategorizedPageNode> catNodes = buildNodeList(result);
		Iterator<CategorizedPageNode> resultIterator = catNodes.iterator();

		// K(CategorizedPageNode) V(PreferenceNode - property page)
		Map<CategorizedPageNode, PreferenceNode> catPageNodeToPages = new HashMap<CategorizedPageNode, PreferenceNode>();

		// Allow each contributor to add its page to the manager.
		boolean actualContributions = false;
		while (resultIterator.hasNext()) {
			CategorizedPageNode next = resultIterator.next();
			IPropertyPageContributor ppcont = next.contributor;
			if (!ppcont.isApplicableTo(object)) {
				continue;
			}
			PreferenceNode page = ppcont.contributePropertyPage(manager, object);
			if (page != null) {
				catPageNodeToPages.put(next, page);
				actualContributions = true;
			}
		}

		// Fixup the parents in each page
		if (actualContributions) {
			resultIterator = catNodes.iterator();
			while (resultIterator.hasNext()) {
				CategorizedPageNode next = resultIterator.next();
				PreferenceNode child = catPageNodeToPages.get(next);
				if (child == null)
					continue;
				PreferenceNode parent = null;
				if (next.parent != null)
					parent = catPageNodeToPages.get(next.parent);
				
				if (parent == null) {
					manager.addToRoot(child);
				} else {
					parent.add(child);
				}
			}
		}
		return actualContributions;
	}

	/**
	 * Build the list of nodes to be sorted.
	 * @param nodes
	 * @return List of CategorizedPageNode
	 */
	private List<CategorizedPageNode> buildNodeList(Collection<IObjectContributor> nodes) {
		Hashtable<String, CategorizedPageNode> mapping = new Hashtable<String, CategorizedPageNode>();
		
		Iterator<IObjectContributor> nodesIterator = nodes.iterator();
		while(nodesIterator.hasNext()){
			RegistryPageContributor page = (RegistryPageContributor) nodesIterator.next();
			mapping.put(page.getPageId(),new CategorizedPageNode(page));
		}
		
		Iterator<CategorizedPageNode> values = mapping.values().iterator();
		List<CategorizedPageNode> returnValue = new ArrayList<CategorizedPageNode>();
		while(values.hasNext()){
			CategorizedPageNode next = values.next();
			returnValue.add(next);
			if(next.contributor.getCategory() == null) {
				continue;
			}
			Object parent = mapping.get(next.contributor.getCategory());
			if(parent != null) {
				next.setParent((CategorizedPageNode) parent);
			}
		}
		return returnValue;
	}

	/**
	 * Ideally, shared instance should not be used and manager should be located
	 * in the workbench class.
	 * @return PropertyPageContributorManager
	 */
	public static PropertyPageContributorManager getManager() {
		if (sharedInstance == null) {
			sharedInstance = new PropertyPageContributorManager();
		}
		return sharedInstance;
	}

	/**
	 * Loads property page contributors from the registry.
	 */
	private void loadContributors() {
		PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(
				this);
		reader.registerPropertyPages(Platform.getExtensionRegistry());
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
     */
    public void addExtension(IExtensionTracker tracker, IExtension extension) {
        IConfigurationElement[] addedElements = extension.getConfigurationElements();
        for (int i = 0; i < addedElements.length; i++) {
            PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(this);
            reader.readElement(addedElements[i]);
        }
    }

	/**
	 * Return the contributors for element filters on the enablement. 
	 * @param element
	 * @return Collection of PropertyPageContribution
	 */
	public Collection<RegistryPageContributor> getApplicableContributors(Object element) {
		if (element instanceof IStructuredSelection)
			return getApplicableContributors((IStructuredSelection) element);
		Collection<IObjectContributor> contributors = getContributors(element);
		Collection<RegistryPageContributor> result = new ArrayList<RegistryPageContributor>();
		for (Iterator<IObjectContributor> iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor contributor = (RegistryPageContributor) iter.next();
			if(contributor.isApplicableTo(element))
				result.add(contributor);
			
		}
		return result;
	}

	/**
	 * Get applicable contributors for multiple selection
	 * 
	 * @param selection
	 * @return Collection of applicable property page contributors
	 * @since 3.7
	 */
	public Collection<RegistryPageContributor> getApplicableContributors(
			IStructuredSelection selection) {
		Iterator<?> it = selection.iterator();
		Collection<RegistryPageContributor> result = null;
		while (it.hasNext()) {
			Object element = it.next();
			Collection<RegistryPageContributor> collection = getApplicableContributors(element);
			if (result == null)
				result = new LinkedHashSet<RegistryPageContributor>(collection);
			else
				result.retainAll(collection);
		}
		if (result != null && !result.isEmpty() && selection.size() > 1) {
			// only add contributors which can handle multi selection
			it = result.iterator();
			while (it.hasNext()) {
				RegistryPageContributor contrib = (RegistryPageContributor) it
						.next();
				if (!contrib.supportsMultipleSelection())
					it.remove();
			}
		}
		return result;
	}

	@Override
	protected String getExtensionPointFilter() {
		return IWorkbenchRegistryConstants.PL_PROPERTY_PAGES;
	}
}
