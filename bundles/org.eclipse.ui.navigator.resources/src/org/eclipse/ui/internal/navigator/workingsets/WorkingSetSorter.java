/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - [266030] Allow "others" working set
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.workingsets;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @since 3.2
 *
 */
public class WorkingSetSorter extends ViewerSorter {

	private static final boolean DISABLE_FIX_FOR_364735 = Boolean.getBoolean("eclipse.disable.fix.for.bug364735"); //$NON-NLS-1$

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		// Make the other working set the last one in the explorer
		if (e1 == WorkingSetsContentProvider.OTHERS_WORKING_SET) {
			return +1;
		} else if (e2 == WorkingSetsContentProvider.OTHERS_WORKING_SET) {
			return -1;
		}
		if(viewer instanceof StructuredViewer) {
			ILabelProvider labelProvider = (ILabelProvider) ((StructuredViewer) viewer).getLabelProvider();

			if (labelProvider instanceof DecoratingStyledCellLabelProvider && !DISABLE_FIX_FOR_364735) {
				// Bug 512637: use the real label provider to avoid unstable
				// sort behavior if the decoration is running while sorting.
				// decorations are usually visual aids to the user and
				// shouldn't be used in ordering.
				DecoratingStyledCellLabelProvider dprov = (DecoratingStyledCellLabelProvider) labelProvider;
				IStyledLabelProvider styledLabelProvider = dprov.getStyledStringProvider();
				String text1 = styledLabelProvider.getStyledText(e1).getString();
				String text2 = styledLabelProvider.getStyledText(e2).getString();
				if (text1 != null) {
					return text1.compareTo(text2);
				}
				return -1;
			}

			if (labelProvider instanceof DecoratingLabelProvider && !DISABLE_FIX_FOR_364735) {
				// Bug 364735: use the real label provider to avoid unstable
				// sort behavior if the decoration is running while sorting.
				// decorations are usually visual aids to the user and
				// shouldn't be used in ordering.
				DecoratingLabelProvider dprov = (DecoratingLabelProvider) labelProvider;
				labelProvider = dprov.getLabelProvider();
			}

			String text1 = labelProvider.getText(e1);
			String text2 = labelProvider.getText(e2);
			if (text1 != null) {
				return text1.compareTo(text2);
			}
		}
		return -1;
	}

}
