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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Tests methods on the {@link FieldGenController} class
 */
public class FieldGenControllerTest {

	private MockHttpServletRequest createFieldGenRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setAttribute("javax.servlet.include.servlet_path.fieldGen", "/fieldGen/java.util.Date.field");
		request.setAttribute("org.openmrs.fieldGen.type", "java.util.Date");
		request.setAttribute("org.openmrs.fieldGen.formFieldName", "startDate");
		request.setAttribute("org.openmrs.fieldGen.parameters", new HashMap<String, Object>());
		return request;
	}

	/**
	 * @see FieldGenController#handleRequest(HttpServletRequest,HttpServletResponse)
	 */
	@Test
	public void handleRequest_shouldResolveTheFieldGenJspRegardlessOfTheAcceptHeader() throws Exception {
		InternalResourceViewResolver jspViewResolver = new InternalResourceViewResolver();
		jspViewResolver.setViewClass(JstlView.class);
		jspViewResolver.setPrefix("/WEB-INF/view/");
		jspViewResolver.setSuffix(".jsp");
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.getBeanFactory().registerSingleton("jspViewResolver", jspViewResolver);
		wac.refresh();
		jspViewResolver.setApplicationContext(wac);

		MockHttpServletRequest request = createFieldGenRequest();
		request.addHeader("Accept", "*/*");
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

		ModelAndView modelAndView = new FieldGenController().handleRequest(request, new MockHttpServletResponse());

		Assert.assertTrue(modelAndView.getView() instanceof JstlView);
		// the double slash is what core's jspViewResolver has always built from a fieldGen path
		Assert.assertEquals("/WEB-INF/view//fieldGen/java.util.Date.jsp", ((JstlView) modelAndView.getView()).getUrl());
	}

	/**
	 * @see FieldGenController#handleRequest(HttpServletRequest,HttpServletResponse)
	 */
	@Test
	public void handleRequest_shouldFallBackToTheViewNameWithoutAJspViewResolver() throws Exception {
		ModelAndView modelAndView = new FieldGenController().handleRequest(createFieldGenRequest(),
		    new MockHttpServletResponse());

		Assert.assertEquals("/fieldGen/java.util.Date", modelAndView.getViewName());
	}
}
