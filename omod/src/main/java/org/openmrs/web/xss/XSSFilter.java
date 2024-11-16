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

import java.io.IOException;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class XSSFilter extends OncePerRequestFilter {
	
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		
		String method = request.getMethod();
		String contentType = request.getContentType();
		if (!"GET".equals(method) && !"OPTIONS".equals(method) && !"HEAD".equalsIgnoreCase(method) && contentType != null) {
			if (contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
				if (!(request instanceof MultipartHttpServletRequest)) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				
				request = new XSSMultipartRequestWrapper((MultipartHttpServletRequest) request);
			} else if (contentType.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")) {
				request = new XSSRequestWrapper(request);
			}
		}
		
		chain.doFilter(request, response);
	}
}
