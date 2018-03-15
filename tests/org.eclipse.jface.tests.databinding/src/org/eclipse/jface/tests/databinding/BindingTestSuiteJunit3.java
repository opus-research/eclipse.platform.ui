/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 137877, 152543, 152540, 116920, 164247, 164653,
 *                     159768, 170848, 147515
 *     Bob Smith - bug 198880
 *     Ashley Cambrell - bugs 198903, 198904
 *     Matthew Hall - bugs 210115, 212468, 212223, 206839, 208858, 208322,
 *                    212518, 215531, 221351, 184830, 213145, 218269, 239015,
 *                    237703, 237718, 222289, 247394, 233306, 247647, 254524,
 *                    246103, 249992, 256150, 256543, 262269, 175735, 262946,
 *                    255734, 263693, 169876, 266038, 268336, 270461, 271720,
 *                    283204, 281723, 283428
 *     Ovidio Mallo - bugs 237163, 235195, 299619, 306611, 305367
 *     Eugen Neufeld - bug 461560
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 492268
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import org.eclipse.core.tests.databinding.observable.AbstractObservableTest;
import org.eclipse.core.tests.databinding.observable.list.AbstractObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.ComputedListTest;
import org.eclipse.core.tests.databinding.observable.list.MultiListTest;
import org.eclipse.core.tests.databinding.observable.list.ObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.WritableListTest;
import org.eclipse.core.tests.databinding.observable.set.ComputedSetTest;
import org.eclipse.core.tests.databinding.observable.set.WritableSetTest;
import org.eclipse.core.tests.databinding.observable.value.WritableValueTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanObservableListDecoratorTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableArrayBasedListTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableArrayBasedSetTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableSetTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.ConstantObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.DelayedObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.MapEntryObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.ListDetailValueObservableListTest;
import org.eclipse.jface.tests.internal.databinding.swt.ButtonObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CLabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.swt.GroupObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.LabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.SWTDelayedObservableValueDecoratorTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.ShellObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.swt.TableSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextEditableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ViewerInputObservableValueTest;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import junit.framework.TestSuite;

@RunWith(AllTests.class)
public class BindingTestSuiteJunit3 {

	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite("Contract Tests");
		suite.addTest(JavaBeanObservableArrayBasedSetTest.suite());
		suite.addTest(JavaBeanObservableArrayBasedListTest.suite());
		suite.addTest(AbstractObservableListTest.suite());
		suite.addTest(AbstractObservableTest.suite());
		suite.addTest(BeanObservableListDecoratorTest.suite());
		suite.addTest(ButtonObservableValueTest.suite());
		suite.addTest(CComboObservableValueSelectionTest.suite());
		suite.addTest(CComboObservableValueTextTest.suite());
		suite.addTest(CComboSingleSelectionObservableValueTest.suite());
		suite.addTest(CLabelObservableValueTest.suite());
		suite.addTest(ComboObservableValueSelectionTest.suite());
		suite.addTest(ComboObservableValueTextTest.suite());
		suite.addTest(ComputedListTest.suite());
		suite.addTest(ComputedSetTest.suite());
		suite.addTest(ConstantObservableValueTest.suite());
		suite.addTest(DelayedObservableValueTest.suite());
		suite.addTest(DetailObservableListTest.suite());
		suite.addTest(DetailObservableSetTest.suite());
		suite.addTest(DetailObservableValueTest.suite());
		suite.addTest(GroupObservableValueTest.suite());
		suite.addTest(JavaBeanObservableSetTest.suite());
		suite.addTest(JavaBeanObservableValueTest.suite());
		suite.addTest(LabelObservableValueTest.suite());
		suite.addTest(ListDetailValueObservableListTest.suite());
		suite.addTest(MapEntryObservableValueTest.suite());
		suite.addTest(MultiListTest.suite());
		suite.addTest(ObservableListTest.suite());
		suite.addTest(ScaleObservableValueMaxTest.suite());
		suite.addTest(ScaleObservableValueMinTest.suite());
		suite.addTest(ScaleObservableValueSelectionTest.suite());
		suite.addTest(ShellObservableValueTest.suite());
		suite.addTest(SpinnerObservableValueMaxTest.suite());
		suite.addTest(SpinnerObservableValueMinTest.suite());
		suite.addTest(SpinnerObservableValueSelectionTest.suite());
		suite.addTest(StyledTextObservableValueFocusOutTest.suite());
		suite.addTest(SWTDelayedObservableValueDecoratorTest.suite());
		suite.addTest(TableSingleSelectionObservableValueTest.suite());
		suite.addTest(TextEditableObservableValueTest.suite());
		suite.addTest(TextObservableValueFocusOutTest.suite());
		suite.addTest(UnmodifiableObservableListTest.suite());
		suite.addTest(UnmodifiableObservableSetTest.suite());
		suite.addTest(UnmodifiableObservableValueTest.suite());
		suite.addTest(ValidatedObservableValueTest.suite());
		suite.addTest(ViewerInputObservableValueTest.suite());
		suite.addTest(WritableListTest.suite());
		suite.addTest(WritableSetTest.suite());
		suite.addTest(WritableValueTest.suite());
		return suite;
	}
}