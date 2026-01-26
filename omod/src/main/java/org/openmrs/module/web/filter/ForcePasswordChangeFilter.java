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

import org.openmrs.api.context.Context;
import org.openmrs.util.ConfigUtil;
import org.openmrs.web.user.UserProperties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This filter checks if an authenticated user has been flagged by the admin to change his password
 * on first/subsequent login. It will intercept any requests made to a *.html or a *.form to force
 * the user to change his password.
 */
public class ForcePasswordChangeFilter implements Filter {

	private boolean enabled = true;
	
	private String excludeURL;
	
	private String changePasswordForm;
	
	private FilterConfig config;
	
	private String[] excludedURLs;
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
		
		if (enabled && Context.isAuthenticated()
		        && new UserProperties(Context.getAuthenticatedUser().getUserProperties()).isSupposedToChangePassword()
		        && shouldNotAllowAccessToUrl(requestURI)) {
			config.getServletContext().getRequestDispatcher(changePasswordForm).forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Method to check if the request url is an excluded url.
	 * 
	 * @param requestURI
	 * @return
	 */
	private boolean shouldNotAllowAccessToUrl(String requestURI) {
		// /ws is reserved
		if (requestURI.startsWith("/ws")) {
			return false;
		}
		
		for (String url : excludedURLs) {
			if (requestURI.endsWith(url)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		enabled = !"false".equalsIgnoreCase(getParameter("enabled"));
		excludeURL = getParameter("excludeURL");
		excludedURLs = excludeURL.split(",");
		changePasswordForm = getParameter("changePasswordForm");
	}

	private String getParameter(String name) {
		String propertyName = "legacyui.passwordChangeFilter." + name;
		return ConfigUtil.getProperty(propertyName, name);
	}
}
