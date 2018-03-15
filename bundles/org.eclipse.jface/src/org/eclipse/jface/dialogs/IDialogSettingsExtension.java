package org.eclipse.jface.dialogs;

/**
 * Extension of the {@link IDialogSettings} interface that adds getter methods
 * to take a default value which is returned when an error occurs.
 *
 * @since 3.14
 *
 */
public interface IDialogSettingsExtension extends IDialogSettings {

	/**
	 * Returns the value for the given key in this dialog settings. If no value is
	 * associated with the key or the value is not of type double, the provided
	 * default value is returned.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the double value for the given key or the default value
	 * @since 3.14
	 *
	 */
	public double getDouble(String key, double defaultValue);

	/**
	 * Returns the value for the given key in this dialog settings. If no value is
	 * associated with the key or the value is not of type float, the provided
	 * default value is returned.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the float value for the given key or the default value
	 * @since 3.14
	 *
	 */
	public float getFloat(String key, float defaultValue);

	/**
	 * Returns the value for the given key in this dialog settings. If no value is
	 * associated with the key or the value is not of type int, the provided default
	 * value is returned.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the int value for the given key or the default value
	 * @since 3.14
	 *
	 */
	public int getInt(String key, int defaultValue);

	/**
	 * Returns the value for the given key in this dialog settings. If no value is
	 * associated with the key or the value is not of type long, the provided
	 * default value is returned.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the long value for the given key or the default value
	 * @since 3.14
	 *
	 */
	public long getLong(String key, long defaultValue);
}
