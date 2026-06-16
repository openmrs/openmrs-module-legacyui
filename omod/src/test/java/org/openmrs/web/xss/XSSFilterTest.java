/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.xss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link XSSFilter}. These assert the central contract: web-service request bodies
 * (REST/FHIR under {@code /ws/}) must pass through untouched, while legacy server-rendered form
 * parameters are still HTML-sanitized.
 */
public class XSSFilterTest {

	private XSSFilter filter;

	private CapturingFilterChain chain;

	@BeforeEach
	public void setUp() {
		filter = new XSSFilter();
		chain = new CapturingFilterChain();
	}

	@Test
	public void doFilter_shouldPassWebServiceJsonBodyThroughUnchanged() throws Exception {
		String json = "{\"value\":\"Hello <> & friends\"}";
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/ws/rest/v1/obs");
		request.setContextPath("/openmrs");
		request.setContent(json.getBytes(StandardCharsets.UTF_8));

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertNotNull(chain.capturedRequest);
		String body = IOUtils.toString(chain.capturedRequest.getInputStream(), StandardCharsets.UTF_8.name());
		assertEquals(json, body,
		    "Web-service request bodies must reach the resource layer as raw bytes, never HTML-encoded");
	}

	@Test
	public void doFilter_shouldPassFhirRequestBodyThroughUnchanged() throws Exception {
		String json = "{\"valueString\":\"a < b & c > d\"}";
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/ws/fhir2/R4/Observation");
		request.setContextPath("/openmrs");
		request.setContent(json.getBytes(StandardCharsets.UTF_8));

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertNotNull(chain.capturedRequest);
		String body = IOUtils.toString(chain.capturedRequest.getInputStream(), StandardCharsets.UTF_8.name());
		assertEquals(json, body, "FHIR request bodies must also pass through untouched");
	}

	@Test
	public void doFilter_shouldSanitizeLegacyFormParameters() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/admin/concepts/concept.form");
		request.setContextPath("/openmrs");
		request.setParameter("name", "<script>alert(1)</script>");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertNotNull(chain.capturedRequest);
		assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", chain.capturedRequest.getParameter("name"),
		    "Legacy form parameters must still be HTML-encoded to defend server-rendered pages");
	}

	@Test
	public void doFilter_shouldNotWrapWebServiceRequestsSoParametersAreUntouched() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/ws/rest/v1/concept");
		request.setContextPath("/openmrs");
		request.setParameter("q", "<x>");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertNotNull(chain.capturedRequest);
		assertEquals("<x>", chain.capturedRequest.getParameter("q"),
		    "Web-service requests are not wrapped, so their parameters are not HTML-encoded either");
	}

	@Test
	public void doFilter_shouldSanitizeLegacyRequestWhoseUrlMerelyContainsWsElsewhere() throws Exception {
		// A legacy endpoint reached via a URL that contains "/ws/" somewhere other than the path
		// prefix must NOT be treated as a web service, otherwise it could evade param sanitization.
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/openmrs/patientDashboard/ws/edit.form");
		request.setContextPath("/openmrs");
		request.setParameter("name", "<x>");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertNotNull(chain.capturedRequest);
		assertEquals("&lt;x&gt;", chain.capturedRequest.getParameter("name"),
		    "Only a /ws/ prefix (after the context path) is a web service; legacy paths stay sanitized");
	}

	private static class CapturingFilterChain implements FilterChain {

		private HttpServletRequest capturedRequest;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			this.capturedRequest = (HttpServletRequest) request;
		}
	}
}