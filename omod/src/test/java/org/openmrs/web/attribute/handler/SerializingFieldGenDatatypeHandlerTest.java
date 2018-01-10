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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.ConceptDatatype;
import org.openmrs.customdatatype.datatype.LocationDatatype;
import org.openmrs.customdatatype.datatype.MockLocationDatatype;
import org.openmrs.customdatatype.datatype.ProgramDatatype;
import org.openmrs.customdatatype.datatype.ProviderDatatype;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

public class SerializingFieldGenDatatypeHandlerTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @verifies return the correct typed value
	 * @see SerializingFieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.SerializingCustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, String)
	 */
	@Test
	public void getValue_shouldReturnTheCorrectTypedValue() throws Exception {
		final String locationUuid = "some uuid";
		final String formFieldName = "uuid";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, locationUuid);
		Location expectedLocation = mock(Location.class);
		MockLocationDatatype datatype = mock(MockLocationDatatype.class);
		when(datatype.deserialize(eq(locationUuid))).thenReturn(expectedLocation);
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();
		Assert.assertEquals(expectedLocation, handler.getValue(datatype, request, formFieldName));
	}

	@Test
	public void getValue_givenEmptyValue_shouldReturnNull() throws Exception {
		final String locationUuid = "";
		final String formFieldName = "uuid";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, locationUuid);
		MockLocationDatatype datatype = mock(MockLocationDatatype.class);
		Location expectedLocation = mock(Location.class);
		when(datatype.deserialize(eq(locationUuid))).thenReturn(expectedLocation);
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();
		Assert.assertNull(handler.getValue(datatype, request, formFieldName));
	}

	@Test
	public void getValue_givenLocationId_shouldCallDeserializeWithLocationUuid() throws Exception {
		Location testedLocation = Context.getLocationService().getLocation(1);

		final String locationId = String.valueOf(testedLocation.getLocationId());
		final String formFieldName = "id";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, locationId);
		LocationDatatype datatype = mock(LocationDatatype.class);
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();

		// should call deserialize() with location uuid
		handler.getValue(datatype, request, formFieldName);
		verify(datatype).deserialize(eq(testedLocation.getUuid()));
	}

	@Test
	public void getValue_givenProgramId_shouldCallDeserializeWithProgramUuid() throws Exception {
		Program testedProgram = Context.getProgramWorkflowService().getProgram(1);

		final String programId = String.valueOf(testedProgram.getProgramId());
		final String formFieldName = "id";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, programId);
		ProgramDatatype datatype = mock(ProgramDatatype.class);
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();

		// should call deserialize() with program uuid
		handler.getValue(datatype, request, formFieldName);
		verify(datatype).deserialize(eq(testedProgram.getUuid()));
	}

	@Test
	public void getValue_givenConceptId_shouldCallDeserializeWithConceptUuid() throws Exception {
		Concept testedConcept = Context.getConceptService().getConcept(3);

		final String conceptId = String.valueOf(testedConcept.getConceptId());
		final String formFieldName = "id";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, conceptId);
		ConceptDatatype datatype = mock(ConceptDatatype.class);

		// should call deserialize() with concept uuid
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();
		handler.getValue(datatype, request, formFieldName);
		verify(datatype).deserialize(eq(testedConcept.getUuid()));
	}

	@Test
	public void getValue_givenProviderId_shouldCallDeserializeWithProviderUuid() throws Exception {
		Provider testedProvider = Context.getProviderService().getProvider(1);

		final String providerId = String.valueOf(testedProvider.getProviderId());
		final String formFieldName = "id";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(formFieldName, providerId);
		ProviderDatatype datatype = mock(ProviderDatatype.class);

		// should call deserialize() with provider uuid
		SerializingFieldGenDatatypeHandler handler = new MockLocationFieldGenDatatypeHandler();
		handler.getValue(datatype, request, formFieldName);
		verify(datatype).deserialize(eq(testedProvider.getUuid()));
	}
}
