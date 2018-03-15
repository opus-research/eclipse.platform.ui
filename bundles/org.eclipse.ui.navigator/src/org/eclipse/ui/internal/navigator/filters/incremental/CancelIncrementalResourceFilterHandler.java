package org.eclipse.ui.internal.navigator.filters.incremental;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This handler checks if the active part's {@link IStatusLineManager} contains
 * the {@link IncrementalFilterContribution}. If this is the case, the
 * contribution is removed and the part's common viewer is not filtered any more
 * by the contribution.
 *
 * @author Stefan Winkler <stefan@winklerweb.net>
 * @since 3.3
 */
public class CancelIncrementalResourceFilterHandler extends AbstractHandler implements IHandler {

	/**
	 * Dectivate/cancel the incremental filter text field in the status line for
	 * the currently active {@link IViewPart}. If there is no such contribution
	 * in the status line, this handler does nothing.
	 *
	 * @param event
	 *            the execution event
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IViewPart) {
			IViewSite viewSite = ((IViewPart) activePart).getViewSite();
			IStatusLineManager statusLineManager = viewSite.getActionBars().getStatusLineManager();
			IncrementalFilterContribution.remove(statusLineManager);
		}

		return null;
	}
}
