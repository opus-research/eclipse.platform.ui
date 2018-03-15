/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Simon Scholz <simon.scholz@vogella.com> - initial API and
 * implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Handler, which enables a full screen mode.
 *
 * @since 3.5
 *
 */
public class FullScreenHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		shell.setFullScreen(!shell.getFullScreen());

		if (shell.getFullScreen()) {
			FullScreenInfoPopup fullScreenInfoPopup = new FullScreenInfoPopup(shell, PopupDialog.HOVER_SHELLSTYLE, true,
					false, false, false, false, null, null);
			fullScreenInfoPopup.open();
		}
		return Status.OK_STATUS;
	}

	private static class FullScreenInfoPopup extends PopupDialog {

		public FullScreenInfoPopup(Shell parent, int shellStyle, boolean takeFocusOnOpen, boolean persistSize,
				boolean persistLocation, boolean showDialogMenu, boolean showPersistActions, String titleText,
				String infoText) {
			super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions,
					titleText, infoText);
		}

		@Override
		protected Point getInitialLocation(Point initialSize) {
			return super.getInitialLocation(initialSize);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			Link link = new Link(composite, SWT.BORDER);
			link.setText(WorkbenchMessages.ToggleFullScreenMode_ActivationPopup_Description);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					close();
				}
			});
			GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
			gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
			link.setLayoutData(gd);

			return composite;
		}

	}

}
