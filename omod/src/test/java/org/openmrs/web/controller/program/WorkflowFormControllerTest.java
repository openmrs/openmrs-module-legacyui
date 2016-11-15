/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.program;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ProgramWorkflow;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tests the {@link WorkflowFormController} class.
 */
public class WorkflowFormControllerTest extends BaseModuleWebContextSensitiveTest {

	/**
	 * @see WorkflowFormController#formBackingObject(HttpServletRequest)
	 */
	@Test
	@Verifies(value = "should return valid programWorkflow given valid programId and workflowId", method = "formBackingObject(HttpServletRequest)")
	public void formBackingObject_shouldReturnValidProgramWorkflowGivenValidProgramIdAndWorkflowId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("programId", "1");
		request.setParameter("programWorkflowId", "1");

		HttpServletResponse response = new MockHttpServletResponse();

		WorkflowFormController controller = (WorkflowFormController) applicationContext.getBean("workflowFormController");

		ModelAndView modelAndView = controller.handleRequest(request, response);

		ProgramWorkflow command = (ProgramWorkflow) modelAndView.getModel().get("workflow");
		Assert.assertNotNull(command.getProgramWorkflowId());
	}

}
