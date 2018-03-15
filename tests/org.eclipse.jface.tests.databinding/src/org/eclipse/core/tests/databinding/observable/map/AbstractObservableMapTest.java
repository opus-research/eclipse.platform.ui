/*******************************************************************************
 * Copyright (c) 2006, 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 349038
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import java.util.Set;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class AbstractObservableMapTest {
	private AbstractObservableMapStub map;

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		map = new AbstractObservableMapStub();
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testIsStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.isStale();
			}
		});
	}

	@Test
	public void testSetStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.setStale(true);
			}
		});
	}

	@Test
	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.fireStale();
			}
		});
	}

	@Test
	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.fireChange();
			}
		});
	}

	@Test
	public void testFireMapChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.fireMapChange(null);
			}
		});
	}

	@Test
	public void testAddListChangeListener_AfterDispose() {
		map.dispose();
		map.addMapChangeListener(new IMapChangeListener() {
			@Override
			public void handleMapChange(MapChangeEvent event) {
				// do nothing
			}
		});
	}

	@Test
	public void testRemoveListChangeListener_AfterDispose() {
		map.dispose();
		map.removeMapChangeListener(new IMapChangeListener() {
			@Override
			public void handleMapChange(MapChangeEvent event) {
				// do nothing
			}
		});
	}

	@Test
	public void testAddChangeListener_AfterDispose() {
		map.dispose();
		map.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		});
	}

	@Test
	public void testRemoveChangeListener_AfterDispose() {
		map.dispose();
		map.removeChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		});
	}

	@Test
	public void testAddStaleListener_AfterDispose() {
		map.dispose();
		map.addStaleListener(new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
				// do nothing
			}
		});
	}

	@Test
	public void testRemoveStaleListener_AfterDispose() {
		map.dispose();
		map.removeStaleListener(new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
				// do nothing
			}
		});
	}

	@Test
	public void testAddDisposeListener_AfterDispose() {
		map.dispose();
		map.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				// do nothing
			}
		});
	}

	@Test
	public void testRemoveDisposeListener_AfterDispose() {
		map.dispose();
		map.removeDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				// do nothing
			}
		});
	}

	@Test
	public void testHasListeners_AfterDispose() {
		map.dispose();
		map.hasListeners();
	}

	static class AbstractObservableMapStub extends AbstractObservableMap {
		@Override
		public Set entrySet() {
			return null;
		}

		@Override
		protected void fireChange() {
			super.fireChange();
		}

		@Override
		protected void fireMapChange(MapDiff diff) {
			super.fireMapChange(diff);
		}

		@Override
		protected void fireStale() {
			super.fireStale();
		}

		@Override
		protected synchronized boolean hasListeners() {
			return super.hasListeners();
		}
	}
}
