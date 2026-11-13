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

import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.logging.OpenmrsLoggingUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.openmrs.web.security.RequirePrivilege;
import org.openmrs.util.PrivilegeConstants;

/**
 * Get the log lines from the MEMORY_APPENDER appender of log4j2 as a String list and give it to the
 * view.
 * 
 * @see org.openmrs.logging.MemoryAppender
 */
@RequirePrivilege(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
public class ServerLogController extends SimpleFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected List<String> formBackingObject(HttpServletRequest request) throws ServletException {
		log.info("Server log was accessed");
		MemoryAppender memoryAppender = OpenmrsLoggingUtil.getMemoryAppender();
		if (memoryAppender == null) {
			return Collections.emptyList();
		}
		return memoryAppender.getLogLines();
	}
}
