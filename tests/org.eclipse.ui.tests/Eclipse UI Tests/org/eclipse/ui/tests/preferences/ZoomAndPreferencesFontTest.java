package org.eclipse.ui.tests.preferences;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.themes.ColorsAndFontsPreferencePage;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class ZoomAndPreferencesFontTest {

	private static IProject project;
	private static IFile file;
	private StyledText text;
	private AbstractTextEditor editor;

	/**
	 * @param testName
	 */
	public ZoomAndPreferencesFontTest() {
	}

	@BeforeClass
	public static void createFilesAndOpenEditor() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		file = project.getFile("foo.txt");
		file.create(new ByteArrayInputStream("foo.bar".getBytes()), true, new NullProgressMonitor());
	}

	@Before
	public void restoreDefaultFontAndOpenEditor() throws Exception {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}

		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		editor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(file), desc.getId());
		editor.setFocus();
		text = (StyledText) editor.getAdapter(Control.class);
		// make sure we start from a clean state
		Assert.assertEquals(10, text.getFont().getFontData()[0].getHeight());
	}

	@After
	public void restoreAndCheckDefaults() {
		PreferenceDialog dialog = new PreferenceDialog(Display.getCurrent().getActiveShell(), PlatformUI.getWorkbench().getPreferenceManager());
		dialog.setSelectedNode("org.eclipse.ui.preferencePages.ColorsAndFonts");
		dialog.setBlockOnOpen(false);
		dialog.open();
		ColorsAndFontsPreferencePage page = (ColorsAndFontsPreferencePage)dialog.getSelectedPage();
		page.performDefaults();
		page.performOk();
		dialog.close();
		// make sure we land on a clean state
		Assert.assertEquals(10, text.getFont().getFontData()[0].getHeight());
		editor.close(false);
	}

	@Test
	public void testThemeAPIvsPreferences() {
		int targetHeight = 17; // Whatever > 0 and not default size/10
		FontRegistry registry = editor.getSite().getWorkbenchWindow().getWorkbench().getThemeManager().getCurrentTheme()
				.getFontRegistry();
		FontData[] data = registry.getFontData(JFaceResources.TEXT_FONT);
		FontDescriptor desc = FontDescriptor.createFrom(data).setHeight(targetHeight);
		registry.put(JFaceResources.TEXT_FONT, desc.getFontData());
		Assert.assertEquals(targetHeight, text.getFont().getFontData()[0].getHeight());
		restoreAndCheckDefaults();
	}

	@Test
	public void testZoomCommand() throws Exception {
		Command command = PlatformUI.getWorkbench().getService(ICommandService.class)
				.getCommand("org.eclipse.ui.edit.text.zoomIn");
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
		command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
		Assert.assertEquals(22, text.getFont().getFontData()[0].getHeight());
	}

}
