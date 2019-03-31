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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.web.extension.ExtensionUtil;
import org.openmrs.module.web.extension.provider.Link;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.ApplicationPrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PatientDashboardController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * render the patient dashboard model and direct to the view
	 * 
	 * @should render patient dashboard if given patient id is an existing id
	 * @should render patient dashboard if given patient id is an existing uuid
	 * @should redirect to find patient page if given patient id is not an existing
	 *         id
	 * @should redirect to find patient page if given patient id is not an existing
	 *         uuid
	 */
	@RequestMapping("/patientDashboard.form")
	protected String renderDashboard(@RequestParam("patientId") String patientId, ModelMap map,
			HttpServletRequest request) throws Exception {

		Patient patient = getPatient(patientId);

		if (patient == null) {
			// redirect to the patient search page if no patient is found
			HttpSession session = request.getSession();
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "patientDashboard.noPatientWithId");
			session.setAttribute(WebConstants.OPENMRS_ERROR_ARGS, patientId);
			return "module/legacyui/findPatient";
		}

		log.debug("patient: '" + patient + "'");
		map.put("patient", patient);

		// determine cause of death

		String causeOfDeathOther = "";

		if (Context.isAuthenticated()) {
			String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			Concept conceptCause = Context.getConceptService().getConcept(propCause);

			if (conceptCause != null) {
				List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patient, conceptCause);
				if (obssDeath.size() == 1) {
					Obs obsDeath = obssDeath.iterator().next();
					causeOfDeathOther = obsDeath.getValueText();
					if (causeOfDeathOther == null) {
						log.debug("cod is null, so setting to empty string");
						causeOfDeathOther = "";
					} else {
						log.debug("cod is valid: " + causeOfDeathOther);
					}
				} else {
					log.debug("obssDeath is wrong size: " + obssDeath.size());
				}
			} else {
				log.debug("No concept cause found");
			}
		}

		// determine patient variation

		String patientVariation = "";
		if (patient.isDead()) {
			patientVariation = "Dead";
		}

		Concept reasonForExitConcept = Context.getConceptService()
				.getConcept(Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare"));

		if (reasonForExitConcept != null) {
			List<Obs> patientExitObs = Context.getObsService().getObservationsByPersonAndConcept(patient,
					reasonForExitConcept);
			if (patientExitObs != null) {
				log.debug("Exit obs is size " + patientExitObs.size());
				if (patientExitObs.size() == 1) {
					Obs exitObs = patientExitObs.iterator().next();
					Concept exitReason = exitObs.getValueCoded();
					Date exitDate = exitObs.getObsDatetime();
					if (exitReason != null && exitDate != null) {
						patientVariation = "Exited";
					}
				} else if (patientExitObs.size() > 1) {
					log.error("Too many reasons for exit - not putting data into model");
				}
			}
		}

		map.put("patientVariation", patientVariation);

		// empty objects used to create blank template in the view

		map.put("emptyIdentifier", new PatientIdentifier());
		map.put("emptyName", new PersonName());
		map.put("emptyAddress", new PersonAddress());
		map.put("causeOfDeathOther", causeOfDeathOther);

		Set<Link> links = ExtensionUtil.getAllAddEncounterToVisitLinks();
		map.put("allAddEncounterToVisitLinks", links);

		// Tabs
		List<TabInfo> tabs = new ArrayList<TabInfo>();

		boolean visitsEnabled = Context.getAdministrationService()
				.getGlobalPropertyValue(OpenmrsConstants.GLOBAL_PROPERTY_ENABLE_VISITS, true);
		boolean formEntryEnabled = Boolean
				.valueOf(Context.getAdministrationService().getGlobalProperty("FormEntry.enableDashboardTab"));
		String tabOrder = Context.getAdministrationService().getGlobalProperty("legacyui.dashboardTabs");
		if (tabOrder != null)
			tabOrder = tabOrder.toLowerCase().replace("formentry", "formEntry");

		String[] tabNames = (tabOrder == null)
				? new String[] { "overview", "visits", "demographics", "graphs", "formEntry" }
				: StringUtils.split(tabOrder, ",");

		HashMap<String, String> tabConstants = new HashMap<String, String>();
		tabConstants.put("overview", ApplicationPrivilegeConstants.DASHBOARD_OVERVIEW);
		tabConstants.put("visits", ApplicationPrivilegeConstants.DASHBOARD_VISITS);
		tabConstants.put("encounters", ApplicationPrivilegeConstants.DASHBOARD_ENCOUNTERS);
		tabConstants.put("demographics", ApplicationPrivilegeConstants.DASHBOARD_VISITS);
		tabConstants.put("graphs", ApplicationPrivilegeConstants.DASHBOARD_GRAPHS);
		tabConstants.put("formEntry", ApplicationPrivilegeConstants.DASHBOARD_FORMS);

		for (String tabName : tabNames) {
			String tabConstant = tabConstants.get(tabName);

			if (tabConstant == null || tabName.equals("encounters")
					|| (tabName.equals("formEntry") && !formEntryEnabled))
				continue;

			if (tabName.equals("visits") && visitsEnabled) {
				tabName = "encounters";
				tabConstant = tabConstants.get(tabName);
			}

			String tabID = tabName.equals("formEntry") ? "formEntryTab"
					: ("patient" + tabName.substring(0, 1).toUpperCase() + tabName.substring(1) + "Tab");
			tabs.add(new TabInfo(tabID, "patientDashboard." + tabName, tabConstant));
		}

		map.put("tabs", tabs);

		return "module/legacyui/patientDashboardForm";
	}

	/**
	 * Get {@code Patient} by ID or UUID string.
	 * 
	 * @param patientId the id or uuid of wanted patient
	 * @return patient matching given patient id
	 * @should return patient if given patient id is an existing id
	 * @should return patient if given patient id is an existing uuid
	 * @should return null if given null or whitespaces only
	 * @should return null if given patient id is not an existing id
	 * @should return null if given patient id is not an existing uuid
	 */
	private Patient getPatient(String patientId) {

		if (StringUtils.isBlank(patientId)) {
			return null;
		}

		PatientService ps = Context.getPatientService();
		Patient patient = null;
		try {
			patient = ps.getPatient(Integer.valueOf(patientId));
		} catch (Exception ex) {
			patient = ps.getPatientByUuid(patientId);
		}
		return patient;
	}

	public class TabInfo {

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getPrivilege() {
			return privilege;
		}

		public void setPrivilege(String privilege) {
			this.privilege = privilege;
		}

		private String id;

		private String code;

		private String privilege;

		public TabInfo(String id, String code, String privilege) {
			super();
			this.id = id;
			this.code = code;
			this.privilege = privilege;
		}

	}
}