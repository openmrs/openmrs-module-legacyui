/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.maintenance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.servlet.LoginServlet;
import org.openmrs.web.user.CurrentUsers;
import org.springframework.web.servlet.mvc.SimpleFormController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Display the current users logged in the system.
 * 
 * @see CurrentUsers
 * @see LoginServlet
 * @see org.openmrs.web.SessionListener
 */
public class CurrentUsersController extends SimpleFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(jakarta.servlet.http.HttpServletRequest)
	 */
	protected List<String> formBackingObject(HttpServletRequest request) throws ServletException {
		log.debug("List current users");
		if (!Context.hasPrivilege(PrivilegeConstants.GET_USERS)) {
			throw new APIAuthenticationException("Privilege required: " + PrivilegeConstants.GET_USERS);
		}
		return CurrentUsers.getCurrentUsernames(request.getSession());
	}
}
