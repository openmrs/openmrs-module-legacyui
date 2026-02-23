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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * This class is to allow us modify the http servlet request's uri, url, and path info to those
 * expected by the dwr servlet
 */
public class DwrServletRequest extends HttpServletRequestWrapper {
	
	private String uri;
	
	private String url;
	
	private String pathInfo;
	
	public DwrServletRequest(HttpServletRequest request, String uri, String url, String pathInfo) {
		super(request);
		this.uri = uri;
		this.url = url;
		this.pathInfo = pathInfo;
	}
	
	@Override
	public String getPathInfo() {
		return pathInfo;
	}
	
	@Override
	public String getRequestURI() {
		return uri;
	}
	
	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer(url);
	}
}
