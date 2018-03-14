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

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses a string for size information. The items can be expressed
 * as relative sizes (50 or 50%) or absolute sizes (50px). <br />
 * <br />
 * The default value can be specified with the prefix default: or no prefix at
 * all.<br />
 * <br />
 * Minimum and maximum values can be specified with min: or max: prefix. <br />
 * <br />
 * Fixed values can be specified with fixed. Internal this is the same as
 * setting default, min, and max to the same value. <br />
 * <br />
 * Multiple parameters must separated with semicolons. Parameters may be
 * specified in any order.
 * 
 */
public class SizeInfo {

	private static final String PCT = "%"; //$NON-NLS-1$
	private static final String PX = "px"; //$NON-NLS-1$
	static final Pattern patternSizeValue = Pattern
			.compile("(\\d+\\.?\\d*)(px|%)?"); //$NON-NLS-1$
	static final Pattern patternSplit = Pattern.compile("\\s*;[;\\s]*"); //$NON-NLS-1$

	private Double maxValue;
	private Boolean bMaxAbsolute;
	private Double defValue;
	private Boolean bDefAbsolute;
	private Double minValue;

	/**
	 * @param containerData
	 * @return SizeInfo for the container data
	 */
	public static SizeInfo parse(String containerData) {
		SizeInfo ret = new SizeInfo();
		if (containerData == null) {
			containerData = ""; //$NON-NLS-1$
		}
		String[] pairs = patternSplit.split(containerData);
		for (String pair : pairs) {
			if (pair.startsWith("min:")) { //$NON-NLS-1$
				Matcher matcher = SizeInfo.patternSizeValue.matcher(pair
						.substring(4));
				if (matcher.matches()) {
					ret.minValue = Double.parseDouble(matcher.group(1));
					ret.bMinAbsolute = PX.equals(matcher.group(2));
				}
			} else if (pair.startsWith("max:")) { //$NON-NLS-1$
				Matcher matcher = SizeInfo.patternSizeValue.matcher(pair
						.substring(4));
				if (matcher.matches()) {
					ret.maxValue = Double.parseDouble(matcher.group(1));
					ret.bMaxAbsolute = PX.equals(matcher.group(2));
				}
			} else if (pair.startsWith("fixed:")) { //$NON-NLS-1$
				Matcher matcher = SizeInfo.patternSizeValue.matcher(pair
						.substring(6));
				if (matcher.matches()) {
					ret.defValue = Double.parseDouble(matcher.group(1));
					ret.maxValue = ret.defValue;
					ret.minValue = ret.defValue;
					ret.bDefAbsolute = PX.equals(matcher.group(2));
					ret.bMaxAbsolute = ret.bDefAbsolute;
					ret.bMinAbsolute = ret.bDefAbsolute;
				}
			} else {
				if (pair.startsWith("size:")) { //$NON-NLS-1$
					pair = pair.substring(5);
				}
				Matcher matcher = SizeInfo.patternSizeValue.matcher(pair);
				if (matcher.matches()) {
					ret.defValue = Double.parseDouble(matcher.group(1));
					ret.bDefAbsolute = PX.equals(matcher.group(2));
				}
			}
		}
		if (ret.defValue == null) {
			ret.bDefAbsolute = false;
			ret.defValue = 100.0;
		}
		return ret;
	}

	/**
	 * @param totalRelative
	 *            Total weight of all visible relative values.
	 * @param availableRelative
	 *            The total available space available to distribute among
	 *            relative values. Sash sizes must NOT be included.
	 * @return The size constrained to min and max parameters, if specified.
	 */
	public Double getValueConstrained(double totalRelative,
			double availableRelative) {
		// get value
		Double ret = getDefaultValue();
		if (ret != null) {
			if (getMinValue() != null) {
				Double newValue = getMinValue();
				if (availableRelative > 0 && bMinAbsolute != bDefAbsolute) {
					if (isMinAbsolute()) {
						// convert to relative
						newValue = (newValue / availableRelative * totalRelative);
					} else {
						// convert to absolute
						newValue = (newValue * 100 / availableRelative);
					}
				}
				if (newValue > ret) {
					return newValue;
				}
			}
			if (getMaxValue() != null) {
				Double newValue = getMaxValue();
				if (availableRelative > 0 && bMaxAbsolute != bDefAbsolute) {
					if (isMaxAbsolute()) {
						// convert to relative
						newValue = (newValue / availableRelative * totalRelative);
					} else {
						// convert to absolute
						newValue = (newValue * 100 / availableRelative);
					}
				}
				if (newValue < ret) {
					return newValue;
				}
			}
			return ret;
		}

		// no default value specified
		ret = getMinValue();
		if (ret != null) {
			// TODO convert
			if (bMinAbsolute != bDefAbsolute) {
			}
			return ret;
		}
		ret = getMaxValue();
		if (ret != null) {
			// TODO convert
			if (bMaxAbsolute != bDefAbsolute) {
			}
			return ret;
		}
		return 0.0;
	}

	/**
	 * @return A string representation of the SizeInfo used to persist the
	 *         values.
	 */
	public String getEncodedParameters() {
		String val = ""; //$NON-NLS-1$
		if (defValue != null) {
			val += defValue;
			val += bDefAbsolute ? PX : PCT;
		}
		if (minValue != null) {
			if (!val.isEmpty()) {
				val += ";"; //$NON-NLS-1$
			}
			val += "min:"; //$NON-NLS-1$
			val += minValue;
			val += bMinAbsolute ? PX : PCT;
		}
		if (maxValue != null) {
			if (!val.isEmpty()) {
				val += ";"; //$NON-NLS-1$
			}
			val += "max:"; //$NON-NLS-1$
			val += maxValue;
			val += bMaxAbsolute ? PX : PCT;
		}
		return val;
	}

	/**
	 * @param totalRelative
	 * @param availableRelative
	 * @return The minimum value in pixels
	 * 
	 */
	public double getMinValueAsAbsolute(double totalRelative,
			double availableRelative) {
		if (getMinValue() == null) {
			return 0.0;
		} else if (isMinAbsolute()) {
			return getMinValue();
		} else {
			return getMinValue() / totalRelative * availableRelative;
		}

	}

	/**
	 * @param totalRelative
	 * @param availableRelative
	 * @return The maximum value in pixels
	 * 
	 */
	public double getMaxValueAsAbsolute(double totalRelative,
			double availableRelative) {
		if (getMaxValue() == null) {
			return Double.MAX_VALUE;
		} else if (isMaxAbsolute()) {
			return getMaxValue();
		} else {
			return getMaxValue() / totalRelative * availableRelative;
		}
	}

	/**
	 * @param totalRelative
	 * @param availableRelative
	 * @return The default value in pixels
	 */
	public double getValueAsAbsolute(double totalRelative,
			double availableRelative) {
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
	public void setMaxValue(Double value) {
		maxValue = value;
	}

	/**
	 * @param value
	 */
	public void setMinValue(Double value) {
		minValue = value;
	}

	/**
	 * @return the minValue
	 */
	public Double getMinValue() {
		return minValue;
	}

	private Boolean bMinAbsolute;

	/**
	 * @return the bMinAbsolute
	 */
	public Boolean isMinAbsolute() {
		return bMinAbsolute;
	}

	/**
	 * @return the maxValue
	 */
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * @return the bMaxAbsolute
	 */
	public Boolean isMaxAbsolute() {
		return bMaxAbsolute;
	}

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

}
