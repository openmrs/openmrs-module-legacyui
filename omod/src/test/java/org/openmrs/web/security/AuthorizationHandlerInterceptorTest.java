/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * Direct unit tests for {@link AuthorizationHandlerInterceptor#preHandle}. These tests do not
 * exercise the Spring URL mapping or any controller; they construct the handler reference directly
 * and assert the interceptor's authorization decisions.
 */
public class AuthorizationHandlerInterceptorTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String LIMITED_USER_DATASET = "org/openmrs/web/controller/hl7/include/HL7SourceFormControllerTest.xml";
	
	private AuthorizationHandlerInterceptor interceptor;
	
	@Before
	public void setUp() {
		interceptor = new AuthorizationHandlerInterceptor();
	}
	
	@Test
	public void preHandle_shouldAllowHandlerWithoutAnnotation() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/admin/foo.htm");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		boolean result = interceptor.preHandle(request, response, new UnannotatedHandler());
		
		assertTrue("Handler without @RequirePrivilege must be allowed (fail-open)", result);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void preHandle_shouldRedirectUnauthenticatedCallerToLogin() throws Exception {
		Context.logout();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/admin/hl7/hl7Source.form");
		request.setContextPath("/openmrs");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		boolean result = interceptor.preHandle(request, response, new UpdateHl7SourceHandler());
		
		assertFalse("Interceptor must short-circuit unauthenticated callers", result);
		assertEquals(302, response.getStatus());
		assertEquals("/openmrs/login.htm", response.getRedirectedUrl());
		assertEquals("require.login", request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR));
		assertNotNull(request.getSession().getAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR));
	}
	
	@Test
	public void preHandle_shouldRedirectAuthenticatedCallerLackingPrivilege() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);
		Context.logout();
		Context.authenticate("limiteduser", "test");
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/admin/hl7/hl7Source.form");
		request.setContextPath("/openmrs");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		boolean result = interceptor.preHandle(request, response, new UpdateHl7SourceHandler());
		
		assertFalse("Interceptor must reject authenticated callers without the required privilege", result);
		assertEquals(302, response.getStatus());
		assertEquals("/openmrs/login.htm", response.getRedirectedUrl());
		assertEquals(Boolean.TRUE, request.getSession().getAttribute(WebConstants.INSUFFICIENT_PRIVILEGES));
		assertEquals("Update HL7 Source", request.getSession().getAttribute(WebConstants.REQUIRED_PRIVILEGES));
	}
	
	@Test
	public void preHandle_shouldAllowAuthenticatedCallerWithPrivilege() throws Exception {
		Context.authenticate("admin", "test");
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/admin/hl7/hl7Source.form");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		boolean result = interceptor.preHandle(request, response, new UpdateHl7SourceHandler());
		
		assertTrue("Caller with the required privilege must be allowed through", result);
		assertNull(request.getSession(false) == null ? null : request.getSession().getAttribute(
		    WebConstants.INSUFFICIENT_PRIVILEGES));
	}
	
	@Test
	public void preHandle_authOnlyAnnotation_shouldRequireAuthenticationButNoPrivilege() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);
		Context.logout();
		Context.authenticate("limiteduser", "test"); // any authenticated user
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/admin/something");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		boolean result = interceptor.preHandle(request, response, new AuthOnlyHandler());
		
		assertTrue("Empty @RequirePrivilege must accept any authenticated caller", result);
	}
	
	@Test
	public void preHandle_requireAllSemantics_shouldRejectIfAnyMissing() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);
		Context.logout();
		Context.authenticate("admin", "test"); // admin has all privileges, this passes
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/admin/multi");
		request.setContextPath("/openmrs");
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertTrue("admin holds all listed privileges and must be allowed through",
		    interceptor.preHandle(request, response, new RequireAllHandler()));
		
		// hl7reader has only Get HL7 Source, missing Update HL7 Source - require-all must reject
		Context.logout();
		Context.authenticate("hl7reader", "test");
		MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/openmrs/admin/multi");
		request2.setContextPath("/openmrs");
		MockHttpServletResponse response2 = new MockHttpServletResponse();
		assertFalse("Caller missing one of the required privileges must be rejected under require-all semantics",
		    interceptor.preHandle(request2, response2, new RequireAllHandler()));
	}
	
	@Test
	public void preHandle_handlerMethod_shouldPreferMethodAnnotationOverClass() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);
		Context.logout();
		Context.authenticate("hl7reader", "test"); // has only Get HL7 Source
		
		Method readMethod = AnnotatedController.class.getDeclaredMethod("read");
		HandlerMethod handler = new HandlerMethod(new AnnotatedController(), readMethod);
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/admin/foo");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		boolean result = interceptor.preHandle(request, response, handler);
		
		assertTrue("Method-level @RequirePrivilege(Get HL7 Source) must override class-level "
		        + "@RequirePrivilege(Update HL7 Source) so a hl7reader can hit a read handler", result);
	}
	
	@RequirePrivilege("Update HL7 Source")
	private static class UpdateHl7SourceHandler {
		
	}
	
	@RequirePrivilege
	private static class AuthOnlyHandler {
		
	}
	
	@RequirePrivilege(value = { "Get HL7 Source", "Update HL7 Source" }, requireAll = true)
	private static class RequireAllHandler {
		
	}
	
	private static class UnannotatedHandler {
		
	}
	
	@RequirePrivilege("Update HL7 Source")
	public static class AnnotatedController {
		
		@RequirePrivilege("Get HL7 Source")
		public void read() {
		}
	}
}
