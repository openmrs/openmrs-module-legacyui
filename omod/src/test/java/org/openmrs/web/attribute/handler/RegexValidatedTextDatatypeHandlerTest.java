/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.attribute.handler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.servlet.http.HttpServletRequest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests {@code RegexValidatedTextDatatypeHandler}.
 */
@RunWith(PowerMockRunner.class)
public class RegexValidatedTextDatatypeHandlerTest {
	
	
	@Rule
	ExpectedException expectedException = ExpectedException.none();
	
	private RegexValidatedTextDatatypeHandler handler = new RegexValidatedTextDatatypeHandler();
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(CustomDatatype,
	 *      HttpServletRequest, String)
	 * @verifies return attribute value from request for given field name if the attribute value is
	 *           valid according to datataype
	 */
	@Test
	public void getValue_shouldReturnAttributeValueFromRequestForGivenFieldNameIfTheAttributeValueIsValidAccordingToDatatype() {
		
		// given
		String fieldName = "regexfield";
		String validFieldValue = "1";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(fieldName, validFieldValue);
		RegexValidatedTextDatatype datatype = new RegexValidatedTextDatatype();
		datatype.setConfiguration("^[012]$");
		
		assertThat(handler.getValue(datatype, request, fieldName), is(validFieldValue));
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(CustomDatatype,
	 *      HttpServletRequest, String)
	 * @verifies throw invalid custom value exception if attribute value from request for given
	 *           field name is invalid according to datatype
	 */
	@Test
	public void getValue_shouldThrowInvalidCustomValueExceptionIfAttributeValueFromRequestForGivenFieldNameIsInvalidAccordingToDatatype() {
		
		// given
		String fieldName = "regexfield";
		String invalidFieldValue = "9";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(fieldName, invalidFieldValue);
		RegexValidatedTextDatatype datatype = new RegexValidatedTextDatatype();
		datatype.setConfiguration("^[012]$");
		
		expectedException.expect(InvalidCustomValueException.class);
		expectedException.expectMessage("Invalid value: " + invalidFieldValue);
		handler.getValue(datatype, request, fieldName);
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      String)
	 * @verifies return the value reference
	 */
	@Test
	public void toHtml_shouldReturnTheValueReference() throws Exception {
		
		final String fieldValue = "1";
		
		RegexValidatedTextDatatype datatype = new RegexValidatedTextDatatype();
		datatype.setConfiguration("^[012]$");
		
		assertThat(handler.toHtml(datatype, fieldValue), is(fieldValue));
	}
	
	/**
	 * @verifies use the name in the html summary instance
	 * @see BaseMetadataFieldGenDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      String)
	 */
	@Test
	public void toHtmlSummary_shouldUseTheNameInTheHtmlSummaryInstance() throws Exception {
		
		final String fieldValue = "1";
		
		RegexValidatedTextDatatype datatype = new RegexValidatedTextDatatype();
		datatype.setConfiguration("^[012]$");
		
		CustomDatatype.Summary summary = handler.toHtmlSummary(datatype, fieldValue);
		
		assertNotNull(summary);
		assertEquals(fieldValue, summary.getSummary());
		assertEquals(true, summary.isComplete());
	}
}
