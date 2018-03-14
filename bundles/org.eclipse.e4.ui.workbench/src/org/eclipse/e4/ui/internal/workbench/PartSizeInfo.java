/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;

/**
 * This class parses a string for size information. The size can be expressed as relative (50) or
 * absolute (50px). <br />
 * <br />
 * Resize Mode can be specified as fixed or weighted. <br />
 * Multiple parameters must separated with semicolons. Parameters may be specified in any order.
 * 
 */
public class PartSizeInfo {

	/**
	 *
	 */
	public enum PartResizeMode {
		/**
		 * The part will not adjust its size when its parent resizes
		 */
		FIXED,
		/**
		 * The part will adjust its size relative to other sibling weights when the parent resizes
		 */
		WEIGHTED
	}

	//private static final String PCT = "%"; //$NON-NLS-1$
	private static final String PX = "px"; //$NON-NLS-1$
	static final Pattern patternSizeValue = Pattern.compile("(\\d+\\.?\\d*)(px|%)?"); //$NON-NLS-1$
	static final Pattern patternSplit = Pattern.compile("\\s*;[;\\s]*"); //$NON-NLS-1$

	// private Double maxValue;
	// private Boolean bMaxAbsolute;
	// private Double minValue;
	// private Boolean bMinAbsolute;

	private Double defValue;
	private Boolean bDefAbsolute;
	PartResizeMode resizeMode = PartResizeMode.WEIGHTED;

	/**
	 * @param sizeMode
	 *            the sizeMode to set
	 */
	public void setResizeMode(PartResizeMode sizeMode) {
		this.resizeMode = sizeMode;
	}

	/**
	 * @param containerData
	 * @return PartSizeInfo for the container data
	 */
	public static PartSizeInfo parse(String containerData) {
		PartSizeInfo ret = new PartSizeInfo();
		if (containerData == null) {
			containerData = ""; //$NON-NLS-1$
		}
		String[] pairs = patternSplit.split(containerData);
		for (String pair : pairs) {
			if (pair.equals("fixed")) { //$NON-NLS-1$
				ret.resizeMode = PartResizeMode.FIXED;
			} else if (pair.equals("weighted")) { //$NON-NLS-1$
				ret.resizeMode = PartResizeMode.WEIGHTED;
				//			} else if (pair.startsWith("min:")) { //$NON-NLS-1$
				// Matcher matcher = PartSizeInfo.patternSizeValue.matcher(pair.substring(4));
				// if (matcher.matches()) {
				// ret.minValue = Double.parseDouble(matcher.group(1));
				// ret.bMinAbsolute = PX.equals(matcher.group(2));
				// }
				//			} else if (pair.startsWith("max:")) { //$NON-NLS-1$
				// Matcher matcher = PartSizeInfo.patternSizeValue.matcher(pair.substring(4));
				// if (matcher.matches()) {
				// ret.maxValue = Double.parseDouble(matcher.group(1));
				// ret.bMaxAbsolute = PX.equals(matcher.group(2));
				// }
				//			} else if (pair.startsWith("fixed:")) { //$NON-NLS-1$
				// Matcher matcher = PartSizeInfo.patternSizeValue.matcher(pair.substring(6));
				// if (matcher.matches()) {
				// ret.defValue = Double.parseDouble(matcher.group(1));
				// ret.maxValue = ret.defValue;
				// ret.minValue = ret.defValue;
				// ret.bDefAbsolute = PX.equals(matcher.group(2));
				// ret.bMaxAbsolute = ret.bDefAbsolute;
				// ret.bMinAbsolute = ret.bDefAbsolute;
				// }
			} else {
				//				if (pair.startsWith("size:")) { //$NON-NLS-1$
				// pair = pair.substring(5);
				// }
				Matcher matcher = PartSizeInfo.patternSizeValue.matcher(pair);
				if (matcher.matches()) {
					ret.defValue = Double.parseDouble(matcher.group(1));
					ret.bDefAbsolute = PX.equals(matcher.group(2));
				}
			}
		}
		if (ret.defValue == null) {
			ret.bDefAbsolute = false;
			// TODO use constant
			ret.defValue = 10000.0;
		}
		return ret;
	}

	/**
	 * @param totalRelative
	 *            Total weight of all visible relative values.
	 * @param availableRelative
	 *            The total available space available to distribute among relative values. Sash
	 *            sizes must NOT be included.
	 * @return The size constrained to min and max parameters, if specified.
	 */
	public Double getValueConstrained(double totalRelative, double availableRelative) {
		// get value
		return getDefaultValue();
		/*
		 * Double ret = getDefaultValue(); if (ret != null) { if (getMinValue() != null) { Double
		 * newValue = getMinValue(); if (availableRelative > 0 && bMinAbsolute != bDefAbsolute) { if
		 * (isMinAbsolute()) { // convert to relative newValue = (newValue / availableRelative *
		 * totalRelative); } else { // convert to absolute newValue = (newValue * 100 /
		 * availableRelative); } } if (newValue > ret) { return newValue; } } if (getMaxValue() !=
		 * null) { Double newValue = getMaxValue(); if (availableRelative > 0 && bMaxAbsolute !=
		 * bDefAbsolute) { if (isMaxAbsolute()) { // convert to relative newValue = (newValue /
		 * availableRelative * totalRelative); } else { // convert to absolute newValue = (newValue
		 * * 100 / availableRelative); } } if (newValue < ret) { return newValue; } } return ret; }
		 * 
		 * // no default value specified ret = getMinValue(); if (ret != null) { // TODO convert if
		 * (bMinAbsolute != bDefAbsolute) { } return ret; } ret = getMaxValue(); if (ret != null) {
		 * // TODO convert if (bMaxAbsolute != bDefAbsolute) { } return ret; } return 0.0;
		 */
	}

	/**
	 * @return A string representation of the SizeInfo used to persist the values.
	 */
	public String getEncodedParameters() {
		String val = ""; //$NON-NLS-1$
		val += resizeMode == PartResizeMode.FIXED ? "fixed;" : "weighted;"; //$NON-NLS-1$ //$NON-NLS-2$
		if (defValue != null) {
			val += defValue;
			val += bDefAbsolute ? PX : ""; //$NON-NLS-1$
		}
		// if (minValue != null) {
		// if (!val.isEmpty()) {
		//				val += ";"; //$NON-NLS-1$
		// }
		//			val += "min:"; //$NON-NLS-1$
		// val += minValue;
		//			val += bMinAbsolute ? PX : ""; //$NON-NLS-1$
		// }
		// if (maxValue != null) {
		// if (!val.isEmpty()) {
		//				val += ";"; //$NON-NLS-1$
		// }
		//			val += "max:"; //$NON-NLS-1$
		// val += maxValue;
		//			val += bMaxAbsolute ? PX : ""; //$NON-NLS-1$
		// }
		return val;
	}

	/**
	 * @param totalRelative
	 * @param availableRelative
	 * @return The minimum value in pixels
	 * 
	 */
	// public double getMinValueAsAbsolute(double totalRelative, double availableRelative) {
	// if (getMinValue() == null) {
	// return 0.0;
	// } else if (isMinAbsolute()) {
	// return getMinValue();
	// } else {
	// return getMinValue() / totalRelative * availableRelative;
	// }
	//
	// }

	/**
	 * @param totalRelative
	 * @param availableRelative
	 * @return The maximum value in pixels
	 * 
	 */
	// public double getMaxValueAsAbsolute(double totalRelative, double availableRelative) {
	// if (getMaxValue() == null) {
	// return Double.MAX_VALUE;
	// } else if (isMaxAbsolute()) {
	// return getMaxValue();
	// } else {
	// return getMaxValue() / totalRelative * availableRelative;
	// }
	// }

	/**
	 * @param totalRelative
	 * @param availableRelative
	 * @return The default value in pixels
	 */
	public double getValueAsAbsolute(double totalRelative, double availableRelative) {
		if (getDefaultValue() == null) {
			return 10000.0;
		} else if (isDefaultAbsolute()) {
			return getDefaultValue();
		} else {
			return getDefaultValue() / totalRelative * availableRelative;
		}
	}

	/**
	 * @param bIsAbsolute
	 */
	public void setDefaultAbsolute(boolean bIsAbsolute) {
		bDefAbsolute = bIsAbsolute;
	}

	/**
	 * @param value
	 */
	public void setDefaultValue(double value) {
		defValue = value;
	}

	/**
	 * @param value
	 */
	// public void setMaxValue(Double value) {
	// maxValue = value;
	// }

	/**
	 * @param value
	 */
	// public void setMinValue(Double value) {
	// minValue = value;
	// }

	/**
	 * @return the minValue
	 */
	// public Double getMinValue() {
	// return minValue;
	// }

	/**
	 * @return the bMinAbsolute
	 */
	// public Boolean isMinAbsolute() {
	// return bMinAbsolute;
	// }

	/**
	 * @return the maxValue
	 */
	// public Double getMaxValue() {
	// return maxValue;
	// }

	/**
	 * @return the bMaxAbsolute
	 */
	// public Boolean isMaxAbsolute() {
	// return bMaxAbsolute;
	// }

	/**
	 * @return the defValue
	 */
	public Double getDefaultValue() {
		return defValue;
	}

	/**
	 * @return the bDefAbsolute
	 */
	public Boolean isDefaultAbsolute() {
		return bDefAbsolute;
	}

	/**
	 * @return the sizeMode
	 */
	public PartResizeMode getResizeMode() {
		return resizeMode;
	}

	/**
	 * @param element
	 *            The MUIElement containing the container data to be adjusted
	 * @param size
	 *            The size of the parent in pixels
	 * 
	 */
	public void convertToWeighted(MUIElement element, int size) {
		MUIElement sashContainer = element;
		while (sashContainer != null && sashContainer instanceof MPartSashContainer == false) {
			sashContainer = sashContainer.getParent();
		}
		if (sashContainer == null) {
			return;
		}
		List<MUIElement> children = SashUtil.getVisibleChildren((MGenericTile<?>) sashContainer);
		Double pixels = getDefaultValue();
		setResizeMode(PartResizeMode.WEIGHTED);
		setDefaultValue(SashUtil.fixed2Weighted(pixels, children, (double) size, 4.0));
		// do this AFTER calculations
		setDefaultAbsolute(false);
	}
}
