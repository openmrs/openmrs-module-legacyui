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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.WebConstants;

/**
 * Filters dwr requests and forwards them to the legacyui module dwr servlet
 */
public class DwrFilter implements Filter {

	protected final Log log = LogFactory.getLog(getClass());

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		
		String uri = ((HttpServletRequest)req).getRequestURI();
		uri = uri.replace("/dwr/", "/ms/legacyui/dwr-invoker/");
		uri = uri.replace("/ms/call/plaincall/", "/ms/legacyui/dwr-invoker/call/plaincall/");
		uri = uri.replace("/" + WebConstants.WEBAPP_NAME, "");
		
		req.getRequestDispatcher(uri).forward(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void destroy() {
		
	}
}