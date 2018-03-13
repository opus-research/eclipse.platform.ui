/*******************************************************************************
 * Copyright (c) 2013 Hendrik Still and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hendrik Still<hendrik.still@gammas.de> - initial implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * This is the generic version of the TestModelContentProvider class
 * @see org.eclipse.jface.tests.viewers.TestModelContentProvider
 */
public class TestModelContentProviderGenerics implements ITestModelListener,
        IStructuredContentProvider<TestElement,TestElement>{
    Viewer<TestElement> fViewer;

    public void dispose() {
    }

    protected void doInsert(TestModelChange change) {
        if (fViewer instanceof ListViewer) {
            if (change.getParent() != null
                    && change.getParent().equals(fViewer.getInput())) {
                ((ListViewer) fViewer).add(change.getChildren());
            }
        } else if (fViewer instanceof TableViewer) {
            if (change.getParent() != null
                    && change.getParent().equals(fViewer.getInput())) {
                ((TableViewer) fViewer).add(change.getChildren());
            }
        } else if (fViewer instanceof AbstractTreeViewer) {
            ((AbstractTreeViewer) fViewer).add(change.getParent(), change
                    .getChildren());
        } else if (fViewer instanceof ComboViewer) {
            ((ComboViewer) fViewer).add(change.getChildren());
        } else {
            Assert.isTrue(false, "Unknown kind of viewer");
        }
    }

    protected void doNonStructureChange(TestModelChange change) {
        if (fViewer instanceof StructuredViewer) {
            ((StructuredViewer<TestElement,TestElement>) fViewer).update(change.getParent(),
                    new String[] { IBasicPropertyConstants.P_TEXT });
        } else {
            Assert.isTrue(false, "Unknown kind of viewer");
        }
    }

    protected void doRemove(TestModelChange change) {
        if (fViewer instanceof ListViewer) {
            ((ListViewer) fViewer).remove(change.getChildren());
        } else if (fViewer instanceof TableViewer) {
            ((TableViewer) fViewer).remove(change.getChildren());
        } else if (fViewer instanceof AbstractTreeViewer) {
            ((AbstractTreeViewer) fViewer).remove(change.getChildren());
        } else if (fViewer instanceof ComboViewer) {
            ((ComboViewer<TestElement,TestElement>) fViewer).remove(change.getChildren());
        } else {
            Assert.isTrue(false, "Unknown kind of viewer");
        }
    }

    protected void doStructureChange(TestModelChange change) {
        if (fViewer instanceof StructuredViewer) {
            ((StructuredViewer<TestElement,TestElement>) fViewer).refresh(change.getParent());
        } else {
            Assert.isTrue(false, "Unknown kind of viewer");
        }
    }

    public TestElement[] getChildren(TestElement testElement) {
        int count = testElement.getChildCount();
        TestElement[] children = new TestElement[count];
        for (int i = 0; i < count; ++i)
            children[i] = testElement.getChildAt(i);
        return children;
    }


    public Object getParent(Object element) {
        return ((TestElement) element).getContainer();
    }

    public boolean hasChildren(Object element) {
        return ((TestElement) element).getChildCount() > 0;
    }


    public boolean isDeleted(Object element) {
        return ((TestElement) element).isDeleted();
    }

    public void testModelChanged(TestModelChange change) {
        switch (change.getKind()) {
        case TestModelChange.INSERT:
            doInsert(change);
            break;
        case TestModelChange.REMOVE:
            doRemove(change);
            break;
        case TestModelChange.STRUCTURE_CHANGE:
            doStructureChange(change);
            break;
        case TestModelChange.NON_STRUCTURE_CHANGE:
            doNonStructureChange(change);
            break;
        default:
            throw new IllegalArgumentException("Unknown kind of change");
        }

        StructuredSelection selection = new StructuredSelection(change
                .getChildren());
        if ((change.getModifiers() & TestModelChange.SELECT) != 0) {
            ((StructuredViewer<TestElement,TestElement>) fViewer).setSelection(selection);
        }
        if ((change.getModifiers() & TestModelChange.REVEAL) != 0) {
            Object element = selection.getFirstElement();
            if (element != null) {
                ((StructuredViewer<TestElement,TestElement>) fViewer).reveal(element);
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer<TestElement> viewer, TestElement oldInput,
			TestElement newInput) {
        fViewer = viewer;
        TestElement oldElement = oldInput;
        if (oldElement != null) {
            oldElement.getModel().removeListener(this);
        }
        TestElement newElement = newInput;
        if (newElement != null) {
            newElement.getModel().addListener(this);
        }
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public TestElement[] getElements(TestElement inputElement) {
		return getChildren(inputElement);
	}
}
