package org.eclipse.ui.navigate.search;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class NextPrevSearchEntryHandler extends AbstractHandler implements
		IExecutableExtension {
	private String searchCommandString = IWorkbenchCommandConstants.NAVIGATE_NEXT;

	@SuppressWarnings("restriction")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		ECommandService cs = (ECommandService) window
				.getService(ECommandService.class);
		EHandlerService hs = (EHandlerService) window
				.getService(EHandlerService.class);

		HashMap<String, Object> parms = new HashMap<String, Object>();
		parms.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID,
				"org.eclipse.search.ui.views.SearchView");

		ParameterizedCommand switchToSearchViewCommand = cs.createCommand(
				IWorkbenchCommandConstants.VIEWS_SHOW_VIEW, parms);

		hs.executeHandler(switchToSearchViewCommand);
		ParameterizedCommand searchCommand = cs.createCommand(
				searchCommandString, null);
		hs.executeHandler(searchCommand);
		ParameterizedCommand activateEditorCommand = cs.createCommand(
				IWorkbenchCommandConstants.WINDOW_ACTIVATE_EDITOR, null);
		hs.executeHandler(activateEditorCommand);

		return null;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		if ("previous".equals(data)) {
			searchCommandString = IWorkbenchCommandConstants.NAVIGATE_PREVIOUS;
		}
	}

}