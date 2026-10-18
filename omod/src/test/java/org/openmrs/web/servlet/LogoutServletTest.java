/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the {@link LogoutServlet}
 */
public class LogoutServletTest extends BaseModuleWebContextSensitiveTest {
	
	private String logout() throws Exception {
		return logout("/somecontextpath");
	}
	
	private String logout(String contextPath) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ms/logout");
		request.setContextPath(contextPath);
		MockHttpServletResponse response = new MockHttpServletResponse();
		new LogoutServlet().service(request, response);
		return response.getRedirectedUrl();
	}
	
	@Test
	public void shouldRedirectToIndexWhenNoLoginUrlIsSet() throws Exception {
		Assertions.assertEquals("/somecontextpath/index.htm?noredirect=true", logout());
		Assertions.assertFalse(Context.isAuthenticated());
	}
	
	@Test
	public void shouldRedirectToIndexWhenTheLoginUrlIsCoresDefault() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GP_LOGIN_URL, OpenmrsConstants.LOGIN_URL));
		
		Assertions.assertEquals("/somecontextpath/index.htm?noredirect=true", logout());
		Assertions.assertFalse(Context.isAuthenticated());
	}
	
	@Test
	public void shouldRedirectToTheLoginUrl() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GP_LOGIN_URL, " /spa/login "));
		
		Assertions.assertEquals("/somecontextpath/spa/login", logout());
		Assertions.assertFalse(Context.isAuthenticated());
	}
	
	@Test
	public void shouldStripEveryLeadingSlashFromTheLoginUrl() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GP_LOGIN_URL, "//evil.example/x"));
		
		Assertions.assertEquals("/evil.example/x", logout(""));
	}
	
	@Test
	public void shouldRedirectToTheLoginUrlWhenTheSessionIsNotAuthenticated() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GP_LOGIN_URL, "spa/login"));
		Context.logout();
		
		Assertions.assertEquals("/somecontextpath/spa/login", logout());
	}
	
	@Test
	public void shouldRedirectToIndexWhenTheLoginUrlContainsABackslash() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GP_LOGIN_URL, "/\\/evil.example/x"));
		
		Assertions.assertEquals("/index.htm?noredirect=true", logout(""));
	}
	
	@Test
	public void shouldRedirectToIndexWhenTheLoginUrlContainsAControlCharacter() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GP_LOGIN_URL, "/\t/evil.example/x"));
		
		Assertions.assertEquals("/index.htm?noredirect=true", logout(""));
	}
	
	@Test
	public void shouldLogOutWhenReadingTheLoginUrlFails() throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		AdministrationService failingAdminService = Mockito.mock(AdministrationService.class);
		Mockito.when(failingAdminService.getGlobalProperty(OpenmrsConstants.GP_LOGIN_URL)).thenThrow(
		    new RuntimeException("db down"));
		ServiceContext.getInstance().setAdministrationService(failingAdminService);
		try {
			Assertions.assertThrows(RuntimeException.class, this::logout);
		}
		finally {
			ServiceContext.getInstance().setAdministrationService(adminService);
		}
		Assertions.assertFalse(Context.isAuthenticated());
	}
}
