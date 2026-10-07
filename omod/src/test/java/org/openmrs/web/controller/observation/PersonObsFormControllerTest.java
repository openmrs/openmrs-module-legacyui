/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.observation;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ObsArchiveHelper;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

public class PersonObsFormControllerTest extends BaseModuleWebContextSensitiveTest {

	@Test
	public void formBackingObject_shouldIncludeArchivedObs() throws Exception {
		PersonObsFormController controller = new PersonObsFormController();
		
		try {
			Context.getAdministrationService().executeSQL(
				"INSERT INTO obs_archive (obs_id, person_id, concept_id, encounter_id, obs_datetime, voided, uuid, creator, date_created, status) VALUES (994, 2, 21, 3, '2008-09-01', 1, 'archive-uuid-2', 1, '2026-01-01', 'FINAL')", false);
			Context.getAdministrationService().executeSQL(
				"INSERT INTO obs_archive (obs_id, person_id, concept_id, encounter_id, obs_datetime, voided, uuid, creator, date_created, status) VALUES (993, 2, 5089, 3, '2008-09-01', 1, 'archive-uuid-3', 1, '2026-01-01', 'FINAL')", false);
			Context.getRegisteredComponent("obsArchiveHelper", ObsArchiveHelper.class)
			        .markArchiveHasData();
			
			// Test branch: person only
			MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "");
			request1.setParameter("personId", "2");
			PersonObsFormController.CommandObject commandObj1 = controller.formBackingObject(request1);
			List<Obs> obsList1 = commandObj1.getObservations();
			
			boolean foundArchived1 = false;
			for (Obs obs : obsList1) {
				if (obs.getObsId().equals(994)) {
					foundArchived1 = true;
					break;
				}
			}
			assertTrue(foundArchived1, "Archived obs should be included when searching by person");

			// Test branch: person + concept
			MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "");
			request2.setParameter("personId", "2");
			request2.setParameter("conceptId", "21");
			PersonObsFormController.CommandObject commandObj2 = controller.formBackingObject(request2);
			List<Obs> obsList2 = commandObj2.getObservations();
			
			boolean foundArchived2 = false;
			for (Obs obs : obsList2) {
				assertNotEquals(993, obs.getObsId().intValue(), "archived obs of another concept returned for concept 21");
				if (obs.getObsId().equals(994)) {
					foundArchived2 = true;
				}
			}
			assertTrue(foundArchived2, "Archived obs should be included when searching by person and concept");
			
		} finally {
			Context.getAdministrationService().executeSQL("DELETE FROM obs_archive WHERE obs_id IN (993, 994);", false);
		}
	}
}
