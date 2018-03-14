package org.eclipse.e4.ui.progress.e4new;

import org.eclipse.jface.action.IAction;

/**
 * Interface for a workbench action.
 */
public interface IDisposableAction extends IAction {
    /**
     * Disposes of this action. Once disposed, this action cannot be used.
     * This operation has no effect if the action has already been
     * disposed.
     */
    public void dispose();
}