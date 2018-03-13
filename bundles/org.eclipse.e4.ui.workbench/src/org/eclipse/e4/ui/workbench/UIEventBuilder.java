/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench;

import java.util.HashMap;
import java.util.Map;

/**
 * The event builder class
 */
public class UIEventBuilder {
	private String topic;
	private Map<String, Object> params;

	/**
	 * @param topic
	 *            to broadcast
	 */
	public UIEventBuilder(String topic) {
		this.topic = topic;
		params = new HashMap<String, Object>();
	}

	/**
	 * Factory method that creates the new builder with given topic
	 * 
	 * @param topic
	 *            to broadcast
	 * @return new instance of builder
	 */
	public static UIEventBuilder createEvent(String topic) {
		return new UIEventBuilder(topic);
	}

	/**
	 * @param name
	 *            of parameter
	 * @param value
	 *            of parameter
	 * @return instance of builder
	 */
	public UIEventBuilder withParam(String name, Object value) {
		params.put(name, value);
		return this;
	}

	/**
	 * @return topic to broadcast
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @return parameters for topic
	 */
	public Map<String, Object> getParams() {
		return params;
	}
}
