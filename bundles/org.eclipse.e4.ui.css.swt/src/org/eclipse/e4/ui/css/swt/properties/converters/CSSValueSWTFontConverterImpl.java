/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.converters;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.VALUE_FROM_FONT_DEFINITION;
import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;

import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.resources.FontByDefinition;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSValue;

public class CSSValueSWTFontConverterImpl extends
CSSValueSWTFontDataConverterImpl {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTFontConverterImpl();

	public CSSValueSWTFontConverterImpl() {
		super(Font.class);
	}

	@Override
	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception {
		FontData fontData = (FontData) super.convert(value, engine, context);
		if (fontData != null) {
			Display display = super.getDisplay(context);
			Font font = new Font(display, fontData);

			if (CSSSWTFontHelper.hasFontDefinitionAsFamily(value)) {
				return createFontByDefinition((CSS2FontProperties) value, font);
			}
			return font;
		}
		return null;
	}

	protected FontByDefinition createFontByDefinition(CSS2FontProperties props, Font font) {
		FontData fontData = CSSSWTFontHelper.getFirstFontData(font);
		FontByDefinition fontByDefinition =
				new FontByDefinition(normalizeId(props.getFamily().getCssText().substring(1)), font);
		if (props.getSize() != null && !VALUE_FROM_FONT_DEFINITION.equals(props.getSize().getCssText())) {
			fontByDefinition.setHeight(fontData.getHeight());
		}
		if (props.getStyle() != null && !VALUE_FROM_FONT_DEFINITION.equals(props.getStyle().getCssText())) {
			fontByDefinition.setStyle(fontData.getStyle());
		}
		return fontByDefinition;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		Font font = value instanceof FontByDefinition ? CSSSWTFontHelper
				.getFont((FontByDefinition) value) : (Font) value;
		return super.convert(CSSSWTFontHelper.getFirstFontData(font), engine,
				context, config);
	}
}
