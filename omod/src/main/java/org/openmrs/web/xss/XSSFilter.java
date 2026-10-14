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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import static org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload.isMultipartContent;

public class XSSFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		
		if (!"GET".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
			if (isMultipartContent((HttpServletRequest) request)) {
				// Another filter may have wrapped the multipart request, as Spring Security's firewall does
				MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(request,
				    MultipartHttpServletRequest.class);
				if (multipartRequest == null) {
					throw new ServletException("A multipart request reached XSSFilter without being resolved");
				}
				request = new XSSMultipartRequestWrapper((HttpServletRequest) request, multipartRequest);
			} else {
				request = new XSSRequestWrapper((HttpServletRequest) request);
			}
		}
		
		chain.doFilter(request, response);
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	@Override
	public void destroy() {
		
	}
}
