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

import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;

/**
 * Tests {@link PatientDashboardController}.
 */
public class PatientDashboardControllerTest extends BaseModuleWebContextSensitiveTest {
	
	
	private static final String PATIENT_FIND_VIEW = "module/legacyui/findPatient";
	
	private static final String PATIENT_DASHBOARD_VIEW = "module/legacyui/patientDashboardForm";
	
	private static final String EXISTING_PATIENT_ID = "2";
	
	private static final String EXISTING_PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String NON_EXISTING_PATIENT_ID = "999999999";
	
	private static final String NON_EXISTING_PATIENT_UUID = "99999999-27ce-4bb2-86d6-6d1d05312bd5";
	
	@Autowired
	PatientDashboardController patientDashboardController;
	
	private Method getPatientMethod;
	
	@Before
	public void setUp() throws NoSuchMethodException, SecurityException {
		getPatientMethod = PatientDashboardController.class.getDeclaredMethod("getPatient", new Class[] { String.class });
		getPatientMethod.setAccessible(true);
	}
	
	/**
	 * @see PatientDashboardController#renderDashboard(Integer,ModelMap,HttpServletRequest)
	 * @verifies render patient dashboard if given patient id is an existing id
	 */
	@Test
	public void renderDashboard_shouldRenderPatientDashboardIfGivenPatientIdIsAnExistingId() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap map = new ModelMap();
		
		String view = patientDashboardController.renderDashboard(EXISTING_PATIENT_ID, map, request);
		
		assertThat(view, is(PATIENT_DASHBOARD_VIEW));
		
		assertThat(map, hasKey("patient"));
		Patient patient = (Patient) map.get("patient");
		assertThat(patient.getPatientId(), is(Integer.valueOf(EXISTING_PATIENT_ID)));
		
		assertNull(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
		assertNull(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ARGS));
	}
	
	/**
	 * @see PatientDashboardController#renderDashboard(Integer,ModelMap,HttpServletRequest)
	 * @verifies render patient dashboard if given patient id is an existing uuid
	 */
	// @Test
	public void renderDashboard_shouldRenderPatientDashboardIfGivenPatientIdIsAnExistingUuid() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap map = new ModelMap();
		
		String view = patientDashboardController.renderDashboard(EXISTING_PATIENT_UUID, map, request);
		
		assertThat(view, is(PATIENT_DASHBOARD_VIEW));
		
		assertThat(map, hasKey("patient"));
		Patient patient = (Patient) map.get("patient");
		assertThat(patient.getUuid(), is(EXISTING_PATIENT_UUID));
		
		assertNull(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
		assertNull(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ARGS));
	}
	
	/**
	 * @see PatientDashboardController#renderDashboard(String,ModelMap,HttpServletRequest)
	 * @verifies redirect to find patient page if given patient id is not an existing id
	 */
	@Test
	public void renderDashboard_shouldRedirectToFindPatientPageIfGivenPatientIdIsNotAnExistingId() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap map = new ModelMap();
		
		String view = patientDashboardController.renderDashboard(NON_EXISTING_PATIENT_ID, map, request);
		
		assertThat(view, is(PATIENT_FIND_VIEW));
		
		assertThat(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR),
		    is("patientDashboard.noPatientWithId"));
		assertThat(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ARGS), is(NON_EXISTING_PATIENT_ID));
	}
	
	/**
	 * @see PatientDashboardController#renderDashboard(String,ModelMap,HttpServletRequest)
	 * @verifies redirect to find patient page if given patient id is not an existing uuid
	 */
	@Test
	public void renderDashboard_shouldRedirectToFindPatientPageIfGivenPatientIdIsNotAnExistingUuid() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap map = new ModelMap();
		
		String view = patientDashboardController.renderDashboard(NON_EXISTING_PATIENT_UUID, map, request);
		
		assertThat(view, is(PATIENT_FIND_VIEW));
		
		assertThat(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR),
		    is("patientDashboard.noPatientWithId"));
		assertThat(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ARGS), is(NON_EXISTING_PATIENT_UUID));
	}
	
	/**
	 * @see PatientDashboardController#getPatient(Integer)
	 * @verifies return patient if given patient id is an existing id
	 */
	@Test
	public void getPatient_shouldReturnPatientIfGivenPatientIdIsAnExistingId() throws Exception {
		
		Patient patient = (Patient) getPatientMethod.invoke(patientDashboardController, EXISTING_PATIENT_ID);
		
		assertThat(patient.getPatientId(), is(Integer.valueOf(EXISTING_PATIENT_ID)));
	}
	
	/**
	 * @see PatientDashboardController#getPatient(Integer)
	 * @verifies return patient if given patient id is an existing uuid
	 */
	@Test
	public void getPatient_shouldReturnPatientIfGivenPatientIdIsAnExistingUuid() throws Exception {
		
		Patient patient = (Patient) getPatientMethod.invoke(patientDashboardController, EXISTING_PATIENT_UUID);
		
		assertThat(patient.getUuid(), is(EXISTING_PATIENT_UUID));
	}
	
	/**
	 * @see PatientDashboardController#getPatient(String)
	 * @verifies return null if given null or whitespaces only
	 */
	@Test
	public void getPatient_shouldReturnNullIfGivenNullOrWhitespacesOnly() throws Exception {
		
		String nullString = null;
		assertNull(getPatientMethod.invoke(patientDashboardController, nullString));
		assertNull(getPatientMethod.invoke(patientDashboardController, ""));
		assertNull(getPatientMethod.invoke(patientDashboardController, "  "));
	}
	
	/**
	 * @see PatientDashboardController#getPatient(String)
	 * @verifies return null if given patient id is not an existing id
	 */
	@Test
	public void getPatient_shouldReturnNullIfGivenPatientIdIsNotAnExistingId() throws Exception {
		
		assertNull(getPatientMethod.invoke(patientDashboardController, NON_EXISTING_PATIENT_ID));
	}
	
	/**
	 * @see PatientDashboardController#getPatient(String)
	 * @verifies return null if given patient id is not an existing uuid
	 */
	@Test
	public void getPatient_shouldReturnNullIfGivenPatientIdIsNotAnExistingUuid() throws Exception {
		
		assertNull(getPatientMethod.invoke(patientDashboardController, NON_EXISTING_PATIENT_UUID));
	}
}
