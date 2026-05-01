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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Direct unit tests for {@link DwrAuthorizationFilter}: URL parsing, startup scan of
 * {@code config.xml}, and per-request authorization. These tests do not depend on the DWR
 * servlet, on the DwrFilter rewrite, or on any specific DWR script class beyond the ones
 * already declared in legacyui's {@code config.xml}.
 */
public class DwrAuthorizationFilterTest extends BaseModuleWebContextSensitiveTest {

	private static final String LIMITED_USER_DATASET = "org/openmrs/web/controller/hl7/include/HL7SourceFormControllerTest.xml";

	private DwrAuthorizationFilter filter;

	private RecordingFilterChain chain;

	@BeforeEach
	public void setUp() throws Exception {
		filter = new DwrAuthorizationFilter();
		filter.init(null);
		chain = new RecordingFilterChain();
	}

	@Test
	public void init_shouldRegisterAtLeastTheAnnotatedHl7MigrationMethods() {
		// these three are the pilot annotations; if init succeeded, the map must include them
		assertNotNull(filter.getPrivilegesByScriptMethod().get("DWRHL7Service.startHl7ArchiveMigration"));
		assertNotNull(filter.getPrivilegesByScriptMethod().get("DWRHL7Service.stopHl7ArchiveMigration"));
		assertNotNull(filter.getPrivilegesByScriptMethod().get("DWRHL7Service.getMigrationStatus"));
	}

	@Test
	public void init_everyDeclaredDwrMethodShouldBeAnnotated() {
		// fail-closed contract: every method exposed in config.xml must carry @RequirePrivilege.
		// If this fails, a developer has added a DWR method without annotating it; the filter
		// will start up but reject that endpoint with 403 at request time.
		assertTrue(filter.getUnannotatedMethods().isEmpty(),
		    "All DWR methods must carry @RequirePrivilege. Unannotated: "
		            + filter.getUnannotatedMethods().keySet());
	}

	@Test
	public void extractScriptMethod_shouldParseStandardCallUrl() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/DWRHL7Service.startHl7ArchiveMigration.dwr");
		assertEquals("DWRHL7Service.startHl7ArchiveMigration", filter.extractScriptMethod(request));
	}

	@Test
	public void extractScriptMethod_shouldParseModuleServletPath() {
		// the rewrite-target path that DwrFilter would normally forward to
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/ms/legacyui/dwr-invoker/call/plaincall/DWRHL7Service.startHl7ArchiveMigration.dwr");
		assertEquals("DWRHL7Service.startHl7ArchiveMigration", filter.extractScriptMethod(request));
	}

	@Test
	public void extractScriptMethod_shouldReturnNullForStaticAssets() {
		assertNull(filter.extractScriptMethod(new MockHttpServletRequest("GET", "/openmrs/dwr/engine.js")));
		assertNull(filter.extractScriptMethod(new MockHttpServletRequest("GET", "/openmrs/dwr/util.js")));
		assertNull(filter.extractScriptMethod(new MockHttpServletRequest("GET",
		    "/openmrs/dwr/interface/DWRPatientService.js")));
	}

	@Test
	public void extractScriptMethod_shouldStripQueryString() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/DWRHL7Service.startHl7ArchiveMigration.dwr?foo=bar");
		// MockHttpServletRequest doesn't include query in getRequestURI() but we handle it defensively
		assertEquals("DWRHL7Service.startHl7ArchiveMigration", filter.extractScriptMethod(request));
	}

	@Test
	public void doFilter_shouldRejectUnauthenticatedCallerWith401() throws Exception {
		Context.logout();

		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/DWRHL7Service.startHl7ArchiveMigration.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertFalse(chain.invoked);
	}

	@Test
	public void doFilter_shouldRejectAuthenticatedCallerLackingPrivilegeWith403() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);
		Context.logout();
		Context.authenticate("limiteduser", "test");

		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/DWRHL7Service.startHl7ArchiveMigration.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(403, response.getStatus());
		assertFalse(chain.invoked);
	}

	@Test
	public void doFilter_shouldAllowAuthenticatedCallerWithRequiredPrivilege() throws Exception {
		Context.authenticate("admin", "test");

		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/DWRHL7Service.startHl7ArchiveMigration.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertTrue(chain.invoked, "admin holds all migration privileges and must be passed to the chain");
	}

	@Test
	public void doFilter_shouldPassStaticAssetsThroughWithoutAuth() throws Exception {
		Context.logout();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/dwr/engine.js");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertTrue(chain.invoked,
		    "Static DWR assets (engine.js, util.js, generated interface JS) must be reachable without auth");
	}

	@Test
	public void doFilter_unknownScriptMethod_shouldRejectEvenAdmin() throws Exception {
		// fail-closed: any URL that doesn't resolve to a registered annotated method is 403,
		// even for an admin. This protects against routing surprises and against future
		// regressions where a method gets added to a DWR class but not config.xml.
		Context.authenticate("admin", "test");

		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/NoSuchScript.noSuchMethod.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(403, response.getStatus(),
		    "Calls to {Script}.{method} pairs not declared in config.xml must be rejected even for admin");
		assertFalse(chain.invoked);
	}

	private static class RecordingFilterChain implements FilterChain {

		boolean invoked = false;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			invoked = true;
		}
	}
}
