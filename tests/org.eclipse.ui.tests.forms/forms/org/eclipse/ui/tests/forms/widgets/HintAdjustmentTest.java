package org.eclipse.ui.tests.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TreeNode;
import org.eclipse.ui.forms.widgets.Twistie;

import junit.framework.TestCase;

public class HintAdjustmentTest extends TestCase {
	private static Display display;

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}
	}

	private Shell shell;

	@Override
	public void setUp() throws Exception {
		shell = new Shell(display);
	}

	@Override
	public void tearDown() throws Exception {
		shell.dispose();
	}

	void verifyComputeSize(Control control) {
		int widthAdjustment;
		int heightAdjustment;
		if (control instanceof Scrollable) {
			// For scrollables, subtract off the trim size
			Scrollable scrollable = (Scrollable) control;
			Rectangle trim = scrollable.computeTrim(0, 0, 0, 0);

			widthAdjustment = trim.width;
			heightAdjustment = trim.height;
		} else {
			// For non-composites, subtract off 2 * the border size
			widthAdjustment = control.getBorderWidth() * 2;
			heightAdjustment = widthAdjustment;
		}

		final int TEST_VALUE = 100;

		Point computedSize = control.computeSize(TEST_VALUE, TEST_VALUE);

		assertEquals("control is not applying the width adjustment correctly", TEST_VALUE + widthAdjustment,
				computedSize.x);
		assertEquals("control is not applying the height adjustment correctly", TEST_VALUE + heightAdjustment,
				computedSize.y);
	}

	public void testScrollingHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		link.setText("This is some sample text");
		verifyComputeSize(link);
	}

	public void testHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.NONE);
		link.setText("This is some sample text");
		verifyComputeSize(link);
	}

	public void testScrollingExpandableComposite() {
		ExpandableComposite ec = new ExpandableComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		ec.setText("Foo bar baz zipp");
		verifyComputeSize(ec);
	}

	public void testExpandableComposite() {
		ExpandableComposite ec = new ExpandableComposite(shell, SWT.NONE);
		ec.setText("Foo bar baz zipp");
		verifyComputeSize(ec);
	}

	public void testScrollingForm() {
		Form form = new Form(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		form.setMessage("Hello world");
		verifyComputeSize(form);
	}

	public void testForm() {
		Form form = new Form(shell, SWT.NONE);
		form.setMessage("Hello world");
		verifyComputeSize(form);
	}

	public void testScrollingFormText() {
		FormText formText = new FormText(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		formText.setText("This izza test", false, false);
		verifyComputeSize(formText);
	}

	public void testFormText() {
		FormText formText = new FormText(shell, SWT.NONE);
		formText.setText("This izza test", false, false);
		verifyComputeSize(formText);
	}

	public void testScrollingImageHyperlink() {
		ImageHyperlink hyperlink = new ImageHyperlink(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		hyperlink.setText("Foo, bar, baz");
		verifyComputeSize(hyperlink);
	}

	public void testImageHyperlink() {
		ImageHyperlink hyperlink = new ImageHyperlink(shell, SWT.NONE);
		hyperlink.setText("Foo, bar, baz");
		verifyComputeSize(hyperlink);
	}

	public void testScrolledForm() {
		ScrolledForm scrolledForm = new ScrolledForm(shell, SWT.NONE);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	public void testScrollingScrolledForm() {
		ScrolledForm scrolledForm = new ScrolledForm(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	public void testScrolledFormText() {
		ScrolledFormText scrolledForm = new ScrolledFormText(shell, SWT.NONE, true);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	public void testScrollingScrolledFormText() {
		ScrolledFormText scrolledForm = new ScrolledFormText(shell, SWT.H_SCROLL | SWT.V_SCROLL, true);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	public void testScrolledPageBook() {
		ScrolledPageBook scrolledPageBook = new ScrolledPageBook(shell, SWT.NONE);
		verifyComputeSize(scrolledPageBook);
	}

	public void testScrollingScrolledPageBook() {
		ScrolledPageBook scrolledPageBook = new ScrolledPageBook(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		verifyComputeSize(scrolledPageBook);
	}

	public void testSection() {
		Section section = new Section(shell, SWT.NONE);
		section.setText("Hi ho he hum de da doo dum");
		verifyComputeSize(section);
	}

	public void testScrollingSection() {
		Section section = new Section(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		section.setText("Hi ho he hum de da doo dum");
		verifyComputeSize(section);
	}

	public void testTreeNode() {
		TreeNode treeNode = new TreeNode(shell, SWT.NONE);
		verifyComputeSize(treeNode);
	}

	public void testScrollingTreeNode() {
		TreeNode treeNode = new TreeNode(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		verifyComputeSize(treeNode);
	}

	public void testTwistie() {
		Twistie twistie = new Twistie(shell, SWT.NONE);
		verifyComputeSize(twistie);
	}

	public void testScrollingTwistie() {
		Twistie twistie = new Twistie(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		verifyComputeSize(twistie);
	}
}
