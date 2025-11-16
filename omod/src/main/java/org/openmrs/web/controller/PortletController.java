/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.legacyui.GeneralUtils;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PortletController implements Controller {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	private static final int DEFAULT_PAGE_SIZE = 50;
	
	/**
	 * This method produces a model containing the following mappings:
	 * 
	 * <pre>
	 *     (always)
	 *          (java.util.Date) now
	 *          (String) size
	 *          (Locale) locale
	 *          (String) portletUUID // unique for each instance of any portlet
	 *          (other parameters)
	 *     (if there's currently an authenticated user)
	 *          (User) authenticatedUser
	 *     (if the request has a patientId attribute)
	 *          (Integer) patientId
	 *          (Patient) patient
	 *          (List&lt;Obs&gt;) patientObs
	 *          (List&lt;Encounter&gt;) patientEncounters
	 *          (List&lt;Visit&gt;) patientVisits
	 *          (List&lt;Visit&gt;) activeVisits
	 *          (Obs) patientWeight // most recent weight obs
	 *          (Obs) patientHeight // most recent height obs
	 *          (Double) patientBmi // BMI derived from most recent weight and most recent height
	 *          (String) patientBmiAsString // BMI rounded to one decimal place, or "?" if unknown
	 *          (Integer) personId
	 *     (if the request has a personId or patientId attribute)
	 *          (Person) person
	 *          (List&lt;Relationship&gt;) personRelationships
	 *          (Map&lt;RelationshipType, List&lt;Relationship&gt;&gt;) personRelationshipsByType
	 *     (if the request has an encounterId attribute)
	 *          (Integer) encounterId
	 *          (Encounter) encounter
	 *          (Set&lt;Obs&gt;) encounterObs
	 *     (if the request has a userId attribute)
	 *          (Integer) userId
	 *          (User) user
	 *     (if the request has a patientIds attribute, which should be a (String) comma-separated list of patientIds)
	 *          (PatientSet) patientSet
	 *          (String) patientIds
	 *     (if the request has a conceptIds attribute, which should be a (String) commas-separated list of conceptIds)
	 *          (Map&lt;Integer, Concept&gt;) conceptMap
	 *          (Map&lt;String, Concept&gt;) conceptMapByStringIds
	 * </pre>
	 * 
	 * @should calculate bmi into patientBmiAsString
	 * @should not fail with empty height and weight properties
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		
		AdministrationService as = Context.getAdministrationService();
		ConceptService cs = Context.getConceptService();
		
		// find the portlet that was identified in the openmrs:portlet taglib
		Object uri = request.getAttribute("javax.servlet.include.servlet_path");
		String portletPath = "";
		Map<String, Object> model = null;
		
		{
			HttpSession session = request.getSession();
			String uniqueRequestId = (String) request.getAttribute(WebConstants.INIT_REQ_UNIQUE_ID);
			String lastRequestId = (String) session.getAttribute(WebConstants.OPENMRS_PORTLET_LAST_REQ_ID);
			if (uniqueRequestId.equals(lastRequestId)) {
				model = (Map<String, Object>) session.getAttribute(WebConstants.OPENMRS_PORTLET_CACHED_MODEL);
				
				// remove cached parameters
				List<String> parameterKeys = (List<String>) model.get("parameterKeys");
				if (parameterKeys != null) {
					for (String key : parameterKeys) {
						model.remove(key);
					}
				}
			}
			if (model == null) {
				log.debug("creating new portlet model");
				model = new HashMap<String, Object>();
				session.setAttribute(WebConstants.OPENMRS_PORTLET_LAST_REQ_ID, uniqueRequestId);
				session.setAttribute(WebConstants.OPENMRS_PORTLET_CACHED_MODEL, model);
			}
		}
		
		if (uri != null) {
			long timeAtStart = System.currentTimeMillis();
			portletPath = uri.toString();
			
			// Allowable extensions are '' (no extension) and '.portlet'
			if (portletPath.endsWith("portlet")) {
				portletPath = portletPath.replace(".portlet", "");
			} else if (portletPath.endsWith("jsp")) {
				throw new ServletException(
				        "Illegal extension used for portlet: '.jsp'. Allowable extensions are '' (no extension) and '.portlet'");
			}
			
			log.debug("Loading portlet: " + portletPath);
			
			String id = (String) request.getAttribute("org.openmrs.portlet.id");
			String size = (String) request.getAttribute("org.openmrs.portlet.size");
			Map<String, Object> params = (Map<String, Object>) request.getAttribute("org.openmrs.portlet.parameters");
			Map<String, Object> moreParams = (Map<String, Object>) request.getAttribute("org.openmrs.portlet.parameterMap");
			
			model.put("now", new Date());
			model.put("id", id);
			model.put("size", size);
			model.put("locale", Context.getLocale());
			model.put("portletUUID", UUID.randomUUID().toString().replace("-", ""));
			List<String> parameterKeys = new ArrayList<String>(params.keySet());
			model.putAll(params);
			if (moreParams != null) {
				model.putAll(moreParams);
				parameterKeys.addAll(moreParams.keySet());
			}
			model.put("parameterKeys", parameterKeys); // so we can clean these up in the next request
			
			// if there's an authenticated user, put them, and their patient set, in the
			// model
			if (Context.getAuthenticatedUser() != null) {
				model.put("authenticatedUser", Context.getAuthenticatedUser());
			}
			
			Integer personId = null;
			
			// if a patient id is available, put patient data documented above in the model
			Object o = request.getAttribute("org.openmrs.portlet.patientId");
			if (o != null) {
				String patientVariation = "";
				Integer patientId = (Integer) o;
				if (!model.containsKey("patient") && Context.hasPrivilege(PrivilegeConstants.GET_PATIENTS)) {
					// we can't continue if the user can't view patients
					Patient p = Context.getPatientService().getPatient(patientId);
					model.put("patient", p);
					if (p.isDead()) {
						patientVariation = "Dead";
					}
					
					// add encounters if this user can view them
					if (Context.hasPrivilege(PrivilegeConstants.GET_ENCOUNTERS)) {
						Integer limit = getLimitParameter(request, as, "dashboard.encounters.maximumNumberToShow");
						Integer startIndex = getStartIndexParameter(request);
						
						model.put("patientEncounters",
						    Context.getEncounterService().getEncounters(null, p.getPatientId(), startIndex, limit, false));
					}
					
					// add visits if this user can view them
					if (Context.hasPrivilege(PrivilegeConstants.GET_VISITS)) {
						model.put("person", p);
						PortletControllerUtil.addFormToEditAndViewUrlMaps(model);
						model.put("patientVisits", Context.getVisitService().getVisitsByPatient(p));
						model.put("activeVisits", Context.getVisitService().getActiveVisitsByPatient(p));
					}
					
					if (Context.hasPrivilege(PrivilegeConstants.GET_OBS)) {
						Integer limit = getLimitParameter(request, as, "dashboard.observations.maximumNumberToShow");
						Integer startIndex = getStartIndexParameter(request);
						
						Person person = (Person) p;
						List<Person> persons = Collections.singletonList(person);
						
						// Get paginated observations for display
						List<Obs> paginatedObs = Context.getObsService().getObservations(persons, null, null, null, null,
						    null, Collections.singletonList("obsDatetime desc"), limit, startIndex, null, null, false);
						
						model.put("patientObs", paginatedObs);
						
						// Handle BMI calculation
						String bmiAsString = "?";
						
						try {
							String weightString = as.getGlobalProperty("concept.weight");
							String heightString = as.getGlobalProperty("concept.height");
							
							ConceptNumeric weightConcept = null;
							ConceptNumeric heightConcept = null;
							Obs latestWeight = null;
							Obs latestHeight = null;
							
							if (StringUtils.hasLength(weightString)) {
								weightConcept = cs.getConceptNumeric(GeneralUtils.getConcept(weightString).getConceptId());
								List<Obs> weightObs = Context.getObsService().getObservations(persons, null,
									Collections.singletonList(weightConcept), null, null, null,
									Collections.singletonList("obsDatetime desc"), 1, null, null, null, false);
								if (!weightObs.isEmpty()) {
									latestWeight = weightObs.get(0);
									model.put("patientWeight", latestWeight);
								}
							}
							
							if (StringUtils.hasLength(heightString)) {
								heightConcept = cs.getConceptNumeric(GeneralUtils.getConcept(heightString).getConceptId());
								List<Obs> heightObs = Context.getObsService().getObservations(persons, null,
									Collections.singletonList(heightConcept), null, null, null,
									Collections.singletonList("obsDatetime desc"), 1, null, null, null, false);
								if (!heightObs.isEmpty()) {
									latestHeight = heightObs.get(0);
									model.put("patientHeight", latestHeight);
								}
							}
							
							if (latestWeight != null && latestHeight != null && weightConcept != null
							        && heightConcept != null) {
								double weightInKg;
								double heightInM;
								
								if (weightConcept.getUnits().equalsIgnoreCase("kg")) {
									weightInKg = latestWeight.getValueNumeric();
								} else if (weightConcept.getUnits().equalsIgnoreCase("lb")) {
									weightInKg = latestWeight.getValueNumeric() * 0.45359237;
								} else {
									throw new IllegalArgumentException("Can't handle units of weight concept: "
									        + weightConcept.getUnits());
								}
								
								if (heightConcept.getUnits().equalsIgnoreCase("cm")) {
									heightInM = latestHeight.getValueNumeric() / 100;
								} else if (heightConcept.getUnits().equalsIgnoreCase("m")) {
									heightInM = latestHeight.getValueNumeric();
								} else if (heightConcept.getUnits().equalsIgnoreCase("in")) {
									heightInM = latestHeight.getValueNumeric() * 0.0254;
								} else {
									throw new IllegalArgumentException("Can't handle units of height concept: "
									        + heightConcept.getUnits());
								}
								
								double bmi = weightInKg / (heightInM * heightInM);
								model.put("patientBmi", bmi);
								String temp = "" + bmi;
								bmiAsString = temp.substring(0, temp.indexOf('.') + 2);
							}
						}
						catch (Exception ex) {
							log.error("Failed to calculate BMI", ex);
						}
						
						model.put("patientBmiAsString", bmiAsString);
					} else {
						model.put("patientObs", new HashSet<Obs>());
					}
					/**
					 * Copied from OpenMRS core 1.9.13 See
					 * https://github.com/openmrs/openmrs-core/blob
					 * /1.9.13/web/src/main/java/org/openmrs
					 * /web/controller/PortletController.java#L267
					 */
					// information about whether or not the patient has exited care
					Obs reasonForExitObs = null;
					String reasonForExitConceptString = as.getGlobalProperty("concept.reasonExitedCare");
					if (StringUtils.hasLength(reasonForExitConceptString)) {
						Concept reasonForExitConcept = cs.getConcept(reasonForExitConceptString);
						if (reasonForExitConcept != null) {
							List<Obs> patientExitObs = Context.getObsService().getObservationsByPersonAndConcept(p,
							    reasonForExitConcept);
							if (patientExitObs != null) {
								log.debug("Exit obs is size " + patientExitObs.size());
								if (patientExitObs.size() == 1) {
									reasonForExitObs = patientExitObs.iterator().next();
									Concept exitReason = reasonForExitObs.getValueCoded();
									Date exitDate = reasonForExitObs.getObsDatetime();
									if (exitReason != null && exitDate != null) {
										patientVariation = "Exited";
									}
								} else {
									if (patientExitObs.size() == 0) {
										log.debug("Patient has no reason for exit");
									} else {
										log.error("Too many reasons for exit - not putting data into model");
									}
								}
							}
						}
					}
					model.put("patientReasonForExit", reasonForExitObs);
					
					if (Context.hasPrivilege(PrivilegeConstants.GET_PROGRAMS)
					        && Context.hasPrivilege(PrivilegeConstants.GET_PATIENT_PROGRAMS)) {
						model.put("patientPrograms",
						    Context.getProgramWorkflowService().getPatientPrograms(p, null, null, null, null, null, false));
						model.put(
						    "patientCurrentPrograms",
						    Context.getProgramWorkflowService().getPatientPrograms(p, null, null, new Date(), new Date(),
						        null, false));
					}
					
					model.put("patientId", patientId);
					personId = p.getPatientId();
					model.put("personId", personId);
					
					model.put("patientVariation", patientVariation);
				}
			}
			
			// if a person id is available, put person and relationships in the model
			if (personId == null) {
				o = request.getAttribute("org.openmrs.portlet.personId");
				if (o != null) {
					personId = (Integer) o;
					model.put("personId", personId);
				}
			}
			if (personId != null) {
				Person p = (Person) model.get("person");
				if (p == null) {
					p = (Person) model.get("patient");
					if (p == null) {
						p = Context.getPersonService().getPerson(personId);
					}
					model.put("person", p);
				}
				
				if (!model.containsKey("personRelationships") && Context.hasPrivilege(PrivilegeConstants.GET_RELATIONSHIPS)) {
					List<Relationship> relationships = new ArrayList<Relationship>();
					relationships.addAll(Context.getPersonService().getRelationshipsByPerson(p));
					Map<RelationshipType, List<Relationship>> relationshipsByType = new HashMap<RelationshipType, List<Relationship>>();
					for (Relationship rel : relationships) {
						List<Relationship> list = relationshipsByType.get(rel.getRelationshipType());
						if (list == null) {
							list = new ArrayList<Relationship>();
							relationshipsByType.put(rel.getRelationshipType(), list);
						}
						list.add(rel);
					}
					
					model.put("personRelationships", relationships);
					model.put("personRelationshipsByType", relationshipsByType);
				}
			}
			
			// if an encounter id is available, put "encounter" and "encounterObs" in the
			// model
			o = request.getAttribute("org.openmrs.portlet.encounterId");
			if (o != null && !model.containsKey("encounterId")) {
				if (!model.containsKey("encounter") && Context.hasPrivilege(PrivilegeConstants.GET_ENCOUNTERS)) {
					Encounter e = Context.getEncounterService().getEncounter((Integer) o);
					model.put("encounter", e);
					if (Context.hasPrivilege(PrivilegeConstants.GET_OBS)) {
						model.put("encounterObs", e.getObs());
					}
					model.put("encounterId", (Integer) o);
				}
			}
			
			// if a user id is available, put "user" in the model
			o = request.getAttribute("org.openmrs.portlet.userId");
			if (o != null && !model.containsKey("user")) {
				if (Context.hasPrivilege(PrivilegeConstants.GET_USERS)) {
					User u = Context.getUserService().getUser((Integer) o);
					model.put("user", u);
				}
				model.put("userId", (Integer) o);
			}
			
			// if a list of patient ids is available, make a patientset out of it
			o = request.getAttribute("org.openmrs.portlet.patientIds");
			if (!StringUtils.isEmpty(o) && !model.containsKey("patientIds") && !model.containsKey("patientSet")) {
				Cohort ps = new Cohort((String) o);
				model.put("patientSet", ps);
				model.put("patientIds", (String) o);
			}
			
			o = model.get("conceptIds");
			
			if (!StringUtils.isEmpty(o) && !model.containsKey("conceptMap")) {
				log.debug("Found conceptIds parameter: " + o);
				Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
				Map<String, Concept> conceptsByStringIds = new HashMap<String, Concept>();
				String conceptIds = (String) o;
				String[] ids = conceptIds.split(",");
				for (String cId : ids) {
					try {
						Integer i = Integer.valueOf(cId);
						Concept c = cs.getConcept(i);
						concepts.put(i, c);
						conceptsByStringIds.put(i.toString(), c);
					}
					catch (Exception ex) {
						log.error("Error during putting int i into concept c", ex);
					}
				}
				
				model.put("conceptMap", concepts);
				model.put("conceptMapByStringIds", conceptsByStringIds);
			}
			
			populateModel(request, model);
			log.debug(portletPath + " took " + (System.currentTimeMillis() - timeAtStart) + " ms");
		}
		
		return new ModelAndView(portletPath, "model", model);
		
	}
	
	/**
	 * Subclasses should override this to put more data into the model. This will be called AFTER
	 * handleRequest has put mappings in the model as described in its javadoc. Note that context
	 * could be null when this method is called.
	 */
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
	}
	
	private Integer getStartIndexParameter(HttpServletRequest request) {
		try {
			String startIndexParam = request.getParameter("startIndex");
			if (StringUtils.hasText(startIndexParam)) {
				return Integer.parseInt(startIndexParam);
			}
		}
		catch (NumberFormatException e) {
			log.debug("Unable to parse startIndex parameter, using default", e);
		}
		return 0; // Return first page if not specified or invalid
	}
	
	private Integer getLimitParameter(HttpServletRequest request, AdministrationService as, String globalPropertyKey) {
		try {
			String limitParam = request.getParameter("limit");
			if (StringUtils.hasText(limitParam)) {
				return Integer.parseInt(limitParam);
			}
			
			String globalLimit = as.getGlobalProperty(globalPropertyKey);
			if (StringUtils.hasText(globalLimit)) {
				return Integer.parseInt(globalLimit);
			}
		}
		catch (NumberFormatException e) {
			log.debug("Unable to parse limit parameter, using default", e);
		}
		return DEFAULT_PAGE_SIZE;
	}
	
}
