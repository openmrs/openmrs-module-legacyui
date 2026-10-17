/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

/**
 * Servlet called by the logout link in the webapp. This will call Context.logout() and then make
 * sure the current user's http session is cleaned up and ready for another user to log in
 * 
 * @see Context#logout()
 */
public class LogoutServlet extends HttpServlet {
	
	public static final long serialVersionUID = 123423L;
	
	public static final String GP_LOGOUT_REDIRECT_URL = "legacyui.logoutRedirectUrl";
	
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession httpSession = request.getSession();
		
		String redirect = StringUtils.stripStart(
		    StringUtils.trimToEmpty(Context.getAdministrationService().getGlobalProperty(GP_LOGOUT_REDIRECT_URL)), "/");
		
		Context.logout();
		
		response.sendRedirect(request.getContextPath() + "/"
		        + (redirect.isEmpty() ? "index.htm?noredirect=true" : redirect));
		
		// clears attributes and makes sure that no one can access this session
		httpSession.invalidate();
	}
	
}
