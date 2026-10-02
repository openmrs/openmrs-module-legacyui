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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class XSSFilterTest {
	
	@Test
	public void doFilter_shouldWrapMultipartRequestsThatAreNotDefaultMultipartRequests() throws Exception {
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[] { 1, 2, 3 });
		request.addFile(file);
		request.addParameter("fileCaption", "<script>alert(1)</script>");
		
		AtomicReference<ServletRequest> passedOn = new AtomicReference<>();
		FilterChain chain = (req, res) -> passedOn.set(req);
		
		new XSSFilter().doFilter(request, new MockHttpServletResponse(), chain);
		
		assertTrue(passedOn.get() instanceof XSSMultipartRequestWrapper);
		MultipartHttpServletRequest wrapped = (MultipartHttpServletRequest) passedOn.get();
		MultipartFile wrappedFile = wrapped.getFile("file");
		assertSame(file, wrappedFile);
		assertSame(file, wrapped.getFileMap().get("file"));
		assertEquals("file", wrapped.getFileNames().next());
		assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", wrapped.getParameter("fileCaption"));
	}
}
