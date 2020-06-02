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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	private static final boolean IS_2_4_OR_NEWER = ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT,
	    "2.4.*-2.*");
	
	private static MethodHandle getRootLogger = null;
	
	private static MethodHandle getAppenders = null;
	
	private static MethodHandle getLogLines = null;
	
	protected final Log log = LogFactory.getLog(getClass());
	
	static {
		if (IS_2_4_OR_NEWER) {
			try {
				Class<?> logManagerClass = Class.forName("org.apache.logging.log4j.LogManager");
				Class<?> loggerClass = Class.forName("org.apache.logging.log4j.Logger");
				Class<?> coreLoggerClass = Class.forName("org.apache.logging.log4j.core.Logger");
				Class<?> memoryAppenderClass = Class.forName("org.openmrs.util.MemoryAppender");
				
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				
				MethodType getLoggerType = MethodType.methodType(loggerClass);
				getRootLogger = lookup.findStatic(logManagerClass, "getRootLogger", getLoggerType);
				getRootLogger.asType(getRootLogger.type().changeReturnType(coreLoggerClass));
				
				MethodType getAppendersType = MethodType.methodType(Map.class);
				getAppenders = lookup.findVirtual(coreLoggerClass, "getAppenders", getAppendersType);
				getAppenders.asType(getAppenders.type().changeReturnType(Map.class));
				
				MethodType getLogLinesType = MethodType.methodType(List.class);
				getLogLines = lookup.findVirtual(memoryAppenderClass, "getLogLines", getLogLinesType);
				getLogLines.asType(getLogLines.type().changeReturnType(List.class));
			}
			catch (Exception ignore) {}
		}
	}
	
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
	@SuppressWarnings("unchecked")
	protected List<String> formBackingObject(HttpServletRequest request) throws ServletException {
		// when using Log4j2, the classes for this have changed. To maintain backwards compatibility, we need to load the
		// memory appender via reflection
		if (IS_2_4_OR_NEWER && getRootLogger != null && getAppenders != null && getLogLines != null) {
			try {
				Object logger = getRootLogger.invoke();
				Map<String, ?> appenders = (Map<String, ?>) getAppenders.invoke(logger);
				Object memoryAppender = appenders.get("MEMORY_APPENDER");
				return (List<String>) getLogLines.invoke(memoryAppender);
			}
			catch (Throwable e) {
				log.debug("Caught an exception trying to load Log4j2 logger", e);
			}
		}

		Appender appender = Logger.getRootLogger().getAppender("MEMORY_APPENDER");
		if (appender instanceof MemoryAppender) {
			MemoryAppender memoryAppender = (MemoryAppender) appender;
			return memoryAppender.getLogLines();
		} else {
			return new ArrayList<>();
		}
	}
}
