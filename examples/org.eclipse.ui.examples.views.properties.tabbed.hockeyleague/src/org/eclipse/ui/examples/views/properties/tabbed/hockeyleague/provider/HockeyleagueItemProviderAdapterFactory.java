/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.edit.provider.ChangeNotifier;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IChangeNotifier;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;

import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.util.HockeyleagueAdapterFactory;

/**
 * This is the factory that is used to provide the interfaces needed to support Viewers.
 * The adapters generated by this factory convert EMF adapter notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}.
 * The adapters also support Eclipse property sheets.
 * Note that most of the adapters are shared among multiple instances.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class HockeyleagueItemProviderAdapterFactory extends HockeyleagueAdapterFactory implements ComposeableAdapterFactory, IChangeNotifier, IDisposable {
	/**
	 * This keeps track of the root adapter factory that delegates to this adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComposedAdapterFactory parentAdapterFactory;

	/**
	 * This is used to implement {@link org.eclipse.emf.edit.provider.IChangeNotifier}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IChangeNotifier changeNotifier = new ChangeNotifier();

	/**
	 * This keeps track of all the supported types checked by {@link #isFactoryForType isFactoryForType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection supportedTypes = new ArrayList();

	/**
	 * This constructs an instance.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HockeyleagueItemProviderAdapterFactory() {
		supportedTypes.add(IEditingDomainItemProvider.class);
		supportedTypes.add(IStructuredItemContentProvider.class);
		supportedTypes.add(ITreeItemContentProvider.class);
		supportedTypes.add(IItemLabelProvider.class);
		supportedTypes.add(IItemPropertySource.class);
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ArenaItemProvider arenaItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createArenaAdapter() {
		if (arenaItemProvider == null) {
			arenaItemProvider = new ArenaItemProvider(this);
		}

		return arenaItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DefenceItemProvider defenceItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createDefenceAdapter() {
		if (defenceItemProvider == null) {
			defenceItemProvider = new DefenceItemProvider(this);
		}

		return defenceItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ForwardItemProvider forwardItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createForwardAdapter() {
		if (forwardItemProvider == null) {
			forwardItemProvider = new ForwardItemProvider(this);
		}

		return forwardItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GoalieItemProvider goalieItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createGoalieAdapter() {
		if (goalieItemProvider == null) {
			goalieItemProvider = new GoalieItemProvider(this);
		}

		return goalieItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GoalieStatsItemProvider goalieStatsItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createGoalieStatsAdapter() {
		if (goalieStatsItemProvider == null) {
			goalieStatsItemProvider = new GoalieStatsItemProvider(this);
		}

		return goalieStatsItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LeagueItemProvider leagueItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createLeagueAdapter() {
		if (leagueItemProvider == null) {
			leagueItemProvider = new LeagueItemProvider(this);
		}

		return leagueItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PlayerStatsItemProvider playerStatsItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createPlayerStatsAdapter() {
		if (playerStatsItemProvider == null) {
			playerStatsItemProvider = new PlayerStatsItemProvider(this);
		}

		return playerStatsItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TeamItemProvider teamItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter createTeamAdapter() {
		if (teamItemProvider == null) {
			teamItemProvider = new TeamItemProvider(this);
		}

		return teamItemProvider;
	}

	/**
	 * This returns the root adapter factory that contains this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ComposeableAdapterFactory getRootAdapterFactory() {
		return parentAdapterFactory == null ? this : parentAdapterFactory.getRootAdapterFactory();
	}

	/**
	 * This sets the composed adapter factory that contains this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory) {
		this.parentAdapterFactory = parentAdapterFactory;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isFactoryForType(Object type) {
		return supportedTypes.contains(type) || super.isFactoryForType(type);
	}

	/**
	 * This implementation substitutes the factory itself as the key for the adapter.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Adapter adapt(Notifier notifier, Object type) {
		return super.adapt(notifier, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object adapt(Object object, Object type) {
		if (isFactoryForType(type)) {
			Object adapter = super.adapt(object, type);
			if (!(type instanceof Class) || (((Class)type).isInstance(adapter))) {
				return adapter;
			}
		}

		return null;
	}

	/**
	 * This adds a listener.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void addListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.addListener(notifyChangedListener);
	}

	/**
	 * This removes a listener.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void removeListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.removeListener(notifyChangedListener);
	}

	/**
	 * This delegates to {@link #changeNotifier} and to {@link #parentAdapterFactory}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void fireNotifyChanged(Notification notification) {
		changeNotifier.fireNotifyChanged(notification);

		if (parentAdapterFactory != null) {
			parentAdapterFactory.fireNotifyChanged(notification);
		}
	}

	/**
	 * This disposes all of the item providers created by this factory. 
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void dispose() {
		if (arenaItemProvider != null) arenaItemProvider.dispose();
		if (defenceItemProvider != null) defenceItemProvider.dispose();
		if (forwardItemProvider != null) forwardItemProvider.dispose();
		if (goalieItemProvider != null) goalieItemProvider.dispose();
		if (goalieStatsItemProvider != null) goalieStatsItemProvider.dispose();
		if (leagueItemProvider != null) leagueItemProvider.dispose();
		if (playerStatsItemProvider != null) playerStatsItemProvider.dispose();
		if (teamItemProvider != null) teamItemProvider.dispose();
	}

}
