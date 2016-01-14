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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test the different aspects of
 * {@link org.openmrs.web.controller.ForgotPasswordFormController}
 */
public class ForgotPasswordFormControllerTest extends BaseModuleWebContextSensitiveTest {

	protected static final String TEST_DATA= "org/openmrs/web/controller/include/ForgotPasswordFormControllerTest.xml";

	@Before
	public void updateSearchIndex() {
		super.updateSearchIndex();
	}

	/**
	 * Check to see if the admin's secret question comes back
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldNotNotFailOnNotFoundUsernameTEST() throws Exception {
		executeDataSet(TEST_DATA);

		ForgotPasswordFormController controller = (ForgotPasswordFormController) applicationContext.getBean("forgotPasswordForm");

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		request.setParameter("uname", "bruno");
		request.setParameter("secretAnswer", "valid secret answer");
		request.setMethod("POST");

		controller.handleRequest(request, response);

		Assert.assertEquals("what is your name bruno?", request.getParameter("secretQuestion"));
	}

}
