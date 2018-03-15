/*******************************************************************************
 * Copyright (c) 2017 Ralf M Petter<ralf.petter@gmail.com> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ralf M Petter<ralf.petter@gmail.com> - initial API and implementation
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 322337
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.ui.internal.forms.widgets.FormTextModel;
import org.junit.Test;

/**
 * Tests for FormTextModel
 */
public class FormTextModelTest {

	@Test
	public void testWhitespaceNormalized() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(true);
		formTextModel.parseTaggedText("<form><p>   line with   \r\n   <b>  whitespace </b> Test </p></form>", false);
		assertEquals("FormTextModel does not remove whitespace correctly according to the rules",
				"line with whitespace Test" + System.lineSeparator(), formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text",
				"<form><p>   line with   \r\n   <b>  whitespace </b> Test </p></form>", formTextModel.getRawText());
	}

	@Test
	public void testWhitespaceNotNormalized() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(false);
		formTextModel.parseTaggedText("<form><p>   line with      <b>  whitespace </b> Test </p></form>", false);
		assertEquals("FormTextModel does not preserve whitespace correctly according to the rules",
				"   line with        whitespace  Test " + System.lineSeparator(), formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text",
				"<form><p>   line with      <b>  whitespace </b> Test </p></form>", formTextModel.getRawText());
	}

	@Test
	public void testTextWithAmpersand() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.parseTaggedText("<form>Foo &Bar</form>", false);
		assertEquals("Foo &Bar" + System.lineSeparator(),
				formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text", "<form>Foo &Bar</form>",
				formTextModel.getRawText());
	}

	@Test
	public void testXMLWhitespaceNormalized() throws Exception {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(true);
		formTextModel.parseInputStream(
				new ByteArrayInputStream("<form><p>   line with   \r\n   <b>  whitespace </b> Test </p></form>"
						.getBytes(StandardCharsets.UTF_8)),
				false);
		assertEquals("FormTextModel does not remove whitespace correctly according to the rules",
				"line with whitespace Test" + System.lineSeparator(), formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text",
				"<form><p>   line with   \r\n   <b>  whitespace </b> Test </p></form>", formTextModel.getRawText());
		formTextModel.parseInputStream(new ByteArrayInputStream(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><form><p>   line with å  \r\n   <b>  whitespace </b> Test </p></form>"
						.getBytes("ISO-8859-1")),
				false);
		assertEquals("FormTextModel does not remove whitespace correctly according to the rules",
				"line with å whitespace Test" + System.lineSeparator(), formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text",
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><form><p>   line with å  \r\n   <b>  whitespace </b> Test </p></form>",
				formTextModel.getRawText());
	}

	@Test
	public void testXMLWhitespaceNotNormalized() throws Exception {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(false);
		formTextModel.parseInputStream(
				new ByteArrayInputStream("<form><p>   line with      <b>  whitespace </b> Test </p></form>"
						.getBytes(StandardCharsets.UTF_8)),
				false);
		assertEquals("FormTextModel does not preserve whitespace correctly according to the rules",
				"   line with        whitespace  Test " + System.lineSeparator(), formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text",
				"<form><p>   line with      <b>  whitespace </b> Test </p></form>", formTextModel.getRawText());
		formTextModel.parseInputStream(new ByteArrayInputStream(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><form><p>   line with å     <b>  whitespace </b> Test </p></form>"
						.getBytes("ISO-8859-1")),
				false);
		assertEquals("FormTextModel does not preserve whitespace correctly according to the rules",
				"   line with å       whitespace  Test " + System.lineSeparator(), formTextModel.getAccessibleText());
		assertEquals("FormTextModel does not return the originally provided text",
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><form><p>   line with å     <b>  whitespace </b> Test </p></form>",
				formTextModel.getRawText());
	}

}
