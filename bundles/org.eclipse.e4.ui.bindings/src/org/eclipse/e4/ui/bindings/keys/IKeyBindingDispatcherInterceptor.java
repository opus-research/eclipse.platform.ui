package org.eclipse.e4.ui.bindings.keys;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.widgets.Event;

/**
 * This interface is meant to be used to allow users to know about what the
 * KeyBindingDispatcher will handle and possibly act upon that.
 *
 * @since 0.13
 */
public interface IKeyBindingDispatcherInterceptor {

	/**
	 * @param parameterizedCommand
	 *            the command to check for interception.
	 * @param event
	 *            the event that triggered the command.
	 * @return true if the command should be intercepted (i.e.: not executed),
	 *         in which case all handling by the KeyBindingDispatcher will be
	 *         stopped and false otherwise.
	 */
	boolean interceptExecutePerfectMatch(ParameterizedCommand parameterizedCommand, Event event);

	/**
	 * Called after a given command is executed.
	 *
	 * @param trigger
	 *            the event that triggered the command.
	 * @param parameterizedCommand
	 *            the command that was executed.
	 * @param commandDefined
	 *            whether the command was actually defined.
	 * @param commandHandled
	 *            whether the command was actually handled.
	 */
	void afterCommandExecuted(ParameterizedCommand parameterizedCommand, Event trigger, boolean commandDefined,
			boolean commandHandled);

}
