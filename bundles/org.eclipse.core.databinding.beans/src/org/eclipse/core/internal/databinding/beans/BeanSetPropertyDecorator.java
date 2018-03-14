/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 195222)
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.Set;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanMapProperty;
import org.eclipse.core.databinding.beans.IBeanSetProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.set.SetProperty;

/**
 * @since 3.3
 *
 */
public class BeanSetPropertyDecorator extends SetProperty implements
		IBeanSetProperty {
	private final ISetProperty delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public BeanSetPropertyDecorator(ISetProperty delegate,
			PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	@Override
	public Object getElementType() {
		return delegate.getElementType();
	}

	@Override
	protected Set doGetSet(Object source) {
		return delegate.getSet(source);
	}

	@Override
	protected void doSetSet(Object source, Set set) {
		delegate.setSet(source, set);
	}

	@Override
	protected void doUpdateSet(Object source, SetDiff diff) {
		delegate.updateSet(source, diff);
	}

	@Override
	public IBeanMapProperty values(String propertyName) {
		return values(propertyName, null);
	}

	@Override
	public IBeanMapProperty values(String propertyName, Class valueType) {
		Class beanClass = (Class) delegate.getElementType();
		return values(BeanProperties.value(beanClass, propertyName, valueType));
	}

	@Override
	public IBeanMapProperty values(IBeanValueProperty property) {
		return new BeanMapPropertyDecorator(super.values(property),
				property.getPropertyDescriptor());
	}

	@Override
	public IObservableSet observe(Object source) {
		return new BeanObservableSetDecorator(delegate.observe(source),
				propertyDescriptor);
	}

	@Override
	public IObservableSet observe(Realm realm, Object source) {
		return new BeanObservableSetDecorator(delegate.observe(realm, source),
				propertyDescriptor);
	}

	@Override
	public IObservableSet observeDetail(IObservableValue master) {
		return new BeanObservableSetDecorator(delegate.observeDetail(master),
				propertyDescriptor);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
