/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.PatientProgram;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

public class DWRProgramWorkflowServiceTest extends BaseModuleWebContextSensitiveTest {
	
	private DWRProgramWorkflowService dwrProgramWorkflowService;
	
	protected static final String PROGRAM_WITH_OUTCOMES_XML = "org/openmrs/web/dwr/include/DWRProgramWorkflowServiceTest-initialData.xml";
	
	protected static final String PROGRAM_NEXT_STATES_XML = "org/openmrs/web/dwr/include/DWRProgramWorkflowServiceTest-initialStates.xml";
	
	@Before
	public void setUp() throws Exception {
		dwrProgramWorkflowService = new DWRProgramWorkflowService();
		executeDataSet(PROGRAM_WITH_OUTCOMES_XML);
	}
	
	@Test
	@Verifies(value = "should get possible outcomes for a program", method = "getPossibleOutcomes()")
	public void getPossibleOutcomes_shouldReturnOutcomeConceptsFromProgram() throws Exception {
		Vector<ListItem> possibleOutcomes = dwrProgramWorkflowService.getPossibleOutcomes(4);
		assertFalse(possibleOutcomes.isEmpty());
		assertEquals(4, possibleOutcomes.size());
	}
	
	@Test
	@Verifies(value = "should return a list consisting of active, not retired, states", method = "getPossibleNextStates")
	public void getPossibleNextStates_shouldReturnAllNext() throws Exception {
		executeDataSet(PROGRAM_NEXT_STATES_XML);
		
		Integer patient = 11;
		Integer workflow = 501;
		Vector<ListItem> possibleNextStates;
		
		possibleNextStates = dwrProgramWorkflowService.getPossibleNextStates(patient, workflow);
		assertFalse(possibleNextStates.isEmpty());
		assertEquals(3, possibleNextStates.size());
		
	}
	
	@Test
	@Verifies(value = "should return a list consisting of active, not retired, states", method = "getPossibleNextStates")
	public void getPossibleNextStates_shouldReturnNonRetiredConcepts() throws Exception {
		executeDataSet(PROGRAM_NEXT_STATES_XML);
		
		Integer patient = 11;
		Integer workflow = 501;
		Vector<ListItem> possibleNextStates;
		
		/* retire a concept */
		PatientProgram pp = Context.getProgramWorkflowService().getPatientProgram(patient);
		ProgramWorkflow pw = pp.getProgram().getWorkflow(workflow);
		Set<ProgramWorkflowState> pwss = pw.getStates();
		
		for (ProgramWorkflowState pws : pwss) {
			Concept cp = pws.getConcept();
			ConceptName cn = cp.getName();
			if (cn != null) {
				String cnn = cn.getName();
				if (cnn.equalsIgnoreCase("Test State 3")) {
					cp.setRetired(true);
				}
			}
		}
		
		possibleNextStates = dwrProgramWorkflowService.getPossibleNextStates(patient, workflow);
		assertFalse(possibleNextStates.isEmpty());
		assertEquals(2, possibleNextStates.size());
		
	}
	
	@Test
	@Verifies(value = "should return a list consisting of active, not retired, states", method = "getPossibleNextStates")
	public void getPossibleNextStates_shouldReturnNonRetiredStates() throws Exception {
		executeDataSet(PROGRAM_NEXT_STATES_XML);
		
		Integer patient = 11;
		Integer workflow = 501;
		Vector<ListItem> possibleNextStates;
		
		/* retire a workflow state  */
		PatientProgram pp = Context.getProgramWorkflowService().getPatientProgram(patient);
		ProgramWorkflow pw = pp.getProgram().getWorkflow(workflow);
		Set<ProgramWorkflowState> pwss = pw.getStates();
		
		for (ProgramWorkflowState pws : pwss) {
			Concept cp = pws.getConcept();
			ConceptName cn = cp.getName();
			if (cn != null) {
				String cnn = cn.getName();
				if (cnn.equalsIgnoreCase("Test State 3")) {
					pws.setRetired(true);
				}
			}
		}
		
		possibleNextStates = dwrProgramWorkflowService.getPossibleNextStates(patient, workflow);
		assertFalse(possibleNextStates.isEmpty());
		assertEquals(2, possibleNextStates.size());
		
	}
	
	@Test
	@Verifies(value = "should return a ProgramWorkflow for the given ID", method = "getProgramWorkflow")
	public void getWorkflowById_shouldFindObjectGivenValidId() throws Exception {
		executeDataSet(PROGRAM_NEXT_STATES_XML);
		int programWorkflowId= 501;
		ProgramWorkflow programWorkflow = dwrProgramWorkflowService.getProgramWorkflow(String.valueOf(programWorkflowId));
		assertNotNull(programWorkflow);
		assertEquals("79fbc48b-215f-41af-982c-5071978be018", programWorkflow.getUuid());
	}
	
	@Test
	@Verifies(value = "should return a ProgramWorkflow for the given Name", method = "getProgramWorkflow")
	public void getWorkflowByName_shouldFindObjectGivenValidName() {
		String programWorkflowName = "TREATMENT STATUS";
		ProgramWorkflow programWorkflow = dwrProgramWorkflowService.getProgramWorkflow(programWorkflowName);
		assertEquals(2, (int) programWorkflow.getProgramWorkflowId());
	}
	
	@Test
	@Verifies(value = "should return a ProgramWorkflow for the given UUID", method = "getProgramWorkflow")
	public void getWorkflowByUuid_shouldFindObjectGivenValidUuid() {
		String programWorkflowUuid = "84f0effa-dd73-46cb-b931-7cd6be6c5f81";
		ProgramWorkflow programWorkflow = dwrProgramWorkflowService.getProgramWorkflow(programWorkflowUuid);
		assertEquals(1, (int) programWorkflow.getProgramWorkflowId());
	}
	
	@Test
	@Verifies(value = "should return null when there is no ProgramWorkflow for the given lookup", method = "getProgramWorkflow")
	public void getProgramByName_shouldReturnNullWhenThereIsNoProgramWorkflowForGivenLookup() {
		ProgramWorkflow workflow = dwrProgramWorkflowService.getProgramWorkflow("A name");
		assertNull(workflow);
	}
	
}
