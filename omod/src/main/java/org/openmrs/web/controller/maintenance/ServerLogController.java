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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.MemoryAppender;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Get the log lines from the MEMORY_APPENDER appender of log4j as a String list and give it to the
 * view.
 * 
 * @see org.openmrs.util.MemoryAppender
 */
public class ServerLogController extends SimpleFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private volatile MethodHandle getMemoryAppenderHandle = null;
	
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
		if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, "2.4.* - 2.*")) {
			try {
				if (getMemoryAppenderHandle == null) {
					synchronized (ServerLogController.class) {
						if (getMemoryAppenderHandle == null) {
							getMemoryAppenderHandle = MethodHandles.publicLookup().findStatic(OpenmrsUtil.class,
							    "getMemoryAppender", MethodType.methodType(MemoryAppender.class));
						}
					}
				}
				
				MemoryAppender memoryAppender = (MemoryAppender) getMemoryAppenderHandle.invoke();
				log.info("Server log was accessed");
				return memoryAppender.getLogLines();
			}
			catch (Throwable e) {
				log.error("Error while loading memoryAppender", e);
			}
		}
		
		Appender appender = Logger.getRootLogger().getAppender("MEMORY_APPENDER");
		if (appender instanceof MemoryAppender) {
			MemoryAppender memoryAppender = (MemoryAppender) appender;
			log.info("Server log was accessed");
			return memoryAppender.getLogLines();
		}
		log.info("Server log was accessed");
		return Collections.emptyList();
	}
}
