package org.eclipse.ui.tests.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class OpenViewTest extends UITestCase {

	public static final String PERSPECTIVE_ID = "org.eclipse.ui.tests.views.OpenViewTest.TestPerspective";
	public static final String PERSPECTIVE_VIEW_ID = "org.eclipse.ui.tests.views.OpenViewTest.PerspectiveView";
	public static final String OTHER_VIEW_ID = "org.eclipse.ui.tests.views.OpenViewTest.OtherView";

	public OpenViewTest(String testName) {
		super(testName);
	}

	public void testOpenViewWithEmptyPartStack() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbench workbench = window.getWorkbench();
		IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		IPerspectiveDescriptor perspective = workbench.getPerspectiveRegistry().findPerspectiveWithId(PERSPECTIVE_ID);
		activePage.setPerspective(perspective);
		processUiEvents();

		IViewPart visibleView = activePage.findView(PERSPECTIVE_VIEW_ID);
		activePage.hideView(visibleView);
		processUiEvents();

		OtherView otherView = (OtherView) activePage.showView(OTHER_VIEW_ID);
		processUiEvents();

		assertTrue("expected partVisible notification not received when opening view",
				otherView.receivedPartVisibleEvent);
	}

	private void processUiEvents() {
		while (fWorkbench.getDisplay().readAndDispatch()) {
		}
	}

	public static class TestPerspectiveFactory implements IPerspectiveFactory {
		@Override
		public void createInitialLayout(IPageLayout layout) {
			String editorArea = layout.getEditorArea();
			layout.setEditorAreaVisible(true);

			IFolderLayout folder = layout.createFolder("test_folder", IPageLayout.LEFT, 0.5f, editorArea);
			folder.addView(PERSPECTIVE_VIEW_ID);
			// folder.addPlaceholder(OTHER_VIEW_ID); // we get a partVisible call with this
		}
	}

	private static class TestView extends ViewPart {

		@Override
		public void createPartControl(Composite parent) {
			setPartName(getClass().getSimpleName());

			Label label = new Label(parent, SWT.NONE);
			label.setText("view " + getClass().getSimpleName());
		}

		@Override
		public void setFocus() {
			// Nothing to do.
		}
	}

	public static class PerspectiveView extends TestView {

	}

	public static class OtherView extends TestView {

		private boolean receivedPartVisibleEvent = false;

		@Override
		public void createPartControl(Composite parent) {
			super.createPartControl(parent);

			IWorkbenchPage workbenchPage = getSite().getWorkbenchWindow().getActivePage();

			workbenchPage.addPartListener(new IPartListener2() {
				@Override
				public void partActivated(IWorkbenchPartReference partRef) {
					System.out.println("VIEW ACTIVATED");
				}

				@Override
				public void partVisible(IWorkbenchPartReference partRef) {
					receivedPartVisibleEvent = true;
					System.out.println("VIEW VISIBLE");
				}

				@Override
				public void partHidden(IWorkbenchPartReference partRef) {
					System.out.println("VIEW HIDDEN");
				}

				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {
					System.out.println("VIEW BROUGHT TO TOP");
				}

				@Override
				public void partClosed(IWorkbenchPartReference partRef) {
					System.out.println("VIEW CLOSED");
				}

				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {
					System.out.println("VIEW DEACTIVATED");
				}

				@Override
				public void partOpened(IWorkbenchPartReference partRef) {
					System.out.println("VIEW OPENED");
				}

				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {
					System.out.println("VIEW INPUT CHANGED");
				}
			});
		}
	}
}
