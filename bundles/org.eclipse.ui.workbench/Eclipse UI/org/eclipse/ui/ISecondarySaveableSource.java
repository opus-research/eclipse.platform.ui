package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface for parts providing an adapter to {@link ISaveablePart} objects
 * created or managed originally by other parts.
 * <p>
 * In case the same {@link ISaveablePart} object is created originally by a
 * "primary" part and managed by multiple parts, the "primary" part might want
 * be the only UI element showing the "dirty" state.
 * <p>
 * This interface allows "primary" parts define the default behavior for all
 * "secondary" parts; and allows "secondary" parts to override this and decide
 * how they should be represented in the UI.
 * <p>
 * <li>Parts implementing this interface directly are considered to be
 * "secondary" parts and define only their own state.
 * <li>Parts can also provide an adapter to this interface via
 * {@link IAdaptable#getAdapter(Class)}. If such part is not implementing this
 * interface directly, it is considered as primary "source" part, and can define
 * a default behavior of all secondary parts.
 * <p>
 * Per default, dirty state of "secondary" parts is not indicated in the UI.
 *
 * @since 3.108
 */
public interface ISecondarySaveableSource {

	/**
	 * Whether the dirty state indication should be added to the UI presentation
	 * of the adapted part if the adapter directly implements
	 * {@link ISecondarySaveableSource}. If the adapter is not implementing
	 * {@link ISecondarySaveableSource}, return value defines the default
	 * behavior of "secondary" parts connected to current one.
	 *
	 * @return default implementation returns {@code false}
	 */
	default boolean isDirtyStateIndicationSupported() {
		return false;
	}
}
