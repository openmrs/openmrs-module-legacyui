/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.user;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tests the {@link oldUserFormController} class.
 */
public class UserFormControllerTest extends BaseModuleWebContextSensitiveTest {
	
	protected static final String TEST_DATA = "org/openmrs/web/controller/include/UserFormControllerTest.xml";
	
	@Autowired
	private UserFormController controller;
	
	/**
	 * @see UserFormController#handleSubmission(WebRequest,HttpSession,String,String,String,null,
	 *      String, User,BindingResult, HttpServletResponse)
	 */
	@Test
	@Verifies(value = "should work for an example", method = "handleSubmission(WebRequest,HttpSession,String,String,String,null, String, User,BindingResult, HttpServletResponse)")
	public void handleSubmission_shouldWorkForAnExample() throws Exception {
		WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
		User user = controller.formBackingObject(request, null);
		user.addName(new PersonName("This", "is", "Test"));
		user.getPerson().setGender("F");
		controller.handleSubmission(request, new MockHttpSession(), new ModelMap(), "", "Save User", "pass123", "pass123",
		    null, null, null, new String[0], "true", null, "sample@email.com", user, new BindException(user, "user"),
		    new MockHttpServletResponse());
	}
	
	/**
	 * @see UserFormController#handleSubmission(WebRequest,HttpSession,String,String,String,null,
	 *      String, User,BindingResult,HttpServletResponse)
	 */
	@Test
	@Verifies(value = "Creates Provider Account when Provider Account Checkbox is selected", method = "handleSubmission(WebRequest,HttpSession,String,String,String,null, String, User, BindingResult, HttpServletResponse)")
	public void handleSubmission_createUserProviderAccountWhenProviderAccountCheckboxIsSelected() throws Exception {
		executeDataSet(TEST_DATA);
		WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
		//A user with userId=2 is preset in the Test DataSet and the relevant details are passed
		User user = Context.getUserService().getUser(2);
		Assert.assertTrue(Context.getProviderService().getProvidersByPerson(user.getPerson()).isEmpty());
		ModelMap model = new ModelMap();
		model.addAttribute("isProvider", false);
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.showForm(2, "true", user, model);
		controller.handleSubmission(request, new MockHttpSession(), new ModelMap(), "", null, "Test1234",
		    "valid secret question", "valid secret answer", "Test1234", false, new String[] { "Provider" }, "true",
		    "addToProviderTable", "sample@email.com", user, new BindException(user, "user"), response);
		Assert.assertFalse(Context.getProviderService().getProvidersByPerson(user.getPerson()).isEmpty());
		Assert.assertEquals(200, response.getStatus());
	}
	
	@Test
	public void shouldSetResponseStatusToBadRequestOnError() throws Exception {
		executeDataSet(TEST_DATA);
		WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
		//A user with userId=2 is preset in the Test DataSet and the relevant details are passed
		User user = Context.getUserService().getUser(2);
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.handleSubmission(request, new MockHttpSession(), new ModelMap(), "", null, "Test123",
		    "valid secret question", "valid secret answer", "Test1234", false, new String[] { "Provider" }, "true",
		    "addToProviderTable", "sample@email.com", user, new BindException(user, "user"), response);
		Assert.assertEquals(400, response.getStatus());
	}
	
}
