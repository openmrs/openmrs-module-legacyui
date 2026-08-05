/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

import java.util.List;
import java.util.Map;
import org.openmrs.Obs;

public class EncounterFormControllerTest extends BaseModuleWebContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	protected static final String TRANSFER_ENC_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-transferEncounter.xml";
	
	/**
	 * @see EncounterFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "transfer encounter to another patient when encounter patient was changed", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldSaveANewEncounterRoleObject() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		executeDataSet(TRANSFER_ENC_DATA_XML);
		
		EncounterFormController controller = new EncounterFormController();
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", "201");
		
		Encounter encounter = Context.getEncounterService().getEncounter(200);
		
		Patient oldPatient = encounter.getPatient();
		Patient newPatient = Context.getPatientService().getPatient(201);
		Assertions.assertNotEquals(oldPatient, newPatient);
		
		List<Encounter> newEncounter = Context.getEncounterService().getEncountersByPatientId(newPatient.getPatientId());
		Assertions.assertEquals(0, newEncounter.size());
		
		BindException errors = new BindException(encounter, "encounterRole");
		
		controller.onSubmit(request, response, encounter, errors);
		
		Assertions.assertEquals(true, encounter.isVoided());
		newEncounter = Context.getEncounterService().getEncountersByPatientId(newPatient.getPatientId());
		Assertions.assertEquals(1, newEncounter.size());
		Assertions.assertEquals(false, newEncounter.get(0).isVoided());
	}
	
	@Test
	public void referenceData_shouldIncludeFullyArchivedGroupMembersInMap() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		
		try {
			Context.getAdministrationService().executeSQL(
				"INSERT INTO obs_archive (obs_id, person_id, concept_id, encounter_id, obs_datetime, voided, uuid, creator, date_created, status) VALUES (998, 2, 21, 3, '2026-01-01', 1, 'uuid-998', 1, '2026-01-01', 'FINAL')", false);
			Context.getAdministrationService().executeSQL(
				"INSERT INTO obs_archive (obs_id, person_id, concept_id, encounter_id, obs_datetime, voided, uuid, creator, date_created, status, obs_group_id) VALUES (999, 2, 21, 3, '2026-01-01', 1, 'uuid-999', 1, '2026-01-01', 'FINAL', 998)", false);
			Context.getAdministrationService().setGlobalProperty("obs.archive.last_processed_obs_id", "999");
			
			EncounterFormController controller = new EncounterFormController();
			MockHttpServletRequest request = new MockHttpServletRequest();
			
			Map<String, Object> map = controller.referenceData(request, encounter, new BindException(encounter, "encounter"));
			
			@SuppressWarnings("unchecked")
			Map<Obs, List<Obs>> groupMembersMap = (Map<Obs, List<Obs>>) map.get("groupMembersMap");
			
			boolean foundArchivedParent = false;
			boolean foundArchivedChild = false;
			
			if (groupMembersMap != null) {
				for (Map.Entry<Obs, List<Obs>> entry : groupMembersMap.entrySet()) {
					if (entry.getKey().getObsId().equals(998)) {
						foundArchivedParent = true;
						for (Obs child : entry.getValue()) {
							if (child.getObsId().equals(999)) {
								foundArchivedChild = true;
							}
						}
					}
				}
			}
			
			Assertions.assertTrue(foundArchivedParent, "Archived parent should be in groupMembersMap");
			Assertions.assertTrue(foundArchivedChild, "Archived child should be in groupMembersMap");
		} finally {
			Context.getAdministrationService().executeSQL("DELETE FROM obs_archive WHERE obs_id IN (998, 999)", false);
		}
	}
}
