/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.filter;

import static org.openmrs.web.WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter serves to set the redirect url on the session, if any GET request is received for an
 * unauthenticated user, so that any downstream redirection after login can bring the user to the
 * page they had been requesting.
 */
public class RedirectAfterLoginFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(RedirectAfterLoginFilter.class);
	
	private FilterConfig config;
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest httpReq = (HttpServletRequest) req;
			String requestURI = httpReq.getRequestURI();
			if (!Context.isAuthenticated()) {
				if ("GET".equalsIgnoreCase(httpReq.getMethod()) && !requestURI.contains("login.")) {
					HttpSession session = httpReq.getSession(false);
					if (session != null && session.getAttribute(OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR) == null) {
						String queryParams = httpReq.getQueryString();
						String redirectUrl = requestURI + (StringUtils.isNotBlank(queryParams) ? "?" + queryParams : "");
						httpReq.getSession().setAttribute(OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, redirectUrl);
						if (log.isDebugEnabled()) {
							log.debug("Set {} = {}", OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, redirectUrl);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("An error occurred while setting session attribute " + OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, e);
		}
		chain.doFilter(req, res);
	}
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}
}
