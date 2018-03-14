package org.eclipse.core.databinding.observable;

/**
 * Interface used by objects which must be explicitly disposed by their owner.
 *
 * @since 1.6
 */
public interface IDisposable {
	/**
	 * Disposes this object. Must be invoked by the object's owner when they are
	 * done with it. No further methods may be invoked on this object after
	 * calling dispose.
	 */
	void dispose();
}
