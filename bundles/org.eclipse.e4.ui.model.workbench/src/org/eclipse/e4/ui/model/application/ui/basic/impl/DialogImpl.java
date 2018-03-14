/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.impl.StringToObjectMapImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MFrameElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dialog</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getLocalizedLabel <em>Localized Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getLocalizedTooltip <em>Localized Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getElementId <em>Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getPersistedState <em>Persisted State</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getContributorURI <em>Contributor URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getTransientData <em>Transient Data</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getRenderer <em>Renderer</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#isToBeRendered <em>To Be Rendered</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#isOnTop <em>On Top</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getContainerData <em>Container Data</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getCurSharedRef <em>Cur Shared Ref</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getVisibleWhen <em>Visible When</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getAccessibilityPhrase <em>Accessibility Phrase</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getLocalizedAccessibilityPhrase <em>Localized Accessibility Phrase</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getSelectedElement <em>Selected Element</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#isHorizontal <em>Horizontal</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.DialogImpl#getHeight <em>Height</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DialogImpl extends org.eclipse.emf.ecore.impl.MinimalEObjectImpl.Container implements MDialog {
	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;
	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;
	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @generated
	 * @ordered
	 */
	protected String tooltip = TOOLTIP_EDEFAULT;
	/**
	 * The default value of the '{@link #getLocalizedLabel() <em>Localized Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_LABEL_EDEFAULT = ""; //$NON-NLS-1$
	/**
	 * The default value of the '{@link #getLocalizedTooltip() <em>Localized Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_TOOLTIP_EDEFAULT = ""; //$NON-NLS-1$
	/**
	 * The default value of the '{@link #getElementId() <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElementId()
	 * @generated
	 * @ordered
	 */
	protected static final String ELEMENT_ID_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getElementId() <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElementId()
	 * @generated
	 * @ordered
	 */
	protected String elementId = ELEMENT_ID_EDEFAULT;
	/**
	 * The cached value of the '{@link #getPersistedState() <em>Persisted State</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPersistedState()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> persistedState;
	/**
	 * The cached value of the '{@link #getTags() <em>Tags</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTags()
	 * @generated
	 * @ordered
	 */
	protected EList<String> tags;
	/**
	 * The default value of the '{@link #getContributorURI() <em>Contributor URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributorURI()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTRIBUTOR_URI_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getContributorURI() <em>Contributor URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributorURI()
	 * @generated
	 * @ordered
	 */
	protected String contributorURI = CONTRIBUTOR_URI_EDEFAULT;
	/**
	 * The cached value of the '{@link #getTransientData() <em>Transient Data</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTransientData()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, Object> transientData;
	/**
	 * The default value of the '{@link #getWidget() <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidget()
	 * @generated
	 * @ordered
	 */
	protected static final Object WIDGET_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getWidget() <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidget()
	 * @generated
	 * @ordered
	 */
	protected Object widget = WIDGET_EDEFAULT;
	/**
	 * The default value of the '{@link #getRenderer() <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRenderer()
	 * @generated
	 * @ordered
	 */
	protected static final Object RENDERER_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getRenderer() <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRenderer()
	 * @generated
	 * @ordered
	 */
	protected Object renderer = RENDERER_EDEFAULT;
	/**
	 * The default value of the '{@link #isToBeRendered() <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isToBeRendered()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TO_BE_RENDERED_EDEFAULT = true;
	/**
	 * The cached value of the '{@link #isToBeRendered() <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isToBeRendered()
	 * @generated
	 * @ordered
	 */
	protected boolean toBeRendered = TO_BE_RENDERED_EDEFAULT;
	/**
	 * The default value of the '{@link #isOnTop() <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOnTop()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ON_TOP_EDEFAULT = false;
	/**
	 * The cached value of the '{@link #isOnTop() <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOnTop()
	 * @generated
	 * @ordered
	 */
	protected boolean onTop = ON_TOP_EDEFAULT;
	/**
	 * The default value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected static final boolean VISIBLE_EDEFAULT = true;
	/**
	 * The cached value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected boolean visible = VISIBLE_EDEFAULT;
	/**
	 * The default value of the '{@link #getContainerData() <em>Container Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainerData()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTAINER_DATA_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getContainerData() <em>Container Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainerData()
	 * @generated
	 * @ordered
	 */
	protected String containerData = CONTAINER_DATA_EDEFAULT;
	/**
	 * The cached value of the '{@link #getCurSharedRef() <em>Cur Shared Ref</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCurSharedRef()
	 * @generated
	 * @ordered
	 */
	protected MPlaceholder curSharedRef;
	/**
	 * The cached value of the '{@link #getVisibleWhen() <em>Visible When</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisibleWhen()
	 * @generated
	 * @ordered
	 */
	protected MExpression visibleWhen;
	/**
	 * The default value of the '{@link #getAccessibilityPhrase() <em>Accessibility Phrase</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAccessibilityPhrase()
	 * @generated
	 * @ordered
	 */
	protected static final String ACCESSIBILITY_PHRASE_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getAccessibilityPhrase() <em>Accessibility Phrase</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAccessibilityPhrase()
	 * @generated
	 * @ordered
	 */
	protected String accessibilityPhrase = ACCESSIBILITY_PHRASE_EDEFAULT;
	/**
	 * The default value of the '{@link #getLocalizedAccessibilityPhrase() <em>Localized Accessibility Phrase</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedAccessibilityPhrase()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_ACCESSIBILITY_PHRASE_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<MPartSashContainerElement> children;
	/**
	 * The cached value of the '{@link #getSelectedElement() <em>Selected Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSelectedElement()
	 * @generated
	 * @ordered
	 */
	protected MPartSashContainerElement selectedElement;
	/**
	 * The default value of the '{@link #isHorizontal() <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHorizontal()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HORIZONTAL_EDEFAULT = false;
	/**
	 * The cached value of the '{@link #isHorizontal() <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHorizontal()
	 * @generated
	 * @ordered
	 */
	protected boolean horizontal = HORIZONTAL_EDEFAULT;
	/**
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final int X_EDEFAULT = -2147483648;
	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected int x = X_EDEFAULT;
	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final int Y_EDEFAULT = -2147483648;
	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected int y = Y_EDEFAULT;
	/**
	 * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected static final int WIDTH_EDEFAULT = -1;
	/**
	 * The cached value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected int width = WIDTH_EDEFAULT;
	/**
	 * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected static final int HEIGHT_EDEFAULT = -1;
	/**
	 * The cached value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected int height = HEIGHT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DialogImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BasicPackageImpl.Literals.DIALOG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__LABEL, oldLabel, label));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__ICON_URI, oldIconURI, iconURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__TOOLTIP, oldTooltip, tooltip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLocalizedLabel() {
		// TODO: implement this method to return the 'Localized Label' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLocalizedTooltip() {
		// TODO: implement this method to return the 'Localized Tooltip' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setElementId(String newElementId) {
		String oldElementId = elementId;
		elementId = newElementId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__ELEMENT_ID, oldElementId, elementId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map<String, String> getPersistedState() {
		if (persistedState == null) {
			persistedState = new EcoreEMap<String,String>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, BasicPackageImpl.DIALOG__PERSISTED_STATE);
		}
		return persistedState.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getTags() {
		if (tags == null) {
			tags = new EDataTypeUniqueEList<String>(String.class, this, BasicPackageImpl.DIALOG__TAGS);
		}
		return tags;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getContributorURI() {
		return contributorURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContributorURI(String newContributorURI) {
		String oldContributorURI = contributorURI;
		contributorURI = newContributorURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__CONTRIBUTOR_URI, oldContributorURI, contributorURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map<String, Object> getTransientData() {
		if (transientData == null) {
			transientData = new EcoreEMap<String,Object>(ApplicationPackageImpl.Literals.STRING_TO_OBJECT_MAP, StringToObjectMapImpl.class, this, BasicPackageImpl.DIALOG__TRANSIENT_DATA);
		}
		return transientData.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getWidget() {
		return widget;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWidget(Object newWidget) {
		Object oldWidget = widget;
		widget = newWidget;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__WIDGET, oldWidget, widget));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getRenderer() {
		return renderer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRenderer(Object newRenderer) {
		Object oldRenderer = renderer;
		renderer = newRenderer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__RENDERER, oldRenderer, renderer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isToBeRendered() {
		return toBeRendered;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToBeRendered(boolean newToBeRendered) {
		boolean oldToBeRendered = toBeRendered;
		toBeRendered = newToBeRendered;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__TO_BE_RENDERED, oldToBeRendered, toBeRendered));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOnTop() {
		return onTop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOnTop(boolean newOnTop) {
		boolean oldOnTop = onTop;
		onTop = newOnTop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__ON_TOP, oldOnTop, onTop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisible(boolean newVisible) {
		boolean oldVisible = visible;
		visible = newVisible;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__VISIBLE, oldVisible, visible));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public MElementContainer<MUIElement> getParent() {
		if (eContainerFeatureID() != BasicPackageImpl.DIALOG__PARENT) return null;
		return (MElementContainer<MUIElement>)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(MElementContainer<MUIElement> newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, BasicPackageImpl.DIALOG__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(MElementContainer<MUIElement> newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != BasicPackageImpl.DIALOG__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, (EObject)newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, UiPackageImpl.ELEMENT_CONTAINER__CHILDREN, MElementContainer.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__PARENT, newParent, newParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getContainerData() {
		return containerData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContainerData(String newContainerData) {
		String oldContainerData = containerData;
		containerData = newContainerData;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__CONTAINER_DATA, oldContainerData, containerData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPlaceholder getCurSharedRef() {
		if (curSharedRef != null && ((EObject)curSharedRef).eIsProxy()) {
			InternalEObject oldCurSharedRef = (InternalEObject)curSharedRef;
			curSharedRef = (MPlaceholder)eResolveProxy(oldCurSharedRef);
			if (curSharedRef != oldCurSharedRef) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BasicPackageImpl.DIALOG__CUR_SHARED_REF, oldCurSharedRef, curSharedRef));
			}
		}
		return curSharedRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPlaceholder basicGetCurSharedRef() {
		return curSharedRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCurSharedRef(MPlaceholder newCurSharedRef) {
		MPlaceholder oldCurSharedRef = curSharedRef;
		curSharedRef = newCurSharedRef;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__CUR_SHARED_REF, oldCurSharedRef, curSharedRef));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MExpression getVisibleWhen() {
		return visibleWhen;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetVisibleWhen(MExpression newVisibleWhen, NotificationChain msgs) {
		MExpression oldVisibleWhen = visibleWhen;
		visibleWhen = newVisibleWhen;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__VISIBLE_WHEN, oldVisibleWhen, newVisibleWhen);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisibleWhen(MExpression newVisibleWhen) {
		if (newVisibleWhen != visibleWhen) {
			NotificationChain msgs = null;
			if (visibleWhen != null)
				msgs = ((InternalEObject)visibleWhen).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - BasicPackageImpl.DIALOG__VISIBLE_WHEN, null, msgs);
			if (newVisibleWhen != null)
				msgs = ((InternalEObject)newVisibleWhen).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - BasicPackageImpl.DIALOG__VISIBLE_WHEN, null, msgs);
			msgs = basicSetVisibleWhen(newVisibleWhen, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__VISIBLE_WHEN, newVisibleWhen, newVisibleWhen));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getAccessibilityPhrase() {
		return accessibilityPhrase;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAccessibilityPhrase(String newAccessibilityPhrase) {
		String oldAccessibilityPhrase = accessibilityPhrase;
		accessibilityPhrase = newAccessibilityPhrase;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE, oldAccessibilityPhrase, accessibilityPhrase));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLocalizedAccessibilityPhrase() {
		// TODO: implement this method to return the 'Localized Accessibility Phrase' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MPartSashContainerElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<MPartSashContainerElement>(MPartSashContainerElement.class, this, BasicPackageImpl.DIALOG__CHILDREN, UiPackageImpl.UI_ELEMENT__PARENT) { private static final long serialVersionUID = 1L; @Override public Class<?> getInverseFeatureClass() { return MUIElement.class; } };
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartSashContainerElement getSelectedElement() {
		if (selectedElement != null && ((EObject)selectedElement).eIsProxy()) {
			InternalEObject oldSelectedElement = (InternalEObject)selectedElement;
			selectedElement = (MPartSashContainerElement)eResolveProxy(oldSelectedElement);
			if (selectedElement != oldSelectedElement) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, BasicPackageImpl.DIALOG__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
			}
		}
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartSashContainerElement basicGetSelectedElement() {
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelectedElement(MPartSashContainerElement newSelectedElement) {
		MPartSashContainerElement oldSelectedElement = selectedElement;
		selectedElement = newSelectedElement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHorizontal(boolean newHorizontal) {
		boolean oldHorizontal = horizontal;
		horizontal = newHorizontal;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__HORIZONTAL, oldHorizontal, horizontal));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setX(int newX) {
		int oldX = x;
		x = newX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__X, oldX, x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setY(int newY) {
		int oldY = y;
		y = newY;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWidth(int newWidth) {
		int oldWidth = width;
		width = newWidth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__WIDTH, oldWidth, width));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHeight(int newHeight) {
		int oldHeight = height;
		height = newHeight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.DIALOG__HEIGHT, oldHeight, height));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void updateLocalization() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case BasicPackageImpl.DIALOG__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((MElementContainer<MUIElement>)otherEnd, msgs);
			case BasicPackageImpl.DIALOG__CHILDREN:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildren()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case BasicPackageImpl.DIALOG__PERSISTED_STATE:
				return ((InternalEList<?>)((EMap.InternalMapView<String, String>)getPersistedState()).eMap()).basicRemove(otherEnd, msgs);
			case BasicPackageImpl.DIALOG__TRANSIENT_DATA:
				return ((InternalEList<?>)((EMap.InternalMapView<String, Object>)getTransientData()).eMap()).basicRemove(otherEnd, msgs);
			case BasicPackageImpl.DIALOG__PARENT:
				return basicSetParent(null, msgs);
			case BasicPackageImpl.DIALOG__VISIBLE_WHEN:
				return basicSetVisibleWhen(null, msgs);
			case BasicPackageImpl.DIALOG__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case BasicPackageImpl.DIALOG__PARENT:
				return eInternalContainer().eInverseRemove(this, UiPackageImpl.ELEMENT_CONTAINER__CHILDREN, MElementContainer.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case BasicPackageImpl.DIALOG__LABEL:
				return getLabel();
			case BasicPackageImpl.DIALOG__ICON_URI:
				return getIconURI();
			case BasicPackageImpl.DIALOG__TOOLTIP:
				return getTooltip();
			case BasicPackageImpl.DIALOG__LOCALIZED_LABEL:
				return getLocalizedLabel();
			case BasicPackageImpl.DIALOG__LOCALIZED_TOOLTIP:
				return getLocalizedTooltip();
			case BasicPackageImpl.DIALOG__ELEMENT_ID:
				return getElementId();
			case BasicPackageImpl.DIALOG__PERSISTED_STATE:
				if (coreType) return ((EMap.InternalMapView<String, String>)getPersistedState()).eMap();
				else return getPersistedState();
			case BasicPackageImpl.DIALOG__TAGS:
				return getTags();
			case BasicPackageImpl.DIALOG__CONTRIBUTOR_URI:
				return getContributorURI();
			case BasicPackageImpl.DIALOG__TRANSIENT_DATA:
				if (coreType) return ((EMap.InternalMapView<String, Object>)getTransientData()).eMap();
				else return getTransientData();
			case BasicPackageImpl.DIALOG__WIDGET:
				return getWidget();
			case BasicPackageImpl.DIALOG__RENDERER:
				return getRenderer();
			case BasicPackageImpl.DIALOG__TO_BE_RENDERED:
				return isToBeRendered();
			case BasicPackageImpl.DIALOG__ON_TOP:
				return isOnTop();
			case BasicPackageImpl.DIALOG__VISIBLE:
				return isVisible();
			case BasicPackageImpl.DIALOG__PARENT:
				return getParent();
			case BasicPackageImpl.DIALOG__CONTAINER_DATA:
				return getContainerData();
			case BasicPackageImpl.DIALOG__CUR_SHARED_REF:
				if (resolve) return getCurSharedRef();
				return basicGetCurSharedRef();
			case BasicPackageImpl.DIALOG__VISIBLE_WHEN:
				return getVisibleWhen();
			case BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE:
				return getAccessibilityPhrase();
			case BasicPackageImpl.DIALOG__LOCALIZED_ACCESSIBILITY_PHRASE:
				return getLocalizedAccessibilityPhrase();
			case BasicPackageImpl.DIALOG__CHILDREN:
				return getChildren();
			case BasicPackageImpl.DIALOG__SELECTED_ELEMENT:
				if (resolve) return getSelectedElement();
				return basicGetSelectedElement();
			case BasicPackageImpl.DIALOG__HORIZONTAL:
				return isHorizontal();
			case BasicPackageImpl.DIALOG__X:
				return getX();
			case BasicPackageImpl.DIALOG__Y:
				return getY();
			case BasicPackageImpl.DIALOG__WIDTH:
				return getWidth();
			case BasicPackageImpl.DIALOG__HEIGHT:
				return getHeight();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case BasicPackageImpl.DIALOG__LABEL:
				setLabel((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__ICON_URI:
				setIconURI((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__ELEMENT_ID:
				setElementId((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__PERSISTED_STATE:
				((EStructuralFeature.Setting)((EMap.InternalMapView<String, String>)getPersistedState()).eMap()).set(newValue);
				return;
			case BasicPackageImpl.DIALOG__TAGS:
				getTags().clear();
				getTags().addAll((Collection<? extends String>)newValue);
				return;
			case BasicPackageImpl.DIALOG__CONTRIBUTOR_URI:
				setContributorURI((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__TRANSIENT_DATA:
				((EStructuralFeature.Setting)((EMap.InternalMapView<String, Object>)getTransientData()).eMap()).set(newValue);
				return;
			case BasicPackageImpl.DIALOG__WIDGET:
				setWidget(newValue);
				return;
			case BasicPackageImpl.DIALOG__RENDERER:
				setRenderer(newValue);
				return;
			case BasicPackageImpl.DIALOG__TO_BE_RENDERED:
				setToBeRendered((Boolean)newValue);
				return;
			case BasicPackageImpl.DIALOG__ON_TOP:
				setOnTop((Boolean)newValue);
				return;
			case BasicPackageImpl.DIALOG__VISIBLE:
				setVisible((Boolean)newValue);
				return;
			case BasicPackageImpl.DIALOG__PARENT:
				setParent((MElementContainer<MUIElement>)newValue);
				return;
			case BasicPackageImpl.DIALOG__CONTAINER_DATA:
				setContainerData((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__CUR_SHARED_REF:
				setCurSharedRef((MPlaceholder)newValue);
				return;
			case BasicPackageImpl.DIALOG__VISIBLE_WHEN:
				setVisibleWhen((MExpression)newValue);
				return;
			case BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE:
				setAccessibilityPhrase((String)newValue);
				return;
			case BasicPackageImpl.DIALOG__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MPartSashContainerElement>)newValue);
				return;
			case BasicPackageImpl.DIALOG__SELECTED_ELEMENT:
				setSelectedElement((MPartSashContainerElement)newValue);
				return;
			case BasicPackageImpl.DIALOG__HORIZONTAL:
				setHorizontal((Boolean)newValue);
				return;
			case BasicPackageImpl.DIALOG__X:
				setX((Integer)newValue);
				return;
			case BasicPackageImpl.DIALOG__Y:
				setY((Integer)newValue);
				return;
			case BasicPackageImpl.DIALOG__WIDTH:
				setWidth((Integer)newValue);
				return;
			case BasicPackageImpl.DIALOG__HEIGHT:
				setHeight((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case BasicPackageImpl.DIALOG__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__ELEMENT_ID:
				setElementId(ELEMENT_ID_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__PERSISTED_STATE:
				getPersistedState().clear();
				return;
			case BasicPackageImpl.DIALOG__TAGS:
				getTags().clear();
				return;
			case BasicPackageImpl.DIALOG__CONTRIBUTOR_URI:
				setContributorURI(CONTRIBUTOR_URI_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__TRANSIENT_DATA:
				getTransientData().clear();
				return;
			case BasicPackageImpl.DIALOG__WIDGET:
				setWidget(WIDGET_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__RENDERER:
				setRenderer(RENDERER_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__TO_BE_RENDERED:
				setToBeRendered(TO_BE_RENDERED_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__ON_TOP:
				setOnTop(ON_TOP_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__VISIBLE:
				setVisible(VISIBLE_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__PARENT:
				setParent((MElementContainer<MUIElement>)null);
				return;
			case BasicPackageImpl.DIALOG__CONTAINER_DATA:
				setContainerData(CONTAINER_DATA_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__CUR_SHARED_REF:
				setCurSharedRef((MPlaceholder)null);
				return;
			case BasicPackageImpl.DIALOG__VISIBLE_WHEN:
				setVisibleWhen((MExpression)null);
				return;
			case BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE:
				setAccessibilityPhrase(ACCESSIBILITY_PHRASE_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__CHILDREN:
				getChildren().clear();
				return;
			case BasicPackageImpl.DIALOG__SELECTED_ELEMENT:
				setSelectedElement((MPartSashContainerElement)null);
				return;
			case BasicPackageImpl.DIALOG__HORIZONTAL:
				setHorizontal(HORIZONTAL_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__X:
				setX(X_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__Y:
				setY(Y_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__WIDTH:
				setWidth(WIDTH_EDEFAULT);
				return;
			case BasicPackageImpl.DIALOG__HEIGHT:
				setHeight(HEIGHT_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case BasicPackageImpl.DIALOG__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case BasicPackageImpl.DIALOG__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case BasicPackageImpl.DIALOG__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case BasicPackageImpl.DIALOG__LOCALIZED_LABEL:
				return LOCALIZED_LABEL_EDEFAULT == null ? getLocalizedLabel() != null : !LOCALIZED_LABEL_EDEFAULT.equals(getLocalizedLabel());
			case BasicPackageImpl.DIALOG__LOCALIZED_TOOLTIP:
				return LOCALIZED_TOOLTIP_EDEFAULT == null ? getLocalizedTooltip() != null : !LOCALIZED_TOOLTIP_EDEFAULT.equals(getLocalizedTooltip());
			case BasicPackageImpl.DIALOG__ELEMENT_ID:
				return ELEMENT_ID_EDEFAULT == null ? elementId != null : !ELEMENT_ID_EDEFAULT.equals(elementId);
			case BasicPackageImpl.DIALOG__PERSISTED_STATE:
				return persistedState != null && !persistedState.isEmpty();
			case BasicPackageImpl.DIALOG__TAGS:
				return tags != null && !tags.isEmpty();
			case BasicPackageImpl.DIALOG__CONTRIBUTOR_URI:
				return CONTRIBUTOR_URI_EDEFAULT == null ? contributorURI != null : !CONTRIBUTOR_URI_EDEFAULT.equals(contributorURI);
			case BasicPackageImpl.DIALOG__TRANSIENT_DATA:
				return transientData != null && !transientData.isEmpty();
			case BasicPackageImpl.DIALOG__WIDGET:
				return WIDGET_EDEFAULT == null ? widget != null : !WIDGET_EDEFAULT.equals(widget);
			case BasicPackageImpl.DIALOG__RENDERER:
				return RENDERER_EDEFAULT == null ? renderer != null : !RENDERER_EDEFAULT.equals(renderer);
			case BasicPackageImpl.DIALOG__TO_BE_RENDERED:
				return toBeRendered != TO_BE_RENDERED_EDEFAULT;
			case BasicPackageImpl.DIALOG__ON_TOP:
				return onTop != ON_TOP_EDEFAULT;
			case BasicPackageImpl.DIALOG__VISIBLE:
				return visible != VISIBLE_EDEFAULT;
			case BasicPackageImpl.DIALOG__PARENT:
				return getParent() != null;
			case BasicPackageImpl.DIALOG__CONTAINER_DATA:
				return CONTAINER_DATA_EDEFAULT == null ? containerData != null : !CONTAINER_DATA_EDEFAULT.equals(containerData);
			case BasicPackageImpl.DIALOG__CUR_SHARED_REF:
				return curSharedRef != null;
			case BasicPackageImpl.DIALOG__VISIBLE_WHEN:
				return visibleWhen != null;
			case BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE:
				return ACCESSIBILITY_PHRASE_EDEFAULT == null ? accessibilityPhrase != null : !ACCESSIBILITY_PHRASE_EDEFAULT.equals(accessibilityPhrase);
			case BasicPackageImpl.DIALOG__LOCALIZED_ACCESSIBILITY_PHRASE:
				return LOCALIZED_ACCESSIBILITY_PHRASE_EDEFAULT == null ? getLocalizedAccessibilityPhrase() != null : !LOCALIZED_ACCESSIBILITY_PHRASE_EDEFAULT.equals(getLocalizedAccessibilityPhrase());
			case BasicPackageImpl.DIALOG__CHILDREN:
				return children != null && !children.isEmpty();
			case BasicPackageImpl.DIALOG__SELECTED_ELEMENT:
				return selectedElement != null;
			case BasicPackageImpl.DIALOG__HORIZONTAL:
				return horizontal != HORIZONTAL_EDEFAULT;
			case BasicPackageImpl.DIALOG__X:
				return x != X_EDEFAULT;
			case BasicPackageImpl.DIALOG__Y:
				return y != Y_EDEFAULT;
			case BasicPackageImpl.DIALOG__WIDTH:
				return width != WIDTH_EDEFAULT;
			case BasicPackageImpl.DIALOG__HEIGHT:
				return height != HEIGHT_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MApplicationElement.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.DIALOG__ELEMENT_ID: return ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;
				case BasicPackageImpl.DIALOG__PERSISTED_STATE: return ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;
				case BasicPackageImpl.DIALOG__TAGS: return ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;
				case BasicPackageImpl.DIALOG__CONTRIBUTOR_URI: return ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;
				case BasicPackageImpl.DIALOG__TRANSIENT_DATA: return ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.DIALOG__WIDGET: return UiPackageImpl.UI_ELEMENT__WIDGET;
				case BasicPackageImpl.DIALOG__RENDERER: return UiPackageImpl.UI_ELEMENT__RENDERER;
				case BasicPackageImpl.DIALOG__TO_BE_RENDERED: return UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED;
				case BasicPackageImpl.DIALOG__ON_TOP: return UiPackageImpl.UI_ELEMENT__ON_TOP;
				case BasicPackageImpl.DIALOG__VISIBLE: return UiPackageImpl.UI_ELEMENT__VISIBLE;
				case BasicPackageImpl.DIALOG__PARENT: return UiPackageImpl.UI_ELEMENT__PARENT;
				case BasicPackageImpl.DIALOG__CONTAINER_DATA: return UiPackageImpl.UI_ELEMENT__CONTAINER_DATA;
				case BasicPackageImpl.DIALOG__CUR_SHARED_REF: return UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF;
				case BasicPackageImpl.DIALOG__VISIBLE_WHEN: return UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN;
				case BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE: return UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE;
				case BasicPackageImpl.DIALOG__LOCALIZED_ACCESSIBILITY_PHRASE: return UiPackageImpl.UI_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.DIALOG__CHILDREN: return UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;
				case BasicPackageImpl.DIALOG__SELECTED_ELEMENT: return UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;
				default: return -1;
			}
		}
		if (baseClass == MGenericTile.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.DIALOG__HORIZONTAL: return UiPackageImpl.GENERIC_TILE__HORIZONTAL;
				default: return -1;
			}
		}
		if (baseClass == MFrameElement.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MApplicationElement.class) {
			switch (baseFeatureID) {
				case ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID: return BasicPackageImpl.DIALOG__ELEMENT_ID;
				case ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE: return BasicPackageImpl.DIALOG__PERSISTED_STATE;
				case ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS: return BasicPackageImpl.DIALOG__TAGS;
				case ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI: return BasicPackageImpl.DIALOG__CONTRIBUTOR_URI;
				case ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA: return BasicPackageImpl.DIALOG__TRANSIENT_DATA;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.UI_ELEMENT__WIDGET: return BasicPackageImpl.DIALOG__WIDGET;
				case UiPackageImpl.UI_ELEMENT__RENDERER: return BasicPackageImpl.DIALOG__RENDERER;
				case UiPackageImpl.UI_ELEMENT__TO_BE_RENDERED: return BasicPackageImpl.DIALOG__TO_BE_RENDERED;
				case UiPackageImpl.UI_ELEMENT__ON_TOP: return BasicPackageImpl.DIALOG__ON_TOP;
				case UiPackageImpl.UI_ELEMENT__VISIBLE: return BasicPackageImpl.DIALOG__VISIBLE;
				case UiPackageImpl.UI_ELEMENT__PARENT: return BasicPackageImpl.DIALOG__PARENT;
				case UiPackageImpl.UI_ELEMENT__CONTAINER_DATA: return BasicPackageImpl.DIALOG__CONTAINER_DATA;
				case UiPackageImpl.UI_ELEMENT__CUR_SHARED_REF: return BasicPackageImpl.DIALOG__CUR_SHARED_REF;
				case UiPackageImpl.UI_ELEMENT__VISIBLE_WHEN: return BasicPackageImpl.DIALOG__VISIBLE_WHEN;
				case UiPackageImpl.UI_ELEMENT__ACCESSIBILITY_PHRASE: return BasicPackageImpl.DIALOG__ACCESSIBILITY_PHRASE;
				case UiPackageImpl.UI_ELEMENT__LOCALIZED_ACCESSIBILITY_PHRASE: return BasicPackageImpl.DIALOG__LOCALIZED_ACCESSIBILITY_PHRASE;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN: return BasicPackageImpl.DIALOG__CHILDREN;
				case UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT: return BasicPackageImpl.DIALOG__SELECTED_ELEMENT;
				default: return -1;
			}
		}
		if (baseClass == MGenericTile.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.GENERIC_TILE__HORIZONTAL: return BasicPackageImpl.DIALOG__HORIZONTAL;
				default: return -1;
			}
		}
		if (baseClass == MFrameElement.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case BasicPackageImpl.DIALOG___UPDATE_LOCALIZATION:
				updateLocalization();
				return null;
		}
		return super.eInvoke(operationID, arguments);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", elementId: "); //$NON-NLS-1$
		result.append(elementId);
		result.append(", tags: "); //$NON-NLS-1$
		result.append(tags);
		result.append(", contributorURI: "); //$NON-NLS-1$
		result.append(contributorURI);
		result.append(", widget: "); //$NON-NLS-1$
		result.append(widget);
		result.append(", renderer: "); //$NON-NLS-1$
		result.append(renderer);
		result.append(", toBeRendered: "); //$NON-NLS-1$
		result.append(toBeRendered);
		result.append(", onTop: "); //$NON-NLS-1$
		result.append(onTop);
		result.append(", visible: "); //$NON-NLS-1$
		result.append(visible);
		result.append(", containerData: "); //$NON-NLS-1$
		result.append(containerData);
		result.append(", accessibilityPhrase: "); //$NON-NLS-1$
		result.append(accessibilityPhrase);
		result.append(", horizontal: "); //$NON-NLS-1$
		result.append(horizontal);
		result.append(", x: "); //$NON-NLS-1$
		result.append(x);
		result.append(", y: "); //$NON-NLS-1$
		result.append(y);
		result.append(", width: "); //$NON-NLS-1$
		result.append(width);
		result.append(", height: "); //$NON-NLS-1$
		result.append(height);
		result.append(')');
		return result.toString();
	}

} //DialogImpl
