/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joseph Carroll <jdsalingerjr@gmail.com> - Bug 385414 Contributing wizards to toolbar always displays icon and text
 *     Snjezana Peco <snjezana.peco@redhat.com> - Memory leaks in Juno when opening and closing XML Editor - http://bugs.eclipse.org/397909
 *     Marco Descher <marco@descher.at> - Bug 397677
 *     Dmitry Spiridenok - Bug 429756
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445723, 450863
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 461026
 *     Jonas Helming - Bug 410087
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.help.EHelpService;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A contribution item for handled contributions (with a command)
 */
public class HandledContributionItem extends AbstractContributionItem {
	/**
	 * Constant from org.eclipse.ui.handlers.RadioState.PARAMETER_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE_PARAMETER = "org.eclipse.ui.commands.radioStateParameter"; //$NON-NLS-1$

	/**
	 * Constant from org.eclipse.ui.handlers.RadioState.STATE_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE = "org.eclipse.ui.commands.radioState"; //$NON-NLS-1$

	/**
	 * Constant from org.eclipse.ui.handlers.RegistryToggleState.STATE_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_TOGGLE_STATE = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private static final String WW_SUPPORT = "org.eclipse.ui.IWorkbenchWindow"; //$NON-NLS-1$
	private static final String HCI_STATIC_CONTEXT = "HCI-staticContext"; //$NON-NLS-1$
	private MHandledItem handledModelItem;


	// We'll only ever log an error during update once to prevent spamming the
	// log
	private boolean logged = false;

	@Inject
	private ECommandService commandService;

	@Inject
	private EBindingService bindingService;

	@Inject
	@Optional
	private IUpdateService updateService;

	@Inject
	@Optional
	private EHelpService helpService;

	@Inject
	@Optional
	@SuppressWarnings("restriction")
	private ICommandHelpService commandHelpService;

	private Runnable unreferenceRunnable;

	private IStateListener stateListener = new IStateListener() {
		@Override
		public void handleStateChange(State state, Object oldValue) {
			updateState();
		}
	};

	private ISafeRunnable getUpdateRunner() {
		if (updateRunner == null) {
			updateRunner = new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					boolean shouldEnable = canExecuteItem(null);
					if (shouldEnable != handledModelItem.isEnabled()) {
						handledModelItem.setEnabled(shouldEnable);
						update();
					}
				}

				@Override
				public void handleException(Throwable exception) {
					if (!logged) {
						logged = true;
						if (logger != null) {
							logger.error(
									exception,
									"Internal error during tool item enablement updating, this is only logged once per tool item."); //$NON-NLS-1$
						}
					}
				}
			};
		}
		return updateRunner;
	}

	protected void updateItemEnablement() {
		if (!(handledModelItem.getWidget() instanceof ToolItem))
			return;

		ToolItem widget = (ToolItem) handledModelItem.getWidget();
		if (widget == null || widget.isDisposed())
			return;

		SafeRunner.run(getUpdateRunner());
	}

	private ISafeRunnable updateRunner;

	private IEclipseContext infoContext;

	private State styleState;

	private State toggleState;

	private State radioState;

	/**
	 * @param item
	 */
	public void setModel(MHandledItem item) {
		handledModelItem = item;
		super.setModel(item);
		setId(handledModelItem.getElementId());
		generateCommand();
		if (handledModelItem.getCommand() == null) {
			if (logger != null) {
				logger.error("Element " + handledModelItem.getElementId() + " invalid, no command defined."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		updateVisible();
	}

	/**
	 *
	 */
	private void generateCommand() {
		if (handledModelItem.getCommand() != null && handledModelItem.getWbCommand() == null) {
			String cmdId = handledModelItem.getCommand().getElementId();
			if (cmdId == null) {
				Activator.log(IStatus.ERROR, "Unable to generate parameterized command for " + handledModelItem //$NON-NLS-1$
						+ ". ElementId is not allowed to be null."); //$NON-NLS-1$
				return;
			}
			List<MParameter> modelParms = handledModelItem.getParameters();
			Map<String, Object> parameters = new HashMap<String, Object>(4);
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
			ParameterizedCommand parmCmd = commandService.createCommand(cmdId,
					parameters);
			Activator.trace(Policy.DEBUG_MENUS, "command: " + parmCmd, null); //$NON-NLS-1$
			if (parmCmd == null) {
				Activator.log(IStatus.ERROR,
						"Unable to generate parameterized command for " + handledModelItem //$NON-NLS-1$
								+ " with " + parameters); //$NON-NLS-1$
				return;
			}

			handledModelItem.setWbCommand(parmCmd);

			styleState = parmCmd.getCommand().getState(IMenuStateIds.STYLE);
			toggleState = parmCmd.getCommand().getState(
					ORG_ECLIPSE_UI_COMMANDS_TOGGLE_STATE);
			radioState = parmCmd.getCommand().getState(
					ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE);
			updateState();

			if (styleState != null) {
				styleState.addListener(stateListener);
			} else if (toggleState != null) {
				toggleState.addListener(stateListener);
			} else if (radioState != null) {
				radioState.addListener(stateListener);
			}
		}
	}

	private void updateState() {
		if (styleState != null) {
			handledModelItem.setSelected(((Boolean) styleState.getValue()).booleanValue());
		} else if (toggleState != null) {
			handledModelItem.setSelected(((Boolean) toggleState.getValue()).booleanValue());
		} else if (radioState != null && handledModelItem.getWbCommand() != null) {
			ParameterizedCommand c = handledModelItem.getWbCommand();
			Object parameter = c.getParameterMap().get(
					ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE_PARAMETER);
			String value = (String) radioState.getValue();
			handledModelItem.setSelected(value != null && value.equals(parameter));
		}
	}

	@Override
	protected void postMenuFill() {
		if (updateService != null) {
			unreferenceRunnable = updateService.registerElementForUpdate(
					handledModelItem.getWbCommand(), handledModelItem);
		}
	}

	@Override
	public void postToolbarFill() {

		ToolItemUpdater updater = getUpdater();
		if (updater != null) {
			updater.registerItem(this);
		}

		hookCheckListener();

		if (updateService != null) {
			unreferenceRunnable = updateService.registerElementForUpdate(
					handledModelItem.getWbCommand(), handledModelItem);
		}
	}

	private void hookCheckListener() {
		if (handledModelItem.getType() != ItemType.CHECK) {
			return;
		}
		Object obj = handledModelItem.getTransientData().get(ItemType.CHECK.toString());
		if (obj instanceof IContextFunction) {
			IEclipseContext context = getContext(handledModelItem);
			IEclipseContext staticContext = getStaticContext(null);
			staticContext.set(MPart.class, context.get(MPart.class));
			staticContext.set(WW_SUPPORT, context.get(WW_SUPPORT));

			IContextFunction func = (IContextFunction) obj;
			obj = func.compute(staticContext, null);
			if (obj != null) {
				handledModelItem.getTransientData().put(DISPOSABLE, obj);
			}
		}
	}

	private void unhookCheckListener() {
		if (handledModelItem.getType() != ItemType.CHECK) {
			return;
		}
		final Object obj = handledModelItem.getTransientData().remove(DISPOSABLE);
		if (obj == null) {
			return;
		}
		((Runnable) obj).run();
	}

	@Override
	protected void updateMenuItem() {
		MenuItem item = (MenuItem) widget;
		String text = handledModelItem.getLocalizedLabel();
		ParameterizedCommand parmCmd = handledModelItem.getWbCommand();
		String keyBindingText = null;
		if (parmCmd != null) {
			if (text == null || text.isEmpty()) {
				text = handledModelItem.getCommand().getLocalizedCommandName();
			}
			if (bindingService != null) {
				TriggerSequence binding = bindingService
						.getBestSequenceFor(parmCmd);
				if (binding != null)
					keyBindingText = binding.format();
			}
		}
		if (text != null) {
			if (handledModelItem instanceof MMenuElement) {
				String mnemonics = ((MMenuElement) handledModelItem).getMnemonics();
				if (mnemonics != null && !mnemonics.isEmpty()) {
					int idx = text.indexOf(mnemonics);
					if (idx != -1) {
						text = text.substring(0, idx) + '&'
								+ text.substring(idx);
					}
				}
			}
			if (keyBindingText == null)
				item.setText(text);
			else
				item.setText(text + '\t' + keyBindingText);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		item.setSelection(handledModelItem.isSelected());
		item.setEnabled(handledModelItem.isEnabled());
	}

	@Override
	protected void updateToolItem() {
		ToolItem item = (ToolItem) widget;

		if (item.getImage() == null || handledModelItem.getTags().contains(FORCE_TEXT)) {
			final String text = handledModelItem.getLocalizedLabel();
			if (text == null || text.length() == 0) {
				final MCommand command = handledModelItem.getCommand();
				if (command == null) {
					// Set some text so that the item stays visible in the menu
					item.setText("UnLabled"); //$NON-NLS-1$
				} else {
					item.setText(command.getLocalizedCommandName());
				}
			} else {
				item.setText(text);
			}
		} else {
			item.setText(""); //$NON-NLS-1$
		}

		final String tooltip = getToolTipText();
		item.setToolTipText(tooltip);
		item.setSelection(handledModelItem.isSelected());
		item.setEnabled(handledModelItem.isEnabled());
	}

	private String getToolTipText() {
		String text = handledModelItem.getLocalizedTooltip();
		ParameterizedCommand parmCmd = handledModelItem.getWbCommand();
		if (parmCmd == null) {
			generateCommand();
			parmCmd = handledModelItem.getWbCommand();
		}

		if (parmCmd != null && text == null) {
			try {
				text = parmCmd.getName();
			} catch (NotDefinedException e) {
				return null;
			}
		}

		TriggerSequence sequence = bindingService.getBestSequenceFor(parmCmd);
		if (sequence != null) {
			text = text + " (" + sequence.format() + ')'; //$NON-NLS-1$
		}
		return text;
	}

	@Override
	protected void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			if (unreferenceRunnable != null) {
				unreferenceRunnable.run();
				unreferenceRunnable = null;
			}
			unhookCheckListener();
			ToolItemUpdater updater = getUpdater();
			if (updater != null) {
				updater.removeItem(this);
			}
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
			widget.removeListener(SWT.Help, getItemListener());
			widget = null;
			handledModelItem.setWidget(null);
			disposeOldImages();
		}
	}

	@Override
	public void dispose() {
		if (widget != null) {
			if (unreferenceRunnable != null) {
				unreferenceRunnable.run();
				unreferenceRunnable = null;
			}

			ParameterizedCommand command = handledModelItem.getWbCommand();
			if (command != null) {
				if (styleState != null) {
					styleState.removeListener(stateListener);
					styleState = null;
				}
				if (toggleState != null) {
					toggleState.removeListener(stateListener);
					toggleState = null;
				}
				if (radioState != null) {
					radioState.removeListener(stateListener);
					radioState = null;
				}
			}
			widget.dispose();
			widget = null;
			handledModelItem.setWidget(null);
		}
	}

	@Override
	@SuppressWarnings("restriction")
	protected void handleHelpRequest() {
		MCommand command = handledModelItem.getCommand();
		if (command == null || helpService == null
				|| commandHelpService == null) {
			return;
		}

		String contextHelpId = commandHelpService.getHelpContextId(
				command.getElementId(), getContext(handledModelItem));
		if (contextHelpId != null) {
			helpService.displayHelp(contextHelpId);
		}
	}

	private IEclipseContext getStaticContext(Event event) {
		if (infoContext == null) {
			infoContext = EclipseContextFactory.create(HCI_STATIC_CONTEXT);
			ContributionsAnalyzer.populateModelInterfaces(handledModelItem, infoContext,
					handledModelItem.getClass().getInterfaces());
		}
		if (event == null) {
			infoContext.remove(Event.class);
		} else {
			infoContext.set(Event.class, event);
		}
		return infoContext;
	}

	@Override
	protected void executeItem(Event trigger) {
		ParameterizedCommand cmd = handledModelItem.getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = getContext(handledModelItem);
		EHandlerService service = (EHandlerService) lclContext
				.get(EHandlerService.class.getName());
		final IEclipseContext staticContext = getStaticContext(trigger);
		service.executeHandler(cmd, staticContext);
	}

	@Override
	protected boolean canExecuteItem(Event trigger) {
		ParameterizedCommand cmd = handledModelItem.getWbCommand();
		if (cmd == null) {
			return false;
		}
		final IEclipseContext lclContext = getContext(handledModelItem);
		EHandlerService service = lclContext.get(EHandlerService.class);
		if (service == null) {
			return false;
		}
		final IEclipseContext staticContext = getStaticContext(trigger);
		return service.canExecute(cmd, staticContext);
	}

	/**
	 * @return the model
	 */
	@Override
	public MHandledItem getModel() {
		return handledModelItem;
	}

	private ToolItemUpdater getUpdater() {
		if (handledModelItem != null) {
			Object obj = handledModelItem.getRenderer();
			if (obj instanceof ToolBarManagerRenderer) {
				return ((ToolBarManagerRenderer) obj).getUpdater();
			}
		}
		return null;
	}
}
