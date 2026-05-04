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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.security.DwrAuthorizationFilter;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Verifies that {@link DwrAuthorizationFilter} blocks anonymous and unprivileged DWR calls to
 * {@link DWRAdministrationService} <em>before</em> they reach the underlying core service. On
 * Platform 2.0.0 the core {@code AdministrationService.getGlobalProperty(String)} method has no
 * {@code @Authorized} annotation, so without our filter an anonymous caller can read any global
 * property over DWR. This is the leak the original disclosure described.
 */
public class DWRAdministrationServiceTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String LIMITED_USER_DATASET = "org/openmrs/web/controller/hl7/include/HL7SourceFormControllerTest.xml";
	
	private DwrAuthorizationFilter filter;
	
	private RecordingFilterChain chain;
	
	@Before
	public void initFilter() throws Exception {
		filter = new DwrAuthorizationFilter();
		filter.init(null);
		chain = new RecordingFilterChain();
	}
	
	@Test
	public void getGlobalProperty_filterShouldRejectUnauthenticatedCaller() throws Exception {
		// preflight: confirm the value exists in the test dataset
		Context.authenticate("admin", "test");
		assertNotNull(Context.getAdministrationService().getGlobalProperty("locale.allowed.list"));
		Context.logout();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		        "/openmrs/dwr/call/plaincall/DWRAdministrationService.getGlobalProperty.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, chain);
		
		assertEquals("Unauthenticated caller must get 401, not be forwarded to the DWR servlet", 401, response.getStatus());
		assertFalse("Filter must not pass the request to the chain", chain.invoked);
	}
	
	@Test
	public void setGlobalProperty_filterShouldRejectUserWithoutManageGlobalProperties() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);
		Context.logout();
		Context.authenticate("limiteduser", "test"); // no privileges
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		        "/openmrs/dwr/call/plaincall/DWRAdministrationService.setGlobalProperty.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, chain);
		
		assertEquals("Caller without 'Manage Global Properties' must get 403", 403, response.getStatus());
		assertFalse("Filter must not pass the request to the chain", chain.invoked);
	}
	
	@Test
	public void filterShouldAllowAdmin() throws Exception {
		Context.authenticate("admin", "test");
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		        "/openmrs/dwr/call/plaincall/DWRAdministrationService.getGlobalProperty.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		filter.doFilter(request, response, chain);
		
		assertNotEquals("admin must not be 403'd", 403, response.getStatus());
		assertTrue("admin must be passed through to the chain", chain.invoked);
	}
	
	private static class RecordingFilterChain implements FilterChain {
		
		boolean invoked = false;
		
		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			invoked = true;
		}
	}
}
