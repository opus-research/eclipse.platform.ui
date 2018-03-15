/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import java.util.StringTokenizer;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;

/**
 * @since 3.2
 *
 */
public class AggregateObservableValue extends AbstractObservableValue<Object> {

	private IObservableValue<Object>[] observableValues;

	private String delimiter;

	private boolean updating = false;

	private String currentValue;

	private IValueChangeListener<Object> listener = event -> {
		if (!updating) {
			fireValueChange(Diffs.createValueDiff(currentValue, doGetValue()));
		}
	};

	/**
	 * @param observableValues
	 * @param delimiter
	 */
	public AggregateObservableValue(IObservableValue<Object>[] observableValues,
			String delimiter) {
		this.observableValues = observableValues;
		this.delimiter = delimiter;
		for (int i = 0; i < observableValues.length; i++) {
			observableValues[i].addValueChangeListener(listener);
		}
		doGetValue();
	}

	@Override
	public void doSetValue(Object value) {
		Object oldValue = doGetValue();
		StringTokenizer tokenizer = new StringTokenizer((String) value,
				delimiter);
		try {
			updating = true;
			for (int i = 0; i < observableValues.length; i++) {
				if (tokenizer.hasMoreElements()) {
					observableValues[i].setValue(tokenizer.nextElement());
				} else {
					observableValues[i].setValue(null);
				}
			}
		} finally {
			updating = false;
		}
		doGetValue();
		fireValueChange(Diffs.createValueDiff(oldValue, value));
	}

	@Override
	public Object doGetValue() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < observableValues.length; i++) {
			if (i > 0 & i < observableValues.length) {
				result.append(delimiter);
			}
			result.append(observableValues[i].getValue());
		}
		currentValue = result.toString();
		return currentValue;
	}

	@Override
	public Object getValueType() {
		return String.class;
	}

	@Override
	public synchronized void dispose() {
		for (int i = 0; i < observableValues.length; i++) {
			observableValues[i].removeValueChangeListener(listener);
		}
		super.dispose();
	}

}
