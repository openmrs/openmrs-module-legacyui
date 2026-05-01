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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.Hl7InArchivesMigrateThread;
import org.openmrs.web.security.DwrAuthorizationFilter;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Verifies that the request-boundary filter ({@link DwrAuthorizationFilter})
 * blocks unauthorized DWR calls to {@link DWRHL7Service} <em>before</em> the
 * DWR servlet dispatches them to the Java method. Tests that invoke the DWR
 * class directly are not interesting here: the class has no auth check itself,
 * and the static helpers it calls have no {@code @Authorized}, so a direct
 * call will always succeed by design. The filter is the only thing standing
 * between an HTTP attacker and the migration thread; this suite exercises
 * exactly that boundary.
 */
public class DWRHL7ServiceTest extends BaseModuleWebContextSensitiveTest {

	private DwrAuthorizationFilter filter;

	private RecordingFilterChain chain;

	@BeforeEach
	public void initFilter() throws Exception {
		filter = new DwrAuthorizationFilter();
		filter.init(null);
		chain = new RecordingFilterChain();
	}

	@AfterEach
	public void resetMigrationState() {
		try {
			Hl7InArchivesMigrateThread.stopMigration();
		}
		catch (Exception ignore) {
		}
		Hl7InArchivesMigrateThread.setActive(false);
		DWRHL7Service.setHl7MigrationThread(null);
	}

	@Test
	public void startHl7ArchiveMigration_filterShouldRejectUnauthenticatedCaller() throws Exception {
		Context.logout();

		MockHttpServletRequest request = dwrRequest("DWRHL7Service", "startHl7ArchiveMigration");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus(),
		    "An unauthenticated caller hitting the DWR migration endpoint must get 401, not be forwarded to the servlet");
		assertFalse(chain.invoked, "Filter must not pass the request to the chain");
		assertFalse(Hl7InArchivesMigrateThread.isActive(),
		    "Migration must not have started while the filter was supposed to be rejecting the call");
	}

	@Test
	public void startHl7ArchiveMigration_filterShouldRejectUserWithoutPrivilege() throws Exception {
		executeDataSet("org/openmrs/web/controller/hl7/include/HL7SourceFormControllerTest.xml");
		Context.logout();
		Context.authenticate("limiteduser", "test"); // no privileges

		MockHttpServletRequest request = dwrRequest("DWRHL7Service", "startHl7ArchiveMigration");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(403, response.getStatus(),
		    "An authenticated caller without 'Manage HL7 Messages' must get 403");
		assertFalse(chain.invoked, "Filter must not pass the request to the chain");
		assertFalse(Hl7InArchivesMigrateThread.isActive(),
		    "Migration must not have started while the filter was supposed to be rejecting the call");
	}

	@Test
	public void startHl7ArchiveMigration_filterShouldAllowAdmin() throws Exception {
		Context.authenticate("admin", "test");

		MockHttpServletRequest request = dwrRequest("DWRHL7Service", "startHl7ArchiveMigration");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus(), "admin must be allowed through the filter");
		assertTrue(chain.invoked, "Filter must pass the request to the chain for an authorized caller");
	}

	@Test
	public void filterShouldIgnoreNonMethodCallUrls() throws Exception {
		Context.logout();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/dwr/engine.js");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertTrue(chain.invoked,
		    "Filter must let static DWR assets (engine.js, util.js, interface JS) pass through unauthenticated");
	}

	private MockHttpServletRequest dwrRequest(String script, String method) {
		return new MockHttpServletRequest("POST", "/openmrs/dwr/call/plaincall/" + script + "." + method + ".dwr");
	}

	private static class RecordingFilterChain implements FilterChain {

		boolean invoked = false;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			invoked = true;
		}
	}
}
