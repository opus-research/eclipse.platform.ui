package org.eclipse.ui.forms.examples.internal;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractImageManager;

public class ExamplesImages extends AbstractImageManager {

	private static final String PLUGIN_ID = "org.eclipse.ui.forms.examples";
	public static final String IMG_FORM_BG = "formBg";
	public static final String IMG_LARGE = "large";
	public static final String IMG_HORIZONTAL = "horizontal";
	public static final String IMG_VERTICAL = "vertical";
	public static final String IMG_SAMPLE = "sample";
	public static final String IMG_WIZBAN = "wizban";
	public static final String IMG_LINKTO_HELP = "linkto_help";
	public static final String IMG_HELP_TOPIC = "help_topic";
	public static final String IMG_CLOSE = "close";
	public static final String IMG_HELP_CONTAINER = "container_obj";
	public static final String IMG_HELP_TOC_OPEN = "toc_open";
	public static final String IMG_HELP_TOC_CLOSED = "toc_closed";
	public static final String IMG_HELP_SEARCH = "e_search_menu";
	public static final String IMG_CLEAR = "clear";
	public static final String IMG_NW = "nw";

	public static final ExamplesImages INSTANCE = new ExamplesImages();

	private ExamplesImages() {
		// don't allow others to create instances of this class
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		registerImage(PLUGIN_ID, IMG_FORM_BG, "icons/form_banner.gif");
		registerImage(PLUGIN_ID, IMG_LARGE, "icons/large_image.gif");
		registerImage(PLUGIN_ID, IMG_HORIZONTAL, "icons/th_horizontal.gif");
		registerImage(PLUGIN_ID, IMG_VERTICAL, "icons/th_vertical.gif");
		registerImage(PLUGIN_ID, IMG_SAMPLE, "icons/sample.png");
		registerImage(PLUGIN_ID, IMG_WIZBAN, "icons/newprj_wiz.png");
		registerImage(PLUGIN_ID, IMG_LINKTO_HELP, "icons/linkto_help.gif");
		registerImage(PLUGIN_ID, IMG_HELP_TOPIC, "icons/topic.gif");
		registerImage(PLUGIN_ID, IMG_HELP_CONTAINER, "icons/container_obj.gif");
		registerImage(PLUGIN_ID, IMG_HELP_TOC_CLOSED, "icons/toc_closed.gif");
		registerImage(PLUGIN_ID, IMG_HELP_TOC_OPEN, "icons/toc_open.gif");
		registerImage(PLUGIN_ID, IMG_CLOSE, "icons/close_view.gif");
		registerImage(PLUGIN_ID, IMG_HELP_SEARCH, "icons/e_search_menu.gif");
		registerImage(PLUGIN_ID, IMG_CLEAR, "icons/clear.gif");
		registerImage(PLUGIN_ID, IMG_NW, "icons/nw.gif");

	}

}
