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

import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		Assert.assertNotEquals(oldPatient, newPatient);
		
		List<Encounter> newEncounter = Context.getEncounterService().getEncountersByPatientId(newPatient.getPatientId());
		Assert.assertEquals(0, newEncounter.size());
		
		BindException errors = new BindException(encounter, "encounterRole");
		
		controller.onSubmit(request, response, encounter, errors);
		
		Assert.assertEquals(true, encounter.isVoided());
		newEncounter = Context.getEncounterService().getEncountersByPatientId(newPatient.getPatientId());
		Assert.assertEquals(1, newEncounter.size());
		Assert.assertEquals(false, newEncounter.get(0).isVoided());
	}
	
	/**
	 * @see EncounterFormController#referenceData(javax.servlet.http.HttpServletRequest, Object,
	 *      org.springframework.validation.Errors)
	 */
	@Test
	public void referenceData_shouldIncludeProvidersVoidedWithTheEncounter() throws Exception {
		Encounter encounter = voidEncounterAndProviders(Context.getEncounterService().getEncounter(3));
		Assert.assertTrue(encounter.getProvidersByRoles().isEmpty());
		
		EncounterFormController controller = new EncounterFormController();
		Map<String, Object> map = controller.referenceData(new MockHttpServletRequest(), encounter, new BindException(
		        encounter, "encounter"));
		
		Map<EncounterRole, Set<Provider>> providersByRoles = (Map<EncounterRole, Set<Provider>>) map
		        .get("providersByRoles");
		EncounterRole role = Context.getEncounterService().getEncounterRole(1);
		Assert.assertEquals(1, providersByRoles.size());
		Assert.assertTrue(providersByRoles.get(role).contains(Context.getProviderService().getProvider(1)));
	}
	
	/**
	 * @see EncounterFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void processFormSubmission_shouldRestoreProvidersVoidedWithTheEncounterWithoutDuplicatingThem()
	        throws Exception {
		Encounter encounter = voidEncounterAndProviders(Context.getEncounterService().getEncounter(3));
		encounter.setVoided(false);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", encounter.getPatient().getPatientId().toString());
		request.setParameter("providerIds", "1");
		request.setParameter("encounterRoleIds", "1");
		BindException errors = new BindException(encounter, "encounter");
		
		EncounterFormController controller = new EncounterFormController();
		controller.processFormSubmission(request, new MockHttpServletResponse(), encounter, errors);
		Assert.assertFalse(errors.hasErrors());
		Assert.assertEquals(1, encounter.getEncounterProviders().size());
		
		controller.onSubmit(request, new MockHttpServletResponse(), encounter, errors);
		
		Assert.assertFalse(encounter.getVoided());
		Assert.assertEquals(1, encounter.getEncounterProviders().size());
		Assert.assertEquals(1, encounter.getActiveEncounterProviders().size());
	}
	
	/**
	 * @see EncounterFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void processFormSubmission_shouldNotRestoreARemovedProviderThatWasVoidedWithTheEncounter() throws Exception {
		Provider otherProvider = new Provider();
		otherProvider.setPerson(Context.getPersonService().getPerson(2));
		otherProvider.setIdentifier("other");
		Context.getProviderService().saveProvider(otherProvider);
		Encounter encounter = voidEncounterAndProviders(Context.getEncounterService().getEncounter(3));
		encounter.setVoided(false);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", encounter.getPatient().getPatientId().toString());
		request.setParameter("providerIds", otherProvider.getProviderId().toString());
		request.setParameter("encounterRoleIds", "1");
		BindException errors = new BindException(encounter, "encounter");
		
		EncounterFormController controller = new EncounterFormController();
		controller.processFormSubmission(request, new MockHttpServletResponse(), encounter, errors);
		Assert.assertFalse(errors.hasErrors());
		controller.onSubmit(request, new MockHttpServletResponse(), encounter, errors);
		
		Assert.assertFalse(encounter.getVoided());
		Set<Provider> providers = encounter.getProvidersByRole(Context.getEncounterService().getEncounterRole(1));
		Assert.assertEquals(1, providers.size());
		Assert.assertTrue(providers.contains(otherProvider));
	}
	
	/**
	 * @see EncounterFormController#referenceData(javax.servlet.http.HttpServletRequest, Object,
	 *      org.springframework.validation.Errors)
	 */
	@Test
	public void referenceData_shouldOnlyIncludeActiveProvidersIfTheyWereNotVoidedWithTheEncounter() throws Exception {
		Encounter encounter = voidEncounterWithoutProviders(Context.getEncounterService().getEncounter(3));
		Assert.assertEquals(1, encounter.getActiveEncounterProviders().size());

		EncounterFormController controller = new EncounterFormController();
		Map<String, Object> map = controller.referenceData(new MockHttpServletRequest(), encounter, new BindException(
		        encounter, "encounter"));

		Map<EncounterRole, Set<Provider>> providersByRoles = (Map<EncounterRole, Set<Provider>>) map
		        .get("providersByRoles");
		EncounterRole role = Context.getEncounterService().getEncounterRole(1);
		Assert.assertEquals(1, providersByRoles.size());
		Assert.assertEquals(1, providersByRoles.get(role).size());
		Assert.assertTrue(providersByRoles.get(role).contains(Context.getProviderService().getProvider(1)));
	}

	/**
	 * @see EncounterFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void processFormSubmission_shouldKeepProvidersNotVoidedWithTheEncounterWithoutDuplicatingThem()
	        throws Exception {
		Encounter encounter = voidEncounterWithoutProviders(Context.getEncounterService().getEncounter(3));
		encounter.setVoided(false);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", encounter.getPatient().getPatientId().toString());
		request.setParameter("providerIds", "1");
		request.setParameter("encounterRoleIds", "1");
		BindException errors = new BindException(encounter, "encounter");

		EncounterFormController controller = new EncounterFormController();
		controller.processFormSubmission(request, new MockHttpServletResponse(), encounter, errors);
		Assert.assertFalse(errors.hasErrors());
		controller.onSubmit(request, new MockHttpServletResponse(), encounter, errors);

		Assert.assertFalse(encounter.getVoided());
		Assert.assertEquals(1, encounter.getEncounterProviders().size());
		Assert.assertEquals(1, encounter.getActiveEncounterProviders().size());
	}

	/**
	 * @see EncounterFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void processFormSubmission_shouldVoidARemovedProviderThatWasNotVoidedWithTheEncounter() throws Exception {
		Provider otherProvider = new Provider();
		otherProvider.setPerson(Context.getPersonService().getPerson(2));
		otherProvider.setIdentifier("other");
		Context.getProviderService().saveProvider(otherProvider);
		Encounter encounter = voidEncounterWithoutProviders(Context.getEncounterService().getEncounter(3));
		encounter.setVoided(false);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", encounter.getPatient().getPatientId().toString());
		request.setParameter("providerIds", otherProvider.getProviderId().toString());
		request.setParameter("encounterRoleIds", "1");
		BindException errors = new BindException(encounter, "encounter");

		EncounterFormController controller = new EncounterFormController();
		controller.processFormSubmission(request, new MockHttpServletResponse(), encounter, errors);
		Assert.assertFalse(errors.hasErrors());
		controller.onSubmit(request, new MockHttpServletResponse(), encounter, errors);

		Assert.assertFalse(encounter.getVoided());
		Set<Provider> providers = encounter.getProvidersByRole(Context.getEncounterService().getEncounterRole(1));
		Assert.assertEquals(1, providers.size());
		Assert.assertTrue(providers.contains(otherProvider));
	}

	/**
	 * @see EncounterFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void onSubmit_shouldVoidTheObsOfAVoidedEncounterWithoutDuplicatingThem() throws Exception {
		Set<Integer> obsIds = getObsIds(Context.getEncounterService().getEncounter(3));
		Assert.assertEquals(2, obsIds.size());

		Encounter encounter = Context.getEncounterService().getEncounter(3);
		encounter.setVoided(true);
		encounter.setVoidReason("testing");
		submitEncounterForm(encounter);

		encounter = Context.getEncounterService().getEncounter(3);
		Assert.assertTrue(encounter.getVoided());
		Assert.assertEquals(obsIds, getObsIds(encounter));
		for (Obs obs : encounter.getAllObs(true)) {
			Assert.assertTrue(obs.getVoided());
		}
	}

	/**
	 * @see EncounterFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void onSubmit_shouldRestoreTheObsOfAnUnvoidedEncounterWithoutDuplicatingThem() throws Exception {
		Set<Integer> obsIds = getObsIds(Context.getEncounterService().getEncounter(3));
		Assert.assertEquals(2, obsIds.size());

		Encounter encounter = Context.getEncounterService().getEncounter(3);
		encounter.setVoided(true);
		encounter.setVoidReason("testing");
		submitEncounterForm(encounter);

		encounter = Context.getEncounterService().getEncounter(3);
		encounter.setVoided(false);
		submitEncounterForm(encounter);

		encounter = Context.getEncounterService().getEncounter(3);
		Assert.assertFalse(encounter.getVoided());
		Assert.assertEquals(obsIds, getObsIds(encounter));
		for (Obs obs : encounter.getAllObs(true)) {
			Assert.assertFalse(obs.getVoided());
		}
		Assert.assertEquals(1, encounter.getEncounterProviders().size());
		Assert.assertEquals(1, encounter.getActiveEncounterProviders().size());
	}

	/**
	 * @see EncounterFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	public void onSubmit_shouldRestoreTheObsOfAnEncounterVoidedWithItsProvidersWithoutDuplicatingThem()
	        throws Exception {
		Set<Integer> obsIds = getObsIds(Context.getEncounterService().getEncounter(3));
		Assert.assertEquals(2, obsIds.size());

		voidEncounterAndProviders(Context.getEncounterService().getEncounter(3));
		Context.clearSession();

		Encounter encounter = Context.getEncounterService().getEncounter(3);
		encounter.setVoided(false);
		submitEncounterForm(encounter);

		encounter = Context.getEncounterService().getEncounter(3);
		Assert.assertFalse(encounter.getVoided());
		Assert.assertEquals(obsIds, getObsIds(encounter));
		for (Obs obs : encounter.getAllObs(true)) {
			Assert.assertFalse(obs.getVoided());
		}
	}

	/**
	 * Submits the encounter form for the given encounter, posting its patient and the provider with role 1
	 * as the form does, then flushes and clears the session so that the encounter is reloaded from the
	 * database afterwards
	 */
	private void submitEncounterForm(Encounter encounter) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", encounter.getPatient().getPatientId().toString());
		request.setParameter("providerIds", "1");
		request.setParameter("encounterRoleIds", "1");
		BindException errors = new BindException(encounter, "encounter");

		EncounterFormController controller = new EncounterFormController();
		controller.processFormSubmission(request, new MockHttpServletResponse(), encounter, errors);
		Assert.assertFalse(errors.hasErrors());
		controller.onSubmit(request, new MockHttpServletResponse(), encounter, errors);
		Assert.assertFalse(errors.hasErrors());
		Context.flushSession();
		Context.clearSession();
	}

	private Set<Integer> getObsIds(Encounter encounter) {
		Set<Integer> obsIds = new HashSet<Integer>();
		for (Obs obs : encounter.getAllObs(true)) {
			obsIds.add(obs.getObsId());
		}
		return obsIds;
	}

	/**
	 * Voids the encounter but not its encounter providers, as core does before 2.8.x/3.x. The void fields are
	 * set directly so that the providers stay active whatever core version the tests run against.
	 */
	private Encounter voidEncounterWithoutProviders(Encounter encounter) {
		encounter.setVoided(true);
		encounter.setVoidedBy(Context.getAuthenticatedUser());
		encounter.setDateVoided(new Date());
		encounter.setVoidReason("testing");
		Context.getEncounterService().saveEncounter(encounter);
		Context.flushSession();
		return encounter;
	}

	/**
	 * Voids the encounter and, as core does from 2.8.x/3.x, its encounter providers along with it
	 */
	private Encounter voidEncounterAndProviders(Encounter encounter) {
		Context.getEncounterService().voidEncounter(encounter, "testing");
		for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
			if (!encounterProvider.getVoided()) {
				encounterProvider.setVoided(true);
				encounterProvider.setVoidedBy(encounter.getVoidedBy());
				encounterProvider.setDateVoided(encounter.getDateVoided());
				encounterProvider.setVoidReason(encounter.getVoidReason());
			}
		}
		Context.getEncounterService().saveEncounter(encounter);
		Context.flushSession();
		return encounter;
	}
}
