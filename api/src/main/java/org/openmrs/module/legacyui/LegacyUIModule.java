/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/**
 * 
 */
package org.openmrs.module.legacyui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.openmrs.Patient;
// import org.openmrs.propertyeditor.PatientEditor;


/**
 * @author Admin
 *
 */
public class LegacyUIModule {
	
	//protected Patient patient = patient.duplicateName.voided={0};
	
	protected Log log = LogFactory.getLog(getClass());
	
	public void providerAccount () {
		
		log.info ("Provider Account");
	}
	
	public void createProviderAccount () {
		log.info ("Create a Provider account for this user");
	}
	
	public void providerIdentifier () {
		log.info ("Provder Identifier(s) :");
	}
	
	public void noProviderIdentifier () {
		log.info ("No Identifier specified");
	}
	
	public void purgeLocation () {
		log.info ("Permanently delete Location");
	}
	
	public void confirmDelete () {
		log.info ("Are you sure you want to delete this location? It will be permanently removed from the system");
	}
	
	public void purgedSuccessfully () {
		log.info ("Location deleted Successfully");
	}

}
