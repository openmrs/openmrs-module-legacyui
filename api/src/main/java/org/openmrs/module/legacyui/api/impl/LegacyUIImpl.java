/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.legacyui.api.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.legacyui.GeneralUtils;
import org.openmrs.module.legacyui.api.LegacyUIService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the patient service. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context via
 * <code>Context.getPatientService()</code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.PatientService
 * @see org.openmrs.api.PersonService
 */
@Transactional
public class LegacyUIImpl extends BaseOpenmrsService implements LegacyUIService {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * This is the way to establish that a patient has left the care center. This API call is
	 * responsible for:
	 * <ol>
	 * <li>Closing workflow statuses</li>
	 * <li>Terminating programs</li>
	 * <li>Discontinuing orders</li>
	 * <li>Flagging patient table</li>
	 * <li>Creating any relevant observations about the patient (if applicable)</li>
	 * </ol>
	 * 
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient has been declared as
	 *            exited
	 * @throws APIException
	 */
	public void exitFromCare(Patient patient, Date dateExited, Concept reasonForExit) throws APIException {
		
		if (patient == null) {
			throw new APIException("Attempting to exit from care an invalid patient. Cannot proceed");
		}
		if (dateExited == null) {
			throw new APIException("Must supply a valid dateExited when indicating that a patient has left care");
		}
		if (reasonForExit == null) {
			throw new APIException(
			        "Must supply a valid reasonForExit (even if 'Unknown') when indicating that a patient has left care");
		}
		
		// need to create an observation to represent this (otherwise how
		// will we know?)
		saveReasonForExitObs(patient, dateExited, reasonForExit);
		
		// need to terminate any applicable programs
		triggerStateConversion(patient, reasonForExit, dateExited);
		
		// need to discontinue any open orders for this patient
		GeneralUtils.discontinueAllDrugOrders(patient, reasonForExit, dateExited);
	}
	
	/**
	 * TODO: Patients should actually be allowed to exit multiple times
	 * 
	 * @param patient
	 * @param exitDate
	 * @param cause
	 */
	private void saveReasonForExitObs(Patient patient, Date exitDate, Concept cause) throws APIException {
		
		if (patient == null) {
			throw new APIException("Patient supplied to method is null");
		}
		if (exitDate == null) {
			throw new APIException("Exit date supplied to method is null");
		}
		if (cause == null) {
			throw new APIException("Cause supplied to method is null");
		}
		
		// need to make sure there is an Obs that represents the patient's
		// exit
		log.debug("Patient is exiting, so let's make sure there's an Obs for it");
		
		String codProp = Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare");
		Concept reasonForExit = Context.getConceptService().getConcept(codProp);
		
		if (reasonForExit != null) {
			List<Obs> obssExit = Context.getObsService().getObservationsByPersonAndConcept(patient, reasonForExit);
			if (obssExit != null) {
				if (obssExit.size() > 1) {
					log.error("Multiple reasons for exit (" + obssExit.size() + ")?  Shouldn't be...");
				} else {
					Obs obsExit = null;
					if (obssExit.size() == 1) {
						// already has a reason for exit - let's edit it.
						log.debug("Already has a reason for exit, so changing it");
						
						obsExit = obssExit.iterator().next();
						
					} else {
						// no reason for exit obs yet, so let's make one
						log.debug("No reason for exit yet, let's create one.");
						
						obsExit = new Obs();
						obsExit.setPerson(patient);
						obsExit.setConcept(reasonForExit);
						
						Location loc = Context.getLocationService().getDefaultLocation();
						
						if (loc != null) {
							obsExit.setLocation(loc);
						} else {
							log.error("Could not find a suitable location for which to create this new Obs");
						}
					}
					
					if (obsExit != null) {
						// put the right concept and (maybe) text in this
						// obs
						obsExit.setValueCoded(cause);
						obsExit.setValueCodedName(cause.getName()); // ABKTODO: presume current locale?
						obsExit.setObsDatetime(exitDate);
						Context.getObsService().saveObs(obsExit, "updated by PatientService.saveReasonForExit");
					}
				}
			}
		} else {
			log.debug("Reason for exit is null - should not have gotten here without throwing an error on the form.");
		}
		
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#triggerStateConversion(org.openmrs.Patient,
	 *      org.openmrs.Concept, java.util.Date)
	 */
	public void triggerStateConversion(Patient patient, Concept trigger, Date dateConverted) {
		
		// Check input parameters
		if (patient == null) {
			throw new APIException("Attempting to convert state of an invalid patient");
		}
		if (trigger == null) {
			throw new APIException("Attempting to convert state for a patient without a valid trigger concept");
		}
		if (dateConverted == null) {
			throw new APIException("Invalid date for converting patient state");
		}
		
		for (PatientProgram patientProgram : Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null, null, null, false)) {
			//skip past patient programs that already completed
			if (patientProgram.getDateCompleted() == null) {
				Set<ProgramWorkflow> workflows = patientProgram.getProgram().getWorkflows();
				for (ProgramWorkflow workflow : workflows) {
					// (getWorkflows() is only returning over nonretired workflows)
					PatientState patientState = patientProgram.getCurrentState(workflow);
					
					// #1080 cannot exit patient from care  
					// Should allow a transition from a null state to a terminal state
					// Or we should require a user to ALWAYS add an initial workflow/state when a patient is added to a program
					ProgramWorkflowState currentState = (patientState != null) ? patientState.getState() : null;
					ProgramWorkflowState transitionState = workflow.getState(trigger);
					
					log.debug("Transitioning from current state [" + currentState + "]");
					log.debug("|---> Transitioning to final state [" + transitionState + "]");
					
					if (transitionState != null && workflow.isLegalTransition(currentState, transitionState)) {
						patientProgram.transitionToState(transitionState, dateConverted);
						log.debug("State Conversion Triggered: patientProgram=" + patientProgram + " transition from "
						        + currentState + " to " + transitionState + " on " + dateConverted);
					}
				}
				
				// #1068 - Exiting a patient from care causes "not-null property references
				// a null or transient value: org.openmrs.PatientState.dateCreated". Explicitly
				// calling the savePatientProgram() method will populate the metadata properties.
				// 
				// #1067 - We should explicitly save the patient program rather than let 
				// Hibernate do so when it flushes the session.
				Context.getProgramWorkflowService().savePatientProgram(patientProgram);
			}
		}
	}
}
