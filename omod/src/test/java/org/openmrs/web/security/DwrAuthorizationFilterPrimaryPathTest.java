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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/**
 * Covers the primary code path that {@link DwrAuthorizationFilterTest} cannot reach: resolving
 * {@code WEB-INF/dwr-modules.xml} through a real {@link jakarta.servlet.FilterConfig}/
 * {@link jakarta.servlet.ServletContext}, scanning it with {@link org.openmrs.util.OpenmrsClassLoader},
 * and reloading on a change to the file's last-modified time. {@link DwrAuthorizationFilterTest}
 * calls {@link DwrAuthorizationFilter#init(jakarta.servlet.FilterConfig)} with {@code null}, which
 * only ever exercises the classpath fallback ({@code loadFallbackOnce}).
 */
public class DwrAuthorizationFilterPrimaryPathTest extends BaseModuleWebContextSensitiveTest {

	private File webappRoot;

	private File dwrXml;

	private DwrAuthorizationFilter filter;

	private RecordingFilterChain chain;

	@BeforeEach
	public void setUpFilter() throws Exception {
		webappRoot = Files.createTempDirectory("dwr-webapp").toFile();
		File webInf = new File(webappRoot, "WEB-INF");
		assertTrue(webInf.mkdirs() || webInf.isDirectory());
		dwrXml = new File(webInf, "dwr-modules.xml");
		writeDwrXml(false);
		// a "file:" resource base makes MockServletContext.getRealPath resolve into the temp dir
		MockServletContext servletContext = new MockServletContext("file:" + webappRoot.getAbsolutePath());
		filter = new DwrAuthorizationFilter();
		filter.init(new MockFilterConfig(servletContext, "dwrAuthorization"));
		chain = new RecordingFilterChain();
	}

	private void writeDwrXml(boolean includeStopMethod) throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<dwr>\n  <allow moduleId=\"legacyui\">\n"
		        + "    <create creator=\"new\" javascript=\"DWRHL7Service\">\n"
		        + "      <param name=\"class\" value=\"org.openmrs.web.dwr.DWRHL7Service\"/>\n"
		        + "      <include method=\"startHl7ArchiveMigration\"/>\n"
		        + (includeStopMethod ? "      <include method=\"stopHl7ArchiveMigration\"/>\n" : "")
		        + "    </create>\n  </allow>\n</dwr>\n";
		Files.write(dwrXml.toPath(), xml.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void init_shouldScanAggregatedDwrModulesXmlNotClasspathFallback() {
		// the fallback (legacyui's own config.xml) would register far more than one method
		assertEquals(1, filter.getPrivilegesByScriptMethod().size());
		assertNotNull(filter.getPrivilegesByScriptMethod().get("DWRHL7Service.startHl7ArchiveMigration"));
	}

	@Test
	public void doFilter_shouldPickUpChangesToDwrModulesXml() throws Exception {
		assertNull(filter.getPrivilegesByScriptMethod().get("DWRHL7Service.stopHl7ArchiveMigration"));

		long before = dwrXml.lastModified();
		writeDwrXml(true);
		assertTrue(dwrXml.setLastModified(before + 5000), "must bump mtime past fs timestamp granularity");

		Context.logout();
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		    "/openmrs/dwr/call/plaincall/DWRHL7Service.stopHl7ArchiveMigration.dwr");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus(),
		    "after reload the method is annotated, so an unauthenticated call must be rejected");
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
