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

import org.eclipse.core.tests.databinding.AggregateValidationStatusTest;
import org.eclipse.core.tests.databinding.BindingTest;
import org.eclipse.core.tests.databinding.DatabindingContextTest;
import org.eclipse.core.tests.databinding.ListBindingTest;
import org.eclipse.core.tests.databinding.ObservablesManagerTest;
import org.eclipse.core.tests.databinding.UpdateListStrategyTest;
import org.eclipse.core.tests.databinding.UpdateSetStrategyTest;
import org.eclipse.core.tests.databinding.UpdateStrategyTest;
import org.eclipse.core.tests.databinding.UpdateValueStrategyTest;
import org.eclipse.core.tests.databinding.ValueBindingTest;
import org.eclipse.core.tests.databinding.beans.AnonymousBeanValuePropertyTest;
import org.eclipse.core.tests.databinding.beans.AnonymousPojoValuePropertyTest;
import org.eclipse.core.tests.databinding.beans.BeanPropertiesTest;
import org.eclipse.core.tests.databinding.beans.BeansObservablesTest;
import org.eclipse.core.tests.databinding.beans.PojoObservablesTest;
import org.eclipse.core.tests.databinding.beans.PojoPropertiesTest;
import org.eclipse.core.tests.databinding.beans.SetOnlyJavaBeanTest;
import org.eclipse.core.tests.databinding.conversion.NumberToStringConverterTest;
import org.eclipse.core.tests.databinding.conversion.StringToNumberConverterTest;
import org.eclipse.core.tests.databinding.observable.AbstractObservableTest;
import org.eclipse.core.tests.databinding.observable.ChangeSupportTest;
import org.eclipse.core.tests.databinding.observable.DecoratingObservableTest;
import org.eclipse.core.tests.databinding.observable.DiffsTest;
import org.eclipse.core.tests.databinding.observable.Diffs_ListDiffTests;
import org.eclipse.core.tests.databinding.observable.ObservableTrackerTest;
import org.eclipse.core.tests.databinding.observable.ObservablesTest;
import org.eclipse.core.tests.databinding.observable.RealmTest;
import org.eclipse.core.tests.databinding.observable.list.AbstractObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.ComputedListTest;
import org.eclipse.core.tests.databinding.observable.list.DecoratingObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.ListDiffTest;
import org.eclipse.core.tests.databinding.observable.list.ListDiffVisitorTest;
import org.eclipse.core.tests.databinding.observable.list.MultiListTest;
import org.eclipse.core.tests.databinding.observable.list.ObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.WritableListTest;
import org.eclipse.core.tests.databinding.observable.map.AbstractObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.BidiObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.CompositeMapTest;
import org.eclipse.core.tests.databinding.observable.map.ComputedObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.ObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.WritableMapTest;
import org.eclipse.core.tests.databinding.observable.set.AbstractObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.ComputedSetTest;
import org.eclipse.core.tests.databinding.observable.set.DecoratingObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.ObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.UnionSetTest;
import org.eclipse.core.tests.databinding.observable.set.WritableSetTest;
import org.eclipse.core.tests.databinding.observable.value.AbstractObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.AbstractVetoableValueTest;
import org.eclipse.core.tests.databinding.observable.value.ComputedValueTest;
import org.eclipse.core.tests.databinding.observable.value.DateAndTimeObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.DecoratingObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.DuplexingObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.SelectObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.WritableValueTest;
import org.eclipse.core.tests.databinding.util.PolicyTest;
import org.eclipse.core.tests.databinding.validation.MultiValidatorTest;
import org.eclipse.core.tests.databinding.validation.ValidationStatusTest;
import org.eclipse.core.tests.internal.databinding.BindingMessagesTest;
import org.eclipse.core.tests.internal.databinding.BindingStatusTest;
import org.eclipse.core.tests.internal.databinding.ConverterValuePropertyTest;
import org.eclipse.core.tests.internal.databinding.DifferentRealmsBindingTest;
import org.eclipse.core.tests.internal.databinding.IdentityMapTest;
import org.eclipse.core.tests.internal.databinding.IdentitySetTest;
import org.eclipse.core.tests.internal.databinding.QueueTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanObservableListDecoratorTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanObservableSetDecoratorTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanObservableValueDecoratorTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanPropertyHelperTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanPropertyListenerSupportTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanPropertyListenerTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanValuePropertyTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableArrayBasedListTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableArrayBasedSetTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableListTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableMapTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableSetTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableValueTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanPropertyObservableMapTest;
import org.eclipse.core.tests.internal.databinding.conversion.DateConversionSupportTest;
import org.eclipse.core.tests.internal.databinding.conversion.IdentityConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.IntegerToStringConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToBigDecimalTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToBigIntegerConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToByteConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToDoubleConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToFloatConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToIntegerConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToLongConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToShortConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.ObjectToPrimitiveValidatorTest;
import org.eclipse.core.tests.internal.databinding.conversion.StatusToStringConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToBooleanConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToByteConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToCharacterConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserByteTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserDoubleTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserFloatTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserIntegerTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserLongTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserShortTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToShortConverterTest;
import org.eclipse.core.tests.internal.databinding.observable.ConstantObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.DelayedObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.EmptyObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.EmptyObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.IdentityObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.MapEntryObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.StalenessObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableMapTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.ListDetailValueObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.MapDetailValueObservableMapTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.SetDetailValueObservableMapTest;
import org.eclipse.core.tests.internal.databinding.property.value.ListSimpleValueObservableListTest;
import org.eclipse.core.tests.internal.databinding.property.value.MapSimpleValueObservableMapTest;
import org.eclipse.core.tests.internal.databinding.property.value.SetSimpleValueObservableMapTest;
import org.eclipse.core.tests.internal.databinding.validation.AbstractStringToNumberValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToByteValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToDoubleValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToFloatValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToIntegerValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToLongValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToShortValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToUnboundedNumberValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToByteValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToCharacterValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToDoubleValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToFloatValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToIntegerValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToLongValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToShortValidatorTest;
import org.eclipse.jface.tests.databinding.preference.PreferencePageSupportTest;
import org.eclipse.jface.tests.databinding.scenarios.BindingScenariosTestSuite;
import org.eclipse.jface.tests.databinding.swt.SWTObservablesTest;
import org.eclipse.jface.tests.databinding.swt.WidgetObservableThreadTest;
import org.eclipse.jface.tests.databinding.swt.WidgetPropertiesTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableListContentProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableListTreeContentProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableMapLabelProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableSetContentProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableSetTreeContentProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableValueEditingSupportTest;
import org.eclipse.jface.tests.databinding.viewers.ViewerSupportTest;
import org.eclipse.jface.tests.databinding.viewers.ViewersObservablesTest;
import org.eclipse.jface.tests.databinding.wizard.WizardPageSupportTest;
import org.eclipse.jface.tests.examples.databinding.mask.internal.EditMaskLexerAndTokenTest;
import org.eclipse.jface.tests.examples.databinding.mask.internal.EditMaskParserTest;
import org.eclipse.jface.tests.internal.databinding.swt.ButtonObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CLabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.ControlObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.DateTimeCalendarObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.DateTimeDateObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.DateTimeSelectionPropertyTest;
import org.eclipse.jface.tests.internal.databinding.swt.DateTimeTimeObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.GroupObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.LabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.ListSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.SWTDelayedObservableValueDecoratorTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.ShellObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueDefaultSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueModifyTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TableSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextEditableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueDefaultSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueModifyTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.viewers.CheckableCheckedElementsObservableSetTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ObservableCollectionContentProviderTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ObservableCollectionTreeContentProviderTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ObservableViewerElementSetTest;
import org.eclipse.jface.tests.internal.databinding.viewers.SelectionProviderMultiSelectionObservableListTest;
import org.eclipse.jface.tests.internal.databinding.viewers.SelectionProviderSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ViewerElementMapTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ViewerElementSetTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ViewerElementWrapperTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ViewerInputObservableValueTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AggregateValidationStatusTest.class, BindingTest.class, DatabindingContextTest.class,
		ListBindingTest.class, UpdateStrategyTest.class, UpdateListStrategyTest.class, UpdateSetStrategyTest.class,
		UpdateValueStrategyTest.class, ValueBindingTest.class, ObservablesManagerTest.class, PolicyTest.class,
		AnonymousBeanValuePropertyTest.class, AnonymousPojoValuePropertyTest.class, BeanPropertiesTest.class,
		BeansObservablesTest.class, PojoObservablesTest.class, PojoPropertiesTest.class, SetOnlyJavaBeanTest.class,
		NumberToStringConverterTest.class, StringToNumberConverterTest.class, AbstractObservableTest.class,
		ChangeSupportTest.class, DecoratingObservableTest.class, Diffs_ListDiffTests.class, DiffsTest.class,
		ObservablesTest.class, ObservableTrackerTest.class, RealmTest.class, AbstractObservableListTest.class,
		ComputedListTest.class, DecoratingObservableListTest.class, ListDiffTest.class, ListDiffVisitorTest.class,
		MultiListTest.class, ObservableListTest.class, WritableListTest.class, AbstractObservableMapTest.class,
		BidiObservableMapTest.class, ObservableMapTest.class, WritableMapTest.class, CompositeMapTest.class,
		ComputedObservableMapTest.class, AbstractObservableSetTest.class, ComputedSetTest.class,
		DecoratingObservableSetTest.class, ObservableSetTest.class, UnionSetTest.class, WritableSetTest.class,
		AbstractObservableValueTest.class, AbstractVetoableValueTest.class, ComputedValueTest.class,
		DateAndTimeObservableValueTest.class, DecoratingObservableValueTest.class, DuplexingObservableValueTest.class,
		SelectObservableValueTest.class, WritableValueTest.class, MultiValidatorTest.class, ValidationStatusTest.class,
		BindingMessagesTest.class, BindingStatusTest.class, ConverterValuePropertyTest.class,
		DifferentRealmsBindingTest.class, IdentityMapTest.class, IdentitySetTest.class, QueueTest.class,
		DateConversionSupportTest.class, IdentityConverterTest.class, IntegerToStringConverterTest.class,
		NumberToBigDecimalTest.class, NumberToBigIntegerConverterTest.class, NumberToByteConverterTest.class,
		NumberToDoubleConverterTest.class, NumberToFloatConverterTest.class, NumberToIntegerConverterTest.class,
		NumberToLongConverterTest.class, NumberToShortConverterTest.class, ObjectToPrimitiveValidatorTest.class,
		StatusToStringConverterTest.class, StringToBooleanConverterTest.class, StringToByteConverterTest.class,
		StringToCharacterConverterTest.class, StringToNumberParserByteTest.class, StringToNumberParserDoubleTest.class,
		StringToNumberParserFloatTest.class, StringToNumberParserIntegerTest.class, StringToNumberParserLongTest.class,
		StringToNumberParserShortTest.class, StringToNumberParserTest.class, StringToShortConverterTest.class,
		BeanObservableListDecoratorTest.class, BeanObservableSetDecoratorTest.class,
		BeanObservableValueDecoratorTest.class, BeanObservableListDecoratorTest.class, BeanValuePropertyTest.class,
		JavaBeanObservableArrayBasedListTest.class, JavaBeanObservableArrayBasedSetTest.class,
		JavaBeanObservableListTest.class, JavaBeanObservableMapTest.class, JavaBeanObservableSetTest.class,
		JavaBeanObservableValueTest.class, JavaBeanPropertyObservableMapTest.class, BeanPropertyHelperTest.class,
		BeanPropertyListenerSupportTest.class, BeanPropertyListenerTest.class, ConstantObservableValueTest.class,
		DelayedObservableValueTest.class, EmptyObservableListTest.class, EmptyObservableSetTest.class,
		IdentityObservableSetTest.class, MapEntryObservableValueTest.class, StalenessObservableValueTest.class,
		UnmodifiableObservableValueTest.class, UnmodifiableObservableListTest.class,
		UnmodifiableObservableSetTest.class, ValidatedObservableValueTest.class, ValidatedObservableListTest.class,
		ValidatedObservableSetTest.class, DetailObservableListTest.class, DetailObservableMapTest.class,
		DetailObservableSetTest.class, DetailObservableValueTest.class, ListDetailValueObservableListTest.class,
		MapDetailValueObservableMapTest.class, SetDetailValueObservableMapTest.class,
		MapSimpleValueObservableMapTest.class, SetSimpleValueObservableMapTest.class,
		ListSimpleValueObservableListTest.class, AbstractStringToNumberValidatorTest.class,
		NumberToByteValidatorTest.class, NumberToDoubleValidatorTest.class, NumberToFloatValidatorTest.class,
		NumberToIntegerValidatorTest.class, NumberToLongValidatorTest.class, NumberToShortValidatorTest.class,
		NumberToUnboundedNumberValidatorTest.class, StringToByteValidatorTest.class,
		StringToCharacterValidatorTest.class, StringToDoubleValidatorTest.class, StringToFloatValidatorTest.class,
		StringToIntegerValidatorTest.class, StringToLongValidatorTest.class, StringToShortValidatorTest.class,
		BindingScenariosTestSuite.class, SWTObservablesTest.class, WidgetPropertiesTest.class,
		WidgetObservableThreadTest.class, PreferencePageSupportTest.class, ObservableListContentProviderTest.class,
		ObservableListTreeContentProviderTest.class, ObservableMapLabelProviderTest.class,
		ObservableSetContentProviderTest.class, ObservableSetTreeContentProviderTest.class,
		ObservableValueEditingSupportTest.class, ViewersObservablesTest.class, ViewerSupportTest.class,
		WizardPageSupportTest.class, EditMaskLexerAndTokenTest.class, EditMaskParserTest.class,
		ButtonObservableValueTest.class, CComboObservableValueTest.class, CComboObservableValueSelectionTest.class,
		CComboObservableValueTextTest.class, CComboSingleSelectionObservableValueTest.class,
		CComboSingleSelectionObservableValueTest.class, CLabelObservableValueTest.class, ComboObservableValueTest.class,
		ComboObservableValueSelectionTest.class, ComboObservableValueTextTest.class,
		ComboSingleSelectionObservableValueTest.class, DateTimeCalendarObservableValueTest.class,
		DateTimeDateObservableValueTest.class, DateTimeSelectionPropertyTest.class,
		DateTimeTimeObservableValueTest.class, SWTDelayedObservableValueDecoratorTest.class,
		ControlObservableValueTest.class, LabelObservableValueTest.class, GroupObservableValueTest.class,
		ListSingleSelectionObservableValueTest.class, ScaleObservableValueMinTest.class,
		ScaleObservableValueMaxTest.class, ScaleObservableValueSelectionTest.class, ShellObservableValueTest.class,
		SpinnerObservableValueTest.class, SpinnerObservableValueMinTest.class, SpinnerObservableValueMaxTest.class,
		SpinnerObservableValueSelectionTest.class, TableObservableValueTest.class,
		TableSingleSelectionObservableValueTest.class, TextEditableObservableValueTest.class,
		TextObservableValueDefaultSelectionTest.class, TextObservableValueFocusOutTest.class,
		TextObservableValueModifyTest.class, TextObservableValueTest.class,
		StyledTextObservableValueDefaultSelectionTest.class, StyledTextObservableValueFocusOutTest.class,
		StyledTextObservableValueModifyTest.class, StyledTextObservableValueTest.class,
		CheckableCheckedElementsObservableSetTest.class, ObservableViewerElementSetTest.class,
		ObservableCollectionContentProviderTest.class, ObservableCollectionTreeContentProviderTest.class,
		SelectionProviderMultiSelectionObservableListTest.class,
		SelectionProviderSingleSelectionObservableValueTest.class, ViewerElementMapTest.class,
		ViewerElementSetTest.class, ViewerElementWrapperTest.class, ViewerInputObservableValueTest.class,
		BindingTestSuiteJunit3.class })
public class BindingTestSuite {
}