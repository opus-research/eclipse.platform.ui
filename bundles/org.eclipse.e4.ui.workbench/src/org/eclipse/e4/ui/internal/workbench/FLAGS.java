package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * @since 1.1
 */
public enum FLAGS {

	IN_ANY_PERSPECTIVE(1), IN_ACTIVE_PERSPECTIVE(2), IN_MAIN_MENU(3), IN_PART(4), ANYWHERE(5);

	private Map<EClass, List<Integer>> features_by_class = new HashMap<EClass, List<Integer>>();
	private List<EClass> classes_by_flag = new ArrayList<EClass>();

	/**
	 * @return the features_by_class
	 */
	public Map<EClass, List<Integer>> getFeatures_by_class() {
		return features_by_class;
	}

	FLAGS(int flag) {
		init(flag);
	}

	private void init(int flag) {

		if (flag == 1) { // IN_ANY_PERSPECTIVE
			features_by_class.put(ApplicationPackageImpl.Literals.APPLICATION,
					Arrays.asList(ApplicationPackageImpl.APPLICATION__CHILDREN));

			features_by_class.put(BasicPackageImpl.Literals.WINDOW, Arrays
					.asList(BasicPackageImpl.WINDOW__CHILDREN,
							BasicPackageImpl.WINDOW__WINDOWS));

			features_by_class.put(BasicPackageImpl.Literals.TRIMMED_WINDOW,
					Arrays.asList(BasicPackageImpl.TRIMMED_WINDOW__CHILDREN,
							BasicPackageImpl.TRIMMED_WINDOW__WINDOWS));

			features_by_class
					.put(BasicPackageImpl.Literals.PART_SASH_CONTAINER,
							Arrays.asList(BasicPackageImpl.PART_SASH_CONTAINER__CHILDREN));

			features_by_class
					.put(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK,
							Arrays.asList(AdvancedPackageImpl.PERSPECTIVE_STACK__CHILDREN));

			features_by_class.put(AdvancedPackageImpl.Literals.PERSPECTIVE,
					Arrays.asList(AdvancedPackageImpl.PERSPECTIVE__CHILDREN,
							AdvancedPackageImpl.PERSPECTIVE__WINDOWS));

			features_by_class.put(BasicPackageImpl.Literals.PART_STACK,
					Arrays.asList(BasicPackageImpl.PART_STACK__CHILDREN));

			features_by_class.put(BasicPackageImpl.Literals.PART, Arrays
					.asList(BasicPackageImpl.PART__HANDLERS,
							BasicPackageImpl.PART__MENUS,
							BasicPackageImpl.PART__TOOLBAR));

			features_by_class.put(MenuPackageImpl.Literals.TOOL_BAR,
					Arrays.asList(MenuPackageImpl.TOOL_BAR__CHILDREN));

			features_by_class.put(MenuPackageImpl.Literals.MENU,
					Arrays.asList(MenuPackageImpl.MENU__CHILDREN));

			features_by_class.put(AdvancedPackageImpl.Literals.PLACEHOLDER,
					Arrays.asList(AdvancedPackageImpl.PLACEHOLDER__REF));

			features_by_class.put(AdvancedPackageImpl.Literals.AREA,
					Arrays.asList(AdvancedPackageImpl.AREA__CHILDREN));
			
			classes_by_flag.addAll(Arrays.asList(CommandsPackageImpl.Literals.HANDLER,
					BasicPackageImpl.Literals.PART,
					AdvancedPackageImpl.Literals.PERSPECTIVE_STACK,
					AdvancedPackageImpl.Literals.PERSPECTIVE,
					BasicPackageImpl.Literals.PART_SASH_CONTAINER,
					BasicPackageImpl.Literals.PART_STACK,
					AdvancedPackageImpl.Literals.PLACEHOLDER,
					BasicPackageImpl.Literals.TRIMMED_WINDOW,
					BasicPackageImpl.Literals.WINDOW,
					AdvancedPackageImpl.Literals.AREA,
					MenuPackageImpl.Literals.TOOL_BAR,
					MenuPackageImpl.Literals.TOOL_CONTROL,
					MenuPackageImpl.Literals.TOOL_BAR_ELEMENT,
					MenuPackageImpl.Literals.MENU,
					MenuPackageImpl.Literals.MENU_ITEM));
		}

		if (flag == 2) {// IN_ACTIVE_PERSPECTIVE
			features_by_class.putAll(IN_ANY_PERSPECTIVE.getFeatures_by_class());
			features_by_class
					.put(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK,
							Arrays.asList(AdvancedPackageImpl.PERSPECTIVE_STACK__SELECTED_ELEMENT));
			
			classes_by_flag.addAll(Arrays.asList(CommandsPackageImpl.Literals.HANDLER,
					BasicPackageImpl.Literals.PART,
					BasicPackageImpl.Literals.INPUT_PART,
					AdvancedPackageImpl.Literals.PERSPECTIVE,
					BasicPackageImpl.Literals.PART_SASH_CONTAINER,
					BasicPackageImpl.Literals.PART_STACK,
					AdvancedPackageImpl.Literals.PLACEHOLDER,
					BasicPackageImpl.Literals.TRIMMED_WINDOW,
					BasicPackageImpl.Literals.WINDOW,
					AdvancedPackageImpl.Literals.AREA,
					MenuPackageImpl.Literals.TOOL_BAR,
					MenuPackageImpl.Literals.TOOL_BAR_ELEMENT,
					MenuPackageImpl.Literals.TOOL_CONTROL,
					MenuPackageImpl.Literals.MENU,
					MenuPackageImpl.Literals.MENU_ITEM,
					MenuPackageImpl.Literals.MENU_ELEMENT));
		}

		if (flag == 3) {// IN_MAIN_MENU
			features_by_class.put(ApplicationPackageImpl.Literals.APPLICATION,
					Arrays.asList(ApplicationPackageImpl.APPLICATION__CHILDREN));

			features_by_class.put(BasicPackageImpl.Literals.WINDOW, Arrays
					.asList(BasicPackageImpl.WINDOW__CHILDREN,
							BasicPackageImpl.WINDOW__MAIN_MENU));

			features_by_class.put(BasicPackageImpl.Literals.TRIMMED_WINDOW,
					Arrays.asList(BasicPackageImpl.TRIMMED_WINDOW__CHILDREN,
							BasicPackageImpl.TRIMMED_WINDOW__MAIN_MENU));

			features_by_class.put(BasicPackageImpl.Literals.PART_SASH_CONTAINER,
					Arrays.asList(BasicPackageImpl.PART_SASH_CONTAINER__CHILDREN));

			features_by_class.put(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK,
					Arrays.asList(AdvancedPackageImpl.PERSPECTIVE_STACK__CHILDREN));

			features_by_class.put(AdvancedPackageImpl.Literals.PERSPECTIVE, Arrays.asList(
					AdvancedPackageImpl.PERSPECTIVE__WINDOWS));
			
			features_by_class.put(MenuPackageImpl.Literals.MENU,
					Arrays.asList(MenuPackageImpl.MENU__CHILDREN));

			classes_by_flag.addAll(Arrays.asList(MenuPackageImpl.Literals.MENU,
					MenuPackageImpl.Literals.MENU_ITEM, 
					MenuPackageImpl.Literals.MENU_ELEMENT,
					MenuPackageImpl.Literals.MENU_SEPARATOR));
		}

		if (flag == 4) {// IN_PART
			features_by_class.putAll(IN_ANY_PERSPECTIVE.getFeatures_by_class());

			features_by_class.put(BasicPackageImpl.Literals.PART, Arrays
					.asList(BasicPackageImpl.PART__HANDLERS,
							BasicPackageImpl.PART__MENUS,
							BasicPackageImpl.PART__TOOLBAR));

			features_by_class.put(MenuPackageImpl.Literals.TOOL_BAR,
					Arrays.asList(MenuPackageImpl.TOOL_BAR__CHILDREN));

			features_by_class.put(MenuPackageImpl.Literals.MENU,
					Arrays.asList(MenuPackageImpl.MENU__CHILDREN));
			
			classes_by_flag.addAll(Arrays.asList(
					CommandsPackageImpl.Literals.HANDLER,
					MenuPackageImpl.Literals.TOOL_BAR, 
					MenuPackageImpl.Literals.TOOL_BAR_ELEMENT,
					MenuPackageImpl.Literals.TOOL_CONTROL,
					MenuPackageImpl.Literals.MENU, 
					MenuPackageImpl.Literals.MENU_ITEM,
					MenuPackageImpl.Literals.MENU_ELEMENT));
		}
		
		if (flag == 5) {// ANYWHERE
			features_by_class
					.put(ApplicationPackageImpl.Literals.APPLICATION,
							Arrays.asList(
									ApplicationPackageImpl.APPLICATION__CHILDREN,
									ApplicationPackageImpl.APPLICATION__HANDLERS,
									ApplicationPackageImpl.APPLICATION__ADDONS,
									ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS,
									ApplicationPackageImpl.APPLICATION__BINDING_TABLES));

			features_by_class.put(CommandsPackageImpl.Literals.BINDING_TABLE,
					Arrays.asList(CommandsPackageImpl.BINDING_TABLE__BINDINGS));

			features_by_class.put(BasicPackageImpl.Literals.WINDOW, Arrays
					.asList(BasicPackageImpl.WINDOW__CHILDREN,
							BasicPackageImpl.WINDOW__HANDLERS,
							BasicPackageImpl.WINDOW__SHARED_ELEMENTS,
							BasicPackageImpl.WINDOW__MAIN_MENU));

			features_by_class.put(BasicPackageImpl.Literals.TRIMMED_WINDOW,
					Arrays.asList(BasicPackageImpl.TRIMMED_WINDOW__CHILDREN,
							BasicPackageImpl.TRIMMED_WINDOW__HANDLERS,
							BasicPackageImpl.TRIMMED_WINDOW__SHARED_ELEMENTS,
							BasicPackageImpl.TRIMMED_WINDOW__TRIM_BARS,
							BasicPackageImpl.TRIMMED_WINDOW__MAIN_MENU));

			features_by_class
					.put(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK,
							Arrays.asList(AdvancedPackageImpl.PERSPECTIVE_STACK__CHILDREN));

			features_by_class.put(AdvancedPackageImpl.Literals.PERSPECTIVE,
					Arrays.asList(AdvancedPackageImpl.PERSPECTIVE__CHILDREN,
							AdvancedPackageImpl.PERSPECTIVE__WINDOWS));

			features_by_class.put(BasicPackageImpl.Literals.PART_STACK,
					Arrays.asList(BasicPackageImpl.PART_STACK__CHILDREN));
			
			features_by_class.put(AdvancedPackageImpl.Literals.PLACEHOLDER,
					Arrays.asList(AdvancedPackageImpl.PLACEHOLDER__REF));

			features_by_class.put(AdvancedPackageImpl.Literals.AREA,
					Arrays.asList(AdvancedPackageImpl.AREA__CHILDREN));

			features_by_class.put(BasicPackageImpl.Literals.PART, Arrays
					.asList(BasicPackageImpl.PART__HANDLERS,
							BasicPackageImpl.PART__MENUS,
							BasicPackageImpl.PART__TOOLBAR));

			features_by_class.put(MenuPackageImpl.Literals.TOOL_BAR,
					Arrays.asList(MenuPackageImpl.TOOL_BAR__CHILDREN));

			features_by_class.put(MenuPackageImpl.Literals.MENU,
					Arrays.asList(MenuPackageImpl.MENU__CHILDREN));
			
			classes_by_flag.addAll(Arrays.asList(ApplicationPackageImpl.Literals.APPLICATION,
					ApplicationPackageImpl.Literals.ADDON,
					CommandsPackageImpl.Literals.HANDLER,
					CommandsPackageImpl.Literals.BINDING_CONTEXT,
					CommandsPackageImpl.Literals.BINDING_TABLE,
					CommandsPackageImpl.Literals.KEY_BINDING,
					BasicPackageImpl.Literals.PART,
					BasicPackageImpl.Literals.INPUT_PART,
					AdvancedPackageImpl.Literals.PERSPECTIVE_STACK,
					AdvancedPackageImpl.Literals.PERSPECTIVE,
					BasicPackageImpl.Literals.PART_SASH_CONTAINER,
					BasicPackageImpl.Literals.PART_STACK,
					AdvancedPackageImpl.Literals.PLACEHOLDER,
					BasicPackageImpl.Literals.TRIMMED_WINDOW,
					BasicPackageImpl.Literals.WINDOW,
					AdvancedPackageImpl.Literals.AREA,
					MenuPackageImpl.Literals.TOOL_BAR,
					MenuPackageImpl.Literals.TOOL_BAR_ELEMENT,
					MenuPackageImpl.Literals.TOOL_CONTROL,
					MenuPackageImpl.Literals.MENU,
					MenuPackageImpl.Literals.MENU_ITEM, MenuPackageImpl.Literals.MENU_ELEMENT));
		}
	}


	public boolean isAllowWithFlags(EObject eObject) {
		return classes_by_flag.contains(eObject.eClass());
	}
}
