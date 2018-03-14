/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.Util;

/**
 * This class is a default implementation of <code>IObjectContributorManager</code>.
 * It provides fast merging of contributions with the following semantics:
 * <ul>
 * <li> All of the matching contributors will be invoked per property lookup
 * <li> The search order from a class with the definition<br>
 *			<code>class X extends Y implements A, B</code><br>
 *		is as follows:
 * 		<il>
 *			<li>the target's class: X
 *			<li>X's superclasses in order to <code>Object</code>
 *			<li>a depth-first traversal of the target class's interaces in the order 
 *				returned by <code>getInterfaces()</code> (in the example, A and 
 *				its superinterfaces then B and its superinterfaces)
 *		</il>
 * </ul>
 *
 * @see IObjectContributor
 */
public abstract class ObjectContributorManager implements IExtensionChangeHandler {
	
	/** 
	 * @since 3.1
	 */
	private class ContributorRecord {
		/**
		 * @param contributor
		 * @param targetType
		 */
		public ContributorRecord(IObjectContributor contributor, String targetType) {
			this.contributor = contributor;
			this.objectClassName = targetType;
		}
		
		String objectClassName;
		IObjectContributor contributor;
	}

    /** Table of contributors. */
	protected Map<String, List<IObjectContributor>> contributors;

    /** Cache of object class contributor search paths; <code>null</code> if none. */
	protected Map<Class<?>, List<IObjectContributor>> objectLookup;

    /** Cache of resource adapter class contributor search paths; <code>null</code> if none. */
	protected Map<Class<?>, List<IObjectContributor>> resourceAdapterLookup;
    
    /** Cache of adaptable class contributor search paths; <code>null</code> if none. */
	protected Map<String, List<IObjectContributor>> adaptableLookup;
    
	protected Set<ContributorRecord> contributorRecordSet;

    /** 
     * Constructs a new contributor manager.
     */
    public ObjectContributorManager() {
		contributors = new Hashtable<String, List<IObjectContributor>>(5);
		contributorRecordSet = new HashSet<ContributorRecord>(5);
        objectLookup = null;
        resourceAdapterLookup = null;
        adaptableLookup = null;
        String extensionPointId = getExtensionPointFilter();
        if (extensionPointId != null) {
        	IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
    				PlatformUI.PLUGIN_ID, extensionPointId);
			IExtensionTracker tracker = PlatformUI.getWorkbench()
					.getExtensionTracker();
			tracker.registerHandler(this, ExtensionTracker
					.createExtensionPointFilter(extensionPoint));
		}
    }

    /**
	 * Return the extension point id (local to org.eclipse.ui) that this manager
	 * is associated with. Default implementation returns null, which implies no
	 * relationship with a particular extension.
	 * 
	 * @return the extension point id
	 * @since 3.4
	 */
	protected String getExtensionPointFilter() {
		return null;
	}

	/**
     * Adds contributors for the given types to the result list.
     */
	private void addContributorsFor(List<Class<?>> types, List<IObjectContributor> result) {
		for (Iterator<Class<?>> classes = types.iterator(); classes.hasNext();) {
			Class<?> clazz = classes.next();
			List<IObjectContributor> contributorList = contributors.get(clazz.getName());
            if (contributorList != null) {
				result.addAll(contributorList);
			}
        }
    }

    /**
     * Returns the class search order starting with <code>extensibleClass</code>.
     * The search order is defined in this class' comment.
     */
	protected final List<Class<?>> computeClassOrder(Class<?> extensibleClass) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>(4);
		Class<?> clazz = extensibleClass;
        while (clazz != null) {
            result.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    /**
     * Returns the interface search order for the class hierarchy described
     * by <code>classList</code>.
     * The search order is defined in this class' comment.
     */
	protected final List<Class<?>> computeInterfaceOrder(List<Class<?>> classList) {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>(4);

		for (Iterator<Class<?>> list = classList.iterator(); list.hasNext();) {
			Class<?>[] interfaces = list.next().getInterfaces();
			internalComputeInterfaceOrder(interfaces, result, new HashMap<Class<?>, Class<?>>(4));
        }
        return result;
    }

    /**
     * Flushes the cache of contributor search paths.  This is generally required
     * whenever a contributor is added or removed.  
     * <p>
     * It is likely easier to just toss the whole cache rather than trying to be
     * smart and remove only those entries affected.
     */
    public void flushLookup() {
        objectLookup = null;
        resourceAdapterLookup = null;
        adaptableLookup = null;
    }

    /**
     * Cache the real adapter class contributor search path.
     */
	private void cacheResourceAdapterLookup(Class<?> adapterClass, List<IObjectContributor> results) {
        if (resourceAdapterLookup == null) {
			resourceAdapterLookup = new HashMap<Class<?>, List<IObjectContributor>>();
		}
        resourceAdapterLookup.put(adapterClass, results);
    }
    
    /**
     * Cache the real adapter class contributor search path.
     */
	private void cacheAdaptableLookup(String adapterClass, List<IObjectContributor> results) {
        if (adaptableLookup == null) {
			adaptableLookup = new HashMap<String, List<IObjectContributor>>();
		}
        adaptableLookup.put(adapterClass, results);
    }

    /**
     * Cache the object class contributor search path.
     */
	private void cacheObjectLookup(Class<?> objectClass, List<IObjectContributor> results) {
        if (objectLookup == null) {
			objectLookup = new HashMap<Class<?>, List<IObjectContributor>>();
		}
        objectLookup.put(objectClass, results);
    }

    /**
     * Get the contributions registered to this manager.
     * 
     * @return an unmodifiable <code>Collection</code> containing all registered
     * contributions.  The objects in this <code>Collection</code> will be 
     * <code>List</code>s containing the actual contributions.
     * @since 3.0
     */
	public Collection<List<IObjectContributor>> getContributors() {
        return Collections.unmodifiableCollection(contributors.values());
    }

    /**
     * Return the list of contributors for the supplied class.
     */
	protected List<IObjectContributor> addContributorsFor(Class<?> objectClass) {

		List<Class<?>> classList = computeClassOrder(objectClass);
		List<IObjectContributor> result = new ArrayList<IObjectContributor>();
        addContributorsFor(classList, result);
        classList = computeInterfaceOrder(classList); // interfaces
        addContributorsFor(classList, result);
        return result;
    }

    /**
     * Returns true if contributors exist in the manager for
     * this object and any of it's super classes, interfaces, or
     * adapters.
     * 
     * @param object the object to test
     * @return whether the object has contributors
     */
    public boolean hasContributorsFor(Object object) {

		List<IObjectContributor> contributors = getContributors(object);
        return contributors.size() > 0;
    }

    /**
     * Add interface Class objects to the result list based
     * on the class hierarchy. Interfaces will be searched
     * based on their position in the result list.
     */
	private void internalComputeInterfaceOrder(Class<?>[] interfaces, List<Class<?>> result,
			Map<Class<?>, Class<?>> seen) {
		List<Class<?>> newInterfaces = new ArrayList<Class<?>>(seen.size());
        for (int i = 0; i < interfaces.length; i++) {
			Class<?> interfac = interfaces[i];
            if (seen.get(interfac) == null) {
                result.add(interfac);
                seen.put(interfac, interfac);
                newInterfaces.add(interfac);
            }
        }
		for (Iterator<Class<?>> newList = newInterfaces.iterator(); newList.hasNext();) {
			internalComputeInterfaceOrder(newList.next().getInterfaces(), result, seen);
		}
    }

    /**
	 * Return whether the given contributor is applicable to all elements in the
	 * selection.
	 * 
	 * @param selection
	 *            the selection
	 * @param contributor
	 *            the contributor
	 * @return whether it is applicable
	 */
    public boolean isApplicableTo(IStructuredSelection selection,
            IObjectContributor contributor) {
		Iterator<?> elements = selection.iterator();
        while (elements.hasNext()) {
            if (contributor.isApplicableTo(elements.next()) == false) {
				return false;
			}
        }
        return true;
    }

    /**
	 * Return whether the given contributor is applicable to all elements in the
	 * list.
	 * 
	 * @param list
	 *            the selection
	 * @param contributor
	 *            the contributor
	 * @return whether it is applicable
	 */

	public boolean isApplicableTo(List<Object> list, IObjectContributor contributor) {
		Iterator<Object> elements = list.iterator();
        while (elements.hasNext()) {
            if (contributor.isApplicableTo(elements.next()) == false) {
				return false;
			}
        }
        return true;
    }

    /**
     * Register a contributor.
     * 
     * @param contributor the contributor
     * @param targetType the target type
     */
    public void registerContributor(IObjectContributor contributor,
            String targetType) {
		List<IObjectContributor> contributorList = contributors.get(targetType);
        if (contributorList == null) {
			contributorList = new ArrayList<IObjectContributor>(5);
            contributors.put(targetType, contributorList);
        }
        contributorList.add(contributor);
        flushLookup();

        IConfigurationElement element = (IConfigurationElement) Util.getAdapter(contributor,
        	IConfigurationElement.class);
        
        //hook the object listener
        if (element != null) {
			ContributorRecord contributorRecord = new ContributorRecord(
					contributor, targetType);
			contributorRecordSet.add(contributorRecord);
			PlatformUI.getWorkbench().getExtensionTracker().registerObject(
					element.getDeclaringExtension(), contributorRecord,
					IExtensionTracker.REF_WEAK);
        }
    }

    /**
     * Unregister all contributors.
     */
    public void unregisterAllContributors() {
		contributors = new Hashtable<String, List<IObjectContributor>>(5);
        flushLookup();
    }

    /**
     * Unregister a contributor from the target type.
     * 
     * @param contributor the contributor
     * @param targetType the target type
     */
    public void unregisterContributor(IObjectContributor contributor,
            String targetType) {    	
		List<IObjectContributor> contributorList = contributors.get(targetType);
        if (contributorList == null) {
			return;
		}
        contributorList.remove(contributor);
        if (contributorList.isEmpty()) {
			contributors.remove(targetType);
		}
        flushLookup();
    }


    /**
     * Unregister all contributors for the target type.
     * 
     * @param targetType the target type
     */
    public void unregisterContributors(String targetType) {
        contributors.remove(targetType);
        flushLookup();
    }
    
	protected List<IObjectContributor> getContributors(Object object) {
    	// Determine is the object is a resource
    	Object resource  = LegacyResourceSupport.getAdaptedContributorResource(object);	
    	
    	// Fetch the unique adapters
		List<String> adapters = new ArrayList<String>(Arrays.asList(Platform.getAdapterManager()
				.computeAdapterTypes(object.getClass())));
		removeCommonAdapters(adapters, Arrays.asList(new Class<?>[] { object.getClass() }));

		List<IObjectContributor> contributors = new ArrayList<IObjectContributor>();
        
        // Calculate the contributors for this object class
        addAll(contributors, getObjectContributors(object.getClass()));
        // Calculate the contributors for resource classes
        if(resource != null) {
			addAll(contributors, getResourceContributors(resource.getClass()));
		}
        // Calculate the contributors for each adapter type
    	if(adapters != null) {
			for (Iterator<String> it = adapters.iterator(); it.hasNext();) {
				String adapter = it.next();
				addAll(contributors, getAdaptableContributors(adapter));
			}
    	}
    	
        // Remove duplicates.  Note: this -must- maintain the element order to preserve menu order.
        contributors = removeDups(contributors);
		return contributors.isEmpty() ? Collections.emptyList() : new ArrayList(contributors);
    }
    
    /**
     * Returns the contributions for the given class. This considers
     * contributors on any super classes and interfaces.
     * 
     * @param objectClass the class to search for contributions.
     * @return the contributions for the given class. This considers
     * contributors on any super classes and interfaces.
     * 
     * @since 3.1
     */
	protected List<IObjectContributor> getObjectContributors(Class<?> objectClass) {
		List<IObjectContributor> objectList = null;
		// Lookup the results in the cache first.
		if (objectLookup != null) {
			objectList = objectLookup.get(objectClass);
		}
		if (objectList == null) {
			objectList = addContributorsFor(objectClass);
			if (objectList.size() == 0) {
				objectList = Collections.emptyList();
			}
			else {
				objectList = Collections.unmodifiableList(objectList);
			}
			cacheObjectLookup(objectClass, objectList);
		}
		return objectList;
	}

    /**
     * Returns the contributions for the given <code>IResource</code>class. 
     * This considers contributors on any super classes and interfaces. This
     * will only return contributions that are adaptable.
     * 
     * @param resourceClass the class to search for contributions.
     * @return the contributions for the given class. This considers
     * adaptable contributors on any super classes and interfaces.
     * 
     * @since 3.1
     */
	protected List<IObjectContributor> getResourceContributors(Class<?> resourceClass) {
		List<IObjectContributor> resourceList = null;
		if (resourceAdapterLookup != null) {
			resourceList = resourceAdapterLookup.get(resourceClass);
		}
		if (resourceList == null) {
			resourceList = addContributorsFor(resourceClass);
			if (resourceList.size() == 0) {
				resourceList = Collections.emptyList();
			} else {
				resourceList = Collections.unmodifiableList(filterOnlyAdaptableContributors(resourceList));
			}
			cacheResourceAdapterLookup(resourceClass, resourceList);
		}
		return resourceList;
	}

    /**
     * Returns the contributions for the given type name. 
     * 
     * @param adapterType the class to search for contributions.
     * @return the contributions for the given class. This considers
     * contributors to this specific type.
     * 
     * @since 3.1
     */
	protected List<IObjectContributor> getAdaptableContributors(String adapterType) {
		List<IObjectContributor> adaptableList = null;
		// Lookup the results in the cache first, there are two caches
		// one that stores non-adapter contributions and the other
		// contains adapter contributions.
		if (adaptableLookup != null) {
			adaptableList = adaptableLookup.get(adapterType);
		}
		if (adaptableList == null) {
			// ignore resource adapters because these must be adapted via the
			// IContributorResourceAdapter.
			if (LegacyResourceSupport.isResourceType(adapterType) || LegacyResourceSupport.isResourceMappingType(adapterType)) {
				adaptableList = Collections.emptyList();
			}
			else {
				adaptableList = contributors.get(adapterType);
				if (adaptableList == null || adaptableList.size() == 0) {
					adaptableList = Collections.emptyList();
				} else {
					adaptableList = Collections.unmodifiableList(filterOnlyAdaptableContributors(adaptableList));
				}
			}
			cacheAdaptableLookup(adapterType, adaptableList);
		}
		return adaptableList;
	}
	
	/**
	 * Prunes from the list of adapters type names that are in the class
	 * search order of every class in <code>results</code>.  
	 * @param adapters
	 * @param results
	 * @since 3.1
	 */
	protected void removeCommonAdapters(List<String> adapters, List<Class<?>> results) {
		for (Iterator<Class<?>> it = results.iterator(); it.hasNext();) {
			Class<?> clazz = it.next();
			List<Class<?>> commonTypes = computeCombinedOrder(clazz);
			for (Iterator<Class<?>> it2 = commonTypes.iterator(); it2.hasNext();) {
				Class<?> type = it2.next();
				adapters.remove(type.getName());	
			}				
		}
    }
	
	/**
     * Returns the class search order starting with <code>extensibleClass</code>.
     * The search order is defined in this class' comment.
     */
	protected List<Class<?>> computeCombinedOrder(Class<?> inputClass) {
		List<Class<?>> result = new ArrayList<Class<?>>(4);
		Class<?> clazz = inputClass;
        while (clazz != null) {
            // add the class
            result.add(clazz);
            // add all the interfaces it implements
			Class<?>[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                result.add(interfaces[i]);
            }
            // get the superclass
            clazz = clazz.getSuperclass();
        }
        return result;
    }

	private List<IObjectContributor> filterOnlyAdaptableContributors(
			List<IObjectContributor> contributors) {
		List<IObjectContributor> adaptableContributors = null;
		for (Iterator<IObjectContributor> it = contributors.iterator(); it.hasNext();) {
			IObjectContributor c = it.next();
			if(c.canAdapt()) {
				if(adaptableContributors == null) {
					adaptableContributors = new ArrayList<IObjectContributor>();
				}
				adaptableContributors.add(c);
			}
		}
		return adaptableContributors == null ? Collections.EMPTY_LIST : adaptableContributors;
	}
	
	@Override
    public void removeExtension(IExtension source, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof ContributorRecord) {
                ContributorRecord contributorRecord = (ContributorRecord) objects[i];
                unregisterContributor((contributorRecord).contributor, (contributorRecord).objectClassName);
                contributorRecordSet.remove(contributorRecord);
            }
        }
    }

    /**
     * Remove listeners and dispose of this manager.
     * 
     * @since 3.1
     */
    public void dispose() {
    	if(getExtensionPointFilter() != null) {
			PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
		}
    }
    
    /**
     * Returns the list of contributors that are interested in the 
     * given list of model elements.
     * @param elements a list of model elements (<code>Object</code>)
     * @return the list of interested contributors (<code>IObjectContributor</code>)
     */
	protected List<IObjectContributor> getContributors(List<Object> elements) {
        // Calculate the common class, interfaces, and adapters registered
        // via the IAdapterManager.
		List<String> commonAdapters = new ArrayList<String>();
		List<Class<?>> commonClasses = getCommonClasses(elements, commonAdapters);
        
        // Get the resource class. It will be null if any of the
        // elements are resources themselves or do not adapt to
        // IResource.
		Class<?> resourceClass = getCommonResourceClass(elements);
		Class<?> resourceMappingClass = getResourceMappingClass(elements);

        // Get the contributors.   
        
		List<IObjectContributor> contributors = new ArrayList<IObjectContributor>();
        
        // Add the resource contributions to avoid duplication
        if (resourceClass != null) {
            addAll(contributors, getResourceContributors(resourceClass));
        }
        if (commonClasses != null && !commonClasses.isEmpty()) {
            for (int i = 0; i < commonClasses.size(); i++) {
				List<IObjectContributor> results = getObjectContributors(commonClasses.get(i));
                addAll(contributors, results);
            }
        }
        // Add the resource mappings explicitly to avoid possible duplication
        if (resourceMappingClass == null) {
            // Still show the menus if the object is not adaptable but the adapter manager
            // has an entry for it
            resourceMappingClass = LegacyResourceSupport
                    .getResourceMappingClass();
            if (resourceMappingClass != null
                    && commonAdapters.contains(resourceMappingClass.getName())) {
            	addAll(contributors, getResourceContributors(resourceMappingClass));
            }
        } else {
            contributors.addAll(getResourceContributors(resourceMappingClass));
        }
        if (!commonAdapters.isEmpty()) {
			for (Iterator<String> it = commonAdapters.iterator(); it.hasNext();) {
				String adapter = it.next();
                addAll(contributors, getAdaptableContributors(adapter));
            }
        }
    	
        // Remove duplicates.  Note: this -must- maintain the element order to preserve menu order.
        contributors = removeDups(contributors);
        
		return contributors.isEmpty() ? Collections.emptyList() : new ArrayList(contributors);
    }

    /**
	 * Adds all items in toAdd to the given collection.  Optimized to avoid creating an iterator.
	 * This assumes that toAdd is efficient to index (i.e. it's an ArrayList or some other RandomAccessList),
	 * which is the case for all uses in this class.
	 */
	private static void addAll(Collection<IObjectContributor> collection,
			List<IObjectContributor> toAdd) {
		for (int i = 0, size = toAdd.size(); i < size; ++i) {
			collection.add(toAdd.get(i));
		}
	}

    /**
     * Removes duplicates from the given list, preserving order.
     */
	private static List<IObjectContributor> removeDups(List<IObjectContributor> list) {
    	if (list.size() <= 1) {
    		return list;
    	}
		HashSet<IObjectContributor> set = new HashSet<IObjectContributor>(list);
    	if (set.size() == list.size()) {
    		return list;
    	}
		ArrayList<IObjectContributor> result = new ArrayList<IObjectContributor>(set.size());
		for (Iterator<IObjectContributor> i = list.iterator(); i.hasNext();) {
			IObjectContributor o = i.next();
    		if (set.remove(o)) {
    			result.add(o);
    		}
		}
    	return result;
    }
    
	/**
     * Returns the common denominator class, interfaces, and adapters 
     * for the given collection of objects.
     */
	private List<Class<?>> getCommonClasses(List<Object> objects, List<String> commonAdapters) {
        if (objects == null || objects.size() == 0) {
			return null;
		}

        // Optimization: if n==1 (or if all objects are of the same class), then the common class is the object's class,
        // and the common adapters are the adapters cached for that class in the adapter manager
        // See bug 177592 for more details.
        if (allSameClass(objects)) {
        	
			Class<?> clazz = objects.get(0).getClass();
        	commonAdapters.addAll(Arrays.asList(Platform.getAdapterManager().computeAdapterTypes(clazz)));
			List<Class<?>> result = new ArrayList<Class<?>>(1);
        	result.add(clazz);
        	return result;
        }
        
        // Compute all the super classes, interfaces, and adapters 
        // for the first element.
		List<Class<?>> classes = computeClassOrder(objects.get(0).getClass());
		List<String> adapters = computeAdapterOrder(classes);
		List<Class<?>> interfaces = computeInterfaceOrder(classes);

        // Cache of all types found in the selection - this is needed
        // to compute common adapters.
		List<Class<?>> lastCommonTypes = new ArrayList<Class<?>>();

        boolean classesEmpty = classes.isEmpty();
        boolean interfacesEmpty = interfaces.isEmpty();

        // Traverse the selection if there is more than one element selected.
        for (int i = 1; i < objects.size(); i++) {
            // Compute all the super classes for the current element
			List<Class<?>> otherClasses = computeClassOrder(objects.get(i).getClass());
            if (!classesEmpty) {
                classesEmpty = extractCommonClasses(classes, otherClasses);
            }

            // Compute all the interfaces for the current element
            // and all of its super classes.
			List<Class<?>> otherInterfaces = computeInterfaceOrder(otherClasses);
            if (!interfacesEmpty) {
                interfacesEmpty = extractCommonClasses(interfaces,
                        otherInterfaces);
            }

            // Compute all the adapters provided for the calculated
            // classes and interfaces for this element.
			List<Class<?>> classesAndInterfaces = new ArrayList<Class<?>>(otherClasses);
            if (otherInterfaces != null) {
				classesAndInterfaces.addAll(otherInterfaces);
			}
			List<String> otherAdapters = computeAdapterOrder(classesAndInterfaces);

            // Compute common adapters
            // Note here that an adapter can match a class or interface, that is
            // that an element in the selection may not adapt to a type but instead
            // be of that type.
            // If the selected classes doesn't have adapters, keep
            // adapters that match the given classes types (classes and interfaces).
            if (otherAdapters.isEmpty() && !adapters.isEmpty()) {
                removeNonCommonAdapters(adapters, classesAndInterfaces);
            } else {
                if (adapters.isEmpty()) {
                    removeNonCommonAdapters(otherAdapters, lastCommonTypes);
                    if (!otherAdapters.isEmpty()) {
						adapters.addAll(otherAdapters);
					}
                } else {
                    // Remove any adapters of the first element that
                    // are not in the current element's adapter list.
					for (Iterator<String> it = adapters.iterator(); it.hasNext();) {
						String adapter = it.next();
                        if (!otherAdapters.contains(adapter)) {
                            it.remove();
                        }
                    }
                }
            }

            // Remember the common search order up to now, this is
            // used to match adapters against common classes or interfaces.
            lastCommonTypes.clear();
            lastCommonTypes.addAll(classes);
            lastCommonTypes.addAll(interfaces);

            if (interfacesEmpty && classesEmpty && adapters.isEmpty()) {
                // As soon as we detect nothing in common, just exit.
                return null;
            }
        }

        // Once the common classes, interfaces, and adapters are
        // calculated, let's prune the lists to remove duplicates.       
		ArrayList<Class<?>> results = new ArrayList<Class<?>>(4);
		ArrayList<Class<?>> superClasses = new ArrayList<Class<?>>(4);
        if (!classesEmpty) {
            for (int j = 0; j < classes.size(); j++) {
                if (classes.get(j) != null) {
                    superClasses.add(classes.get(j));
                }
            }
            // Just keep the first super class
            if (!superClasses.isEmpty()) {
                results.add(superClasses.get(0));
            }
        }

        if (!interfacesEmpty) {
            removeCommonInterfaces(superClasses, interfaces, results);
        }

        // Remove adapters already included as common classes
        if (!adapters.isEmpty()) {
            removeCommonAdapters(adapters, results);
            commonAdapters.addAll(adapters);
        }
        return results;
    }

    /**
     * Returns <code>true</code> if all objects in the given list are of the same class,
     * <code>false</code> otherwise.
	 */
	private boolean allSameClass(List<Object> objects) {
		int size = objects.size();
		if (size <= 1) return true;
		Class<?> clazz = objects.get(0).getClass();
		for (int i = 1; i < size; ++i) {
			if (!objects.get(i).getClass().equals(clazz)) {
				return false;
			}
		}
		return true;
	}

	private boolean extractCommonClasses(List<Class<?>> classes, List<Class<?>> otherClasses) {
        boolean classesEmpty = true;
        if (otherClasses.isEmpty()) {
            // When no super classes, then it is obvious there
            // are no common super classes with the first element
            // so clear its list.
            classes.clear();
        } else {
            // Remove any super classes of the first element that 
            // are not in the current element's super classes list.
            for (int j = 0; j < classes.size(); j++) {
                if (classes.get(j) != null) {
                    classesEmpty = false; // TODO: should this only be set if item not nulled out?
                    if (!otherClasses.contains(classes.get(j))) {
                        classes.set(j, null);
                    }
                }
            }
        }
        return classesEmpty;
    }

	private void removeNonCommonAdapters(List<String> adapters, List<Class<?>> classes) {
        for (int i = 0; i < classes.size(); i++) {
			Class<?> clazz = classes.get(i);
			if (clazz != null) {
                String name = clazz.getName();
                if (adapters.contains(name)) {
					return;
				}
            }
        }
        adapters.clear();
    }

	private void removeCommonInterfaces(List<Class<?>> superClasses, List<Class<?>> types,
			List<Class<?>> results) {
		List<Class<?>> dropInterfaces = null;
        if (!superClasses.isEmpty()) {
            dropInterfaces = computeInterfaceOrder(superClasses);
        }
        for (int j = 0; j < types.size(); j++) {
            if (types.get(j) != null) {
                if (dropInterfaces != null
                        && !dropInterfaces.contains(types.get(j))) {
                    results.add(types.get(j));
                }
            }
        }
    }

	private List<String> computeAdapterOrder(List<Class<?>> classList) {
		Set<String> result = new HashSet<String>(4);
        IAdapterManager adapterMgr = Platform.getAdapterManager();
		for (Iterator<Class<?>> list = classList.iterator(); list.hasNext();) {
			Class<?> clazz = list.next();
            String[] adapters = adapterMgr.computeAdapterTypes(clazz);
            for (int i = 0; i < adapters.length; i++) {
                String adapter = adapters[i];
                if (!result.contains(adapter)) {
                    result.add(adapter);
                }
            }
        }
		return new ArrayList<String>(result);
    }

    /**
     * Returns the common denominator resource class for the given
     * collection of objects.
     * Do not return a resource class if the objects are resources
     * themselves so as to prevent double registration of actions.
     */
	private Class<?> getCommonResourceClass(List<Object> objects) {
        if (objects == null || objects.size() == 0) {
            return null;
        }
		Class<?> resourceClass = LegacyResourceSupport.getResourceClass();
        if (resourceClass == null) {
            // resources plug-in not loaded - no resources. period.
            return null;
        }

		List<Object> testList = new ArrayList<Object>(objects.size());

        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);

            if (object instanceof IAdaptable) {
                if (resourceClass.isInstance(object)) {
                    continue;
                }

                Object resource = LegacyResourceSupport
                        .getAdaptedContributorResource(object);

                if (resource == null) {
                    //Not a resource and does not adapt. No common resource class
                    return null;
                }
                testList.add(resource);
            } else {
                return null;
            }
        }

        return getCommonClass(testList);
    }

    /**
     * Return the ResourceMapping class if the elements all adapt to it.
     */
	private Class<?> getResourceMappingClass(List<Object> objects) {
        if (objects == null || objects.size() == 0) {
            return null;
        }
		Class<?> resourceMappingClass = LegacyResourceSupport
                .getResourceMappingClass();
        if (resourceMappingClass == null) {
            // resources plug-in not loaded - no resources. period.
            return null;
        }

        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);

            if (object instanceof IAdaptable) {
                if (resourceMappingClass.isInstance(object)) {
                    continue;
                }

                Object resourceMapping = LegacyResourceSupport
                        .getAdaptedContributorResourceMapping(object);

                if (resourceMapping == null) {
                    //Not a resource and does not adapt. No common resource class
                    return null;
                }
            } else {
                return null;
            }
        }
        // If we get here then all objects adapt to ResourceMapping
        return resourceMappingClass;
    }

    /**
     * Returns the common denominator class for the given
     * collection of objects.
     */
	private Class<?> getCommonClass(List<Object> objects) {
        if (objects == null || objects.size() == 0) {
			return null;
		}
		Class<?> commonClass = objects.get(0).getClass();
        // try easy
        if (objects.size() == 1) {
			return commonClass;
        // try harder
		}

        for (int i = 1; i < objects.size(); i++) {
            Object object = objects.get(i);
			Class<?> newClass = object.getClass();
            // try the short cut
            if (newClass.equals(commonClass)) {
				continue;
			}
            // compute common class
            commonClass = getCommonClass(commonClass, newClass);
            // give up
            if (commonClass == null) {
				return null;
			}
        }
        return commonClass;
    }

    /**
     * Returns the common denominator class for
     * two input classes.
     */
	private Class<?> getCommonClass(Class<?> class1, Class<?> class2) {
		List<Class<?>> list1 = computeCombinedOrder(class1);
		List<Class<?>> list2 = computeCombinedOrder(class2);
        for (int i = 0; i < list1.size(); i++) {
            for (int j = 0; j < list2.size(); j++) {
				Class<?> candidate1 = list1.get(i);
				Class<?> candidate2 = list2.get(j);
                if (candidate1.equals(candidate2)) {
					return candidate1;
				}
            }
        }
        // no common class
        return null;
    }
}
