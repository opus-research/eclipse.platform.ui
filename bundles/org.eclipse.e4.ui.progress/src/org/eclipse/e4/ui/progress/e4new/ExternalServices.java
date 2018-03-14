package org.eclipse.e4.ui.progress.e4new;

import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

public class ExternalServices {

	private static Display display;
	private static Shell shell;
	private static UISynchronize uiSynchronize;
	private static EHandlerService eHandlerService;
	private static LogService logService;
	private static StatusReporter statusReporter;
	private static IProgressService progressService;
	private static EModelService modelService;
	private static EPartService partService;
	private static MApplication mApplication;
	private static MWindow window;


	public static synchronized void setDisplay(Display display) {
		ExternalServices.display = display;
	}

	public static synchronized Display getDisplay() {
		return display;
	}

	public static synchronized void setShell(Shell shell) {
		ExternalServices.shell = shell;
	}

	public static synchronized Shell getShell() {
		return shell;
	}

	public static synchronized UISynchronize getUiSynchronize() {
		return uiSynchronize;
	}

	public static synchronized void setUiSynchronize(UISynchronize uiSynchronize) {
		ExternalServices.uiSynchronize = uiSynchronize;
	}

	public static synchronized void setEHandlerService(EHandlerService eHandlerService) {
		ExternalServices.eHandlerService = eHandlerService;
	}

	public static synchronized EHandlerService getEHandlerService() {
		return eHandlerService;
	}

	public static synchronized void setLogService(LogService logService) {
		ExternalServices.logService = logService;
	}

	public static LogService getLogService() {
	    return logService;

    }

	public static void setStatusReporter(StatusReporter statusReporter) {
		ExternalServices.statusReporter = statusReporter;
    }

	public static StatusReporter getStatusReporter() {
	    return statusReporter;
    }

	public static IProgressService getProgressService() {
	    return progressService;
    }

	public static void setModelService(EModelService modelService) {
		ExternalServices.modelService = modelService;
    }

	public static EModelService getModelService() {
		return modelService;
	}

	public static void setPartService(EPartService partService) {
		ExternalServices.partService = partService;
	}

	public static EPartService getPartService() {
		return partService;
	}

	public static void setMWindow(MWindow window) {
		ExternalServices.window = window;
	}

	public static MWindow getMWindow() {
	    return window;
    }
	
	public static void setMApplication(MApplication mApplication) {
		ExternalServices.mApplication = mApplication;
	}
	
	public static MApplication getMApplication() {
		return mApplication;
	}


}
