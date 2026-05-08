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
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;

/**
 * Defines the services provided by the Legacy UI module
 */
public interface LegacyUIService extends OpenmrsService {
	
	/**
	 * Records that a patient has exited care.
	 * <p>
	 * <b>Should</b> save reason for exit observation for given patient.<br>
	 * <b>Should</b> set death date and cause when given reason for exit equals death.<br>
	 * <b>Should</b> terminate all program workflows associated with given patient.<br>
	 * <b>Should</b> throw error when given patient is null.<br>
	 * <b>Should</b> throw error when given date exited is null.<br>
	 * <b>Should</b> throw error when given reason for exist is null.<br>
	 * <b>Should</b> be tested more thoroughly.
	 * 
	 * @deprecated as of 1.10 and moved to exit from care module. This method is no longer supported
	 *             because previously the patient's active orders would get discontinued in the
	 *             process which is no longer happening
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient has been declared as
	 *            exited
	 * @throws APIException if any input parameter is invalid
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.EDIT_PATIENTS })
	public void exitFromCare(Patient patient, Date dateExited, Concept reasonForExit) throws APIException;
	
	/**
	 * Triggers a ConceptStateConversion for the given patient.
	 * <p>
	 * <b>Should</b> trigger state conversion successfully.<br>
	 * <b>Should</b> fail if patient is invalid.<br>
	 * <b>Should</b> fail if trigger is invalid.<br>
	 * <b>Should</b> fail if date converted is invalid.<br>
	 * <b>Should</b> skip past patient programs that are already completed.
	 * 
	 * @deprecated as of 1.10, because the only place in core where it was called was
	 *             PatientService#exitFromCare(Patient patient, Date dateExited, Concept
	 *             reasonForExit) which was moved to exit from care module
	 * @param patient - the Patient to trigger the ConceptStateConversion on
	 * @param reasonForExit - the Concept to trigger the ConceptStateConversion
	 * @param dateConverted - the Date of the ConceptStateConversion
	 * @throws APIException if any input parameter is invalid
	 */
	@Deprecated
	public void triggerStateConversion(Patient patient, Concept reasonForExit, Date dateConverted) throws APIException;
	
	/**
	 * Returns the first active {@link Provider} account associated with the given user.
	 * 
	 * @param user the user whose provider account to look up
	 * @return the provider associated with the given user
	 */
	public Provider getProviderForUser(User user);
}
