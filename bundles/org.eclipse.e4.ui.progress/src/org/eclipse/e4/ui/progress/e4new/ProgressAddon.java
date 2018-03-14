package org.eclipse.e4.ui.progress.e4new;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.e4.ui.progress.e4new.ExternalServices;
import org.eclipse.e4.ui.progress.internal.ProgressManager;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

public class ProgressAddon {

	@PostConstruct
	public void initializeBundle(IEclipseContext context) {
		ContextInjectionFactory.make(Preferences.class, context);
		context.set(IProgressService.class, ProgressManager.getInstance());
	}
	
	@Inject
	@Optional
	public void setEHandlerService(EHandlerService eHandlerService) {
		ExternalServices.setEHandlerService(eHandlerService);
	}

	@Inject
	@Optional
	public void setStatusReporter(StatusReporter statusReporter) {
		ExternalServices.setStatusReporter(statusReporter);
	}

	@Inject
	@Optional
	public void setModelService(EModelService modelService) {
		ExternalServices.setModelService(modelService);
	}

	@Inject
	@Optional
	public void setPartService(EPartService partService) {
		ExternalServices.setPartService(partService);
	}

	@Inject
	@Optional
	public void setMWindow(@Active MWindow window) {
		ExternalServices.setMWindow(window);
	}

	@Inject
	@Optional
	public void setMApplication(MApplication mApplication) {
		ExternalServices.setMApplication(mApplication);
	}

	@Inject
	@Optional
	public void setActiveShell(@Active Shell shell) {
		ExternalServices.setShell(shell);
		if (shell != null) {
			ExternalServices.setDisplay(shell.getDisplay());
		}
	}

	@Inject
	@Optional
	public void setUiSynchronise(@Active UISynchronize uiSynchronize) {
		ExternalServices.setUiSynchronize(uiSynchronize);
	}

	@Inject
	@Optional
	public void setLogService(LogService logService) {
		ExternalServices.setLogService(logService);
	}

}
