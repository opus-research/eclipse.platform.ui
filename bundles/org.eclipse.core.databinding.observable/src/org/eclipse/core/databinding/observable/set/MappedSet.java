/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 263693
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;

/**
 *
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 *
 * @since 1.0
 *
 * @deprecated This class is deprecated.
 */
@Deprecated
// OK to hide warnings on a deprecated class
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MappedSet extends ObservableSet {

	private final IObservableMap wrappedMap;

	/*
	 * Map from values (range elements) to Integer ref counts
	 */
	private Map valueCounts = new HashMap();

	private ISetChangeListener domainListener;

	private IMapChangeListener mapChangeListener = event -> {
		MapDiff diff = event.diff;
		Set additions = new HashSet();
		Set removals = new HashSet();
		for (Iterator it1 = diff.getRemovedKeys().iterator(); it1.hasNext();) {
			Object key1 = it1.next();
			Object oldValue1 = diff.getOldValue(key1);
			if (handleRemoval(oldValue1)) {
				removals.add(oldValue1);
			}
		}
		for (Iterator it2 = diff.getChangedKeys().iterator(); it2.hasNext();) {
			Object key2 = it2.next();
			Object oldValue2 = diff.getOldValue(key2);
			Object newValue1 = diff.getNewValue(key2);
			if (handleRemoval(oldValue2)) {
				removals.add(oldValue2);
			}
			if (handleAddition(newValue1)) {
				additions.add(newValue1);
			}
		}
		for (Iterator it3 = diff.getAddedKeys().iterator(); it3.hasNext();) {
			Object key3 = it3.next();
			Object newValue2 = diff.getNewValue(key3);
			if (handleAddition(newValue2)) {
				additions.add(newValue2);
			}
		}
		fireSetChange(Diffs.createSetDiff(additions, removals));
	};

	private IObservableSet input;

	/**
	 * @param input
	 * @param map
	 */
	public MappedSet(IObservableSet input, IObservableMap map) {
		super(input.getRealm(), Collections.EMPTY_SET, Object.class);
		setWrappedSet(valueCounts.keySet());
		this.wrappedMap = map;
		this.input = input;
		for (Iterator it = input.iterator(); it.hasNext();) {
			Object element = it.next();
			Object functionValue = wrappedMap.get(element);
			handleAddition(functionValue);
		}
		input.addSetChangeListener(domainListener);
		map.addMapChangeListener(mapChangeListener);
		domainListener = event -> {
			Set additions = new HashSet();
			for (Iterator it1 = event.diff.getAdditions().iterator(); it1.hasNext();) {
				Object added = it1.next();
				Object mapValue1 = wrappedMap.get(added);
				if (handleAddition(mapValue1)) {
					additions.add(mapValue1);
				}
			}
			Set removals = new HashSet();
			for (Iterator it2 = event.diff.getRemovals().iterator(); it2.hasNext();) {
				Object removed = it2.next();
				Object mapValue2 = wrappedMap.get(removed);
				if (handleRemoval(mapValue2)) {
					removals.add(mapValue2);
				}
			}
			fireSetChange(Diffs.createSetDiff(additions, removals));
		};
	}

	/**
	 * @param mapValue
	 * @return true if the given mapValue was an addition
	 */
	protected boolean handleAddition(Object mapValue) {
		Integer count = (Integer) valueCounts.get(mapValue);
		if (count == null) {
			valueCounts.put(mapValue, Integer.valueOf(1));
			return true;
		}
		valueCounts.put(mapValue, Integer.valueOf(count.intValue() + 1));
		return false;
	}

	/**
	 * @param mapValue
	 * @return true if the given mapValue has been removed
	 */
	protected boolean handleRemoval(Object mapValue) {
		Integer count = (Integer) valueCounts.get(mapValue);
		if (count.intValue() <= 1) {
			valueCounts.remove(mapValue);
			return true;
		}
		valueCounts.put(mapValue, Integer.valueOf(count.intValue() - 1));
		return false;
	}

	@Override
	public synchronized void dispose() {
		wrappedMap.removeMapChangeListener(mapChangeListener);
		input.removeSetChangeListener(domainListener);
	}

}
