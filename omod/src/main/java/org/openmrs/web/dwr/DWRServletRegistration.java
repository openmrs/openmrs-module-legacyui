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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

/**
 * This class is only here for the registration of the dwr servlet.
 * If we can get the ServletContext from any where in the module, we
 * can move this registration to there and get rid of this class.
 */
public class DWRServletRegistration extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {	
		ServletContext context = config.getServletContext();
		ServletRegistration reg = context.addServlet("dwr-invoker", OpenmrsDWRServlet.class);
		reg.addMapping("/dwr/*");
		reg.setInitParameter("debug", "false");
		reg.setInitParameter("config-modules", "/WEB-INF/dwr-modules.xml");
	}
}
