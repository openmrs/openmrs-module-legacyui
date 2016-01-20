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


import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;



/**
 * Test the different aspects of
 * {@link org.openmrs.web.controller.ForgotPasswordFormController}
 */

public class ForgotPasswordFormControllerTest extends BaseModuleWebContextSensitiveTest {
	
	protected static final String TEST_DATA = "org/openmrs/web/controller/include/ForgotPasswordFormControllerTest.xml";
	
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(TEST_DATA);
		Context.logout();
	}
	
	/**
	 * Check to see if the admin's secret question comes back
	 *
	 * @throws Exception
	 */
	
	@Test
	public void shouldNotNotFailOnNotFoundUsername() throws Exception {
		
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("uname", "validuser");
		request.setMethod("POST");
		
		controller.handleRequest(request, response);
		
		Assert.assertEquals("validuser", request.getAttribute("uname"));
		Assert.assertEquals("valid secret question", request.getAttribute("secretQuestion"));
	}
	
	
	@Test
	public void shouldAuthenticateAsUserWithValidSecretQuestion() throws Exception {
		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "valid secret answer");
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		Assert.assertEquals(2, Context.getAuthenticatedUser().getId().intValue());
	}
	
	/**
	 * If a user enters the wrong secret answer, they should be kicked back to the form and not be
	 * authenticated
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldNotAuthenticateAsUserWithInvalidSecretQuestion() throws Exception {
		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "invaliduser");
		request.addParameter("secretAnswer", "invalid secret answer");
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
	/**
	 * If a user enters 5 requests, the 6th should fail even if that one has a valid username in it
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldLockOutAfterFiveFailedInvalidUsernames() throws Exception {
		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");
		
		for (int x = 1; x <= 5; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setMethod("POST");
			
			request.addParameter("uname", "invaliduser");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first five, now the sixth request (with a valid user) should fail
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		
		controller.handleRequest(request, new MockHttpServletResponse());
		Assert.assertNull(request.getAttribute("secretQuestion"));
	}
	
	/**
	 * If a user enters 5 requests, the 6th should fail even if that one has a valid username and a
	 * secret answer associated with it
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldNotAuthenticateAfterFiveFailedInvalidUsernames() throws Exception {
		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");
		
		for (int x = 1; x <= 5; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setMethod("POST");
			
			request.addParameter("uname", "invaliduser");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first five, now the sixth request (with a valid user) should fail
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "valid secret answer");
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
	/**
	 * If a user enters 5 requests with username+secret answer, the 6th should fail even if that one
	 * has a valid answer in it
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldLockOutAfterFiveFailedInvalidSecretAnswers() throws Exception {
		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");
		
		for (int x = 1; x <= 5; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setMethod("POST");
			
			request.addParameter("uname", "validuser");
			request.addParameter("secretAnswer", "invalid secret answer");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first five, now the sixth request (with a valid user) should fail
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "valid secret answer");
		
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
	/**
	 * If a user enters 4 username requests, the 5th one should reset the lockout and they should be
	 * allowed 5 attempts at the secret answer
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldGiveUserFiveSecretAnswerAttemptsAfterLessThanFiveFailedUsernameAttempts() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		for (int x = 1; x <= 4; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setMethod("POST");
			
			request.addParameter("uname", "invaliduser");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first four, now the fifth is a valid username
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertNotNull(request.getAttribute("secretQuestion"));
		
		// now the user has 5 chances at the secret answer
		
		// fifth request
		MockHttpServletRequest request5 = new MockHttpServletRequest();
		request5.setMethod("POST");
		
		request5.addParameter("uname", "validuser");
		request5.addParameter("secretAnswer", "invalid answer");
		controller.handleRequest(request5, new MockHttpServletResponse());
		Assert.assertNotNull(request5.getAttribute("secretQuestion"));
		
		// sixth request (should not lock out because is after valid username)
		MockHttpServletRequest request6 = new MockHttpServletRequest();
		request6.setMethod("POST");
		
		request6.addParameter("uname", "validuser");
		request6.addParameter("secretAnswer", "invalid answer");
		request.setMethod("POST");
		controller.handleRequest(request6, new MockHttpServletResponse());
		Assert.assertNotNull(request6.getAttribute("secretQuestion"));
		
		// seventh request (should authenticate with valid answer)
		MockHttpServletRequest request7 = new MockHttpServletRequest();
		request7.setMethod("POST");
		
		request7.addParameter("uname", "validuser");
		request7.addParameter("secretAnswer", "valid secret answer");
		controller.handleRequest(request7, new MockHttpServletResponse());
		
		Assert.assertTrue(Context.isAuthenticated());
	}
	
	@Test
	public void shouldNotAuthenticateWithInvalidSecretQuestionIfUserIsNull() throws Exception {
		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");
		MockHttpServletRequest request = new MockHttpServletRequest();
		
		request.setMethod("POST");
		request.addParameter("uname", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		controller.handleRequest(request, response);
		Assert.assertFalse(Context.isAuthenticated());
	}
}
