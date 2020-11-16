/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.legacyui.api;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;

/**
 * Contains methods pertaining to Patients in the system
 * 
 * <pre>
 * Usage:
 * List&lt;Patient&gt; patients = Context.getPatientService().getAllPatients();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
public interface LegacyUIService extends OpenmrsService {
	
	/**
	 * @deprecated as of 1.10 and moved to exit from care module. This method is no longer supported
	 *             because previously the patient's active orders would get discontinued in the
	 *             process which is no longer happening
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient has been declared as
	 *            exited
	 * @throws APIException
	 * @should save reason for exit observation for given patient
	 * @should set death date and cause when given reason for exit equals death
	 * @should terminate all program workflows associated with given patient
	 * @should throw error when given patient is null
	 * @should throw error when given date exited is null
	 * @should throw error when given reason for exist is null
	 * @should be tested more thoroughly
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.EDIT_PATIENTS })
	public void exitFromCare(Patient patient, Date dateExited, Concept reasonForExit) throws APIException;
	
	/**
	 * @deprecated as of 1.10, because the only place in core where it was called was
	 *             PatientService#exitFromCare(Patient patient, Date dateExited, Concept
	 *             reasonForExit) which was moved to exit from care module
	 * @param patient - the Patient to trigger the ConceptStateConversion on
	 * @param reasonForExit - the Concept to trigger the ConceptStateConversion
	 * @param dateConverted - the Date of the ConceptStateConversion
	 * @throws APIException
	 * @should trigger state conversion successfully
	 * @should fail if patient is invalid
	 * @should fail if trigger is invalid
	 * @should fail if date converted is invalid
	 * @should skip past patient programs that are already completed
	 */
	@Deprecated
	public void triggerStateConversion(Patient patient, Concept reasonForExit, Date dateConverted) throws APIException;
}
