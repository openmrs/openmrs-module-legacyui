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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public class XSSFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		
		if (!"GET".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
			if (ServletFileUpload.isMultipartContent((HttpServletRequest) request)) {
				request = new XSSMultipartRequestWrapper((DefaultMultipartHttpServletRequest) request);
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
