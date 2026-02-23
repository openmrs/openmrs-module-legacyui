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

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.web.user.UserProperties;

/**
 * This filter checks if an authenticated user has been flagged by the admin to change his password
 * on first/subsequent login. It will intercept any requests made to a *.html or a *.form to force
 * the user to change his password.
 */
public class ForcePasswordChangeFilter implements Filter {

	private static boolean enabled = true;
	
	private String excludeURL;
	
	private String changePasswordForm;
	
	private FilterConfig config;
	
	private String[] excludedURLs;
	
	/**
	 * @see jakarta.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
	
	/**
	 * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest,
	 *      jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
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
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		excludeURL = config.getInitParameter("excludeURL");
		excludedURLs = excludeURL.split(",");
		changePasswordForm = config.getInitParameter("changePasswordForm");
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		ForcePasswordChangeFilter.enabled = enabled;
	}
}
