/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.patient;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;

import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

/**
 * Consists of unit tests for the PatientFormController
 *
 * @see PatientFormController
 */
public class PatientFormControllerTest extends BaseModuleWebContextSensitiveTest {

	/**
	 * Convenience method to have a Patient object with all required values filled in
	 *
	 * @return a mock Patient object that can be saved
	 */
	private Patient createBasicPatient() {
		Patient patient = new Patient();

		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);

		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		// patient.removeAddress(pAddress);

		patient.setDead(true);
		patient.setDeathDate(new Date());
		List<GlobalProperty> props = new ArrayList<GlobalProperty>();
		props.add(new GlobalProperty("concept.causeOfDeath", "3"));
		Context.getAdministrationService().saveGlobalProperties(props);
		patient.setCauseOfDeath(Context.getConceptService().getConcept(3));
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("male");
		patient.setDeathdateEstimated(true);

		PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(1);
		PatientIdentifier ident1 = new PatientIdentifier("123-A", pit, Context.getLocationService().getLocation(2));
		ident1.setPreferred(true);
		patient.addIdentifier(ident1);


		return patient;
	}

	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "void patient when void reason is not empty", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldVoidPatientWhenVoidReasonIsNotEmpty() throws Exception {

		Patient p = Context.getPatientService().getPatient(2);

		HttpServletResponse response = new MockHttpServletResponse();

		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.void");
		request.setParameter("voidReason", "some reason");
		BindException errors = new BindException(p, "patient");
		ModelAndView modelAndview = controller.onSubmit(request, response, p, errors);

		Assert.assertTrue(p.isVoided());
	}

	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "not void patient when void reason is empty", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldNotVoidPatientWhenVoidReasonIsEmpty() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);

		HttpServletResponse response = new MockHttpServletResponse();

		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.void");
		request.setParameter("voidReason", "");
		BindException errors = new BindException(p, "patient");
		ModelAndView modelAndview = controller.onSubmit(request, response, p, errors);

		Assert.assertTrue(!p.isVoided());
		String tmp = request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR).toString();
		Assert.assertEquals(tmp, "Patient.error.void.reasonEmpty");
	}

	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "should delete patient if patient exist when action is deleteion", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldDeletePatientIfPatientExistIfActionIsDeletion() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		Assert.assertNotNull(p);

		HttpServletResponse response = new MockHttpServletResponse();

		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.delete");
		BindException errors = new BindException(p, "patient");
		controller.onSubmit(request, response, p, errors);

		Assert.assertNull(Context.getPatientService().getPatient(2));
		String tmp = request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR).toString();
		Assert.assertEquals(tmp, "Patient.deleted");
	}

	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "should unvoid patient if patient exist when action is unvoid", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldUnvoidPatientIfPatientExistIfActionIsUnvoid() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		Assert.assertNotNull(p);

		HttpServletResponse response = new MockHttpServletResponse();

		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.void");
		request.setParameter("voidReason", "some reason");
		BindException errors = new BindException(p, "patient");
		controller.onSubmit(request, response, p, errors);
		Assert.assertTrue(Context.getPatientService().getPatient(2).isVoided());

		request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.unvoid");
		controller.onSubmit(request, response, p, errors);
		Assert.assertFalse(Context.getPatientService().getPatient(2).isVoided());

		String tmp = request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR).toString();
		Assert.assertEquals(tmp, "Patient.unvoided");
	}

	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "should add patient", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldAddPatientWithActionAdd() throws Exception {
		Patient p = createBasicPatient();
		Assert.assertNotNull(p);


		//int size = Context.getPatientService().getAllPatients().size();
		HttpServletResponse response = new MockHttpServletResponse();

		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.add");
		BindException errors = new BindException(p, "patient");
		controller.onSubmit(request, response, p, errors);

		//Assert that patient is added
		Assert.assertTrue(Context.getPatientService().getAllPatients().contains(p));
	}
}
