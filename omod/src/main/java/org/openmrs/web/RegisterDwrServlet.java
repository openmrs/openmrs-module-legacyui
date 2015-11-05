/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.dwr.OpenmrsDWRServlet;
import org.springframework.web.context.ServletContextAware;

public class RegisterDwrServlet implements ServletContextAware {
	
	private static final Log log = LogFactory.getLog(RegisterDwrServlet.class);

	@Override
	public void setServletContext(ServletContext servletContext) {
		
		try {
			ServletRegistration reg = servletContext.addServlet("dwr-invoker", OpenmrsDWRServlet.class);
			reg.addMapping("/dwr/*");
			reg.setInitParameter("debug", "false");
			reg.setInitParameter("config-modules", "/WEB-INF/dwr-modules.xml");
		}
		catch (IllegalStateException ex) {
			//ignore if servlet is already registered.
			log.warn("DWR Servlet Registration Failure", ex);
		}
	}
}
