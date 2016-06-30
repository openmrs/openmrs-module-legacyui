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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.directwebremoting.servlet.DwrServlet;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Simply used so that we have a way we can restart the DWR HttpServlet
 */
public class OpenmrsDWRServlet extends DwrServlet {
	
	private static final long serialVersionUID = 121212111335789L;
	
	/**
	 * Overriding the init(ServletConfig) method to save the dwr servlet to the ModuleWebUtil class
	 */
	public void init(ServletConfig config) throws ServletException {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		DwrServletConfig conf =  new DwrServletConfig(config.getServletName(), config.getServletContext());
		conf.setInitParameter("debug", "false");
		conf.setInitParameter("crossDomainSessionSecurity", "false");
		conf.setInitParameter("config-modules", "/WEB-INF/dwr-modules.xml");
		
		super.init(conf);
	}
	
	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		
		HttpServletRequest request = (HttpServletRequest)req;
		
		String uri = request.getRequestURI();
		uri = uri.replace("/ms/legacyui/dwr-invoker/", "/dwr/");
		
		String url = request.getRequestURL().toString();
		url = url.replace("/ms/legacyui/dwr-invoker/", "/dwr/");
		
		String pathInfo = request.getPathInfo();
		pathInfo = pathInfo.replace("/legacyui/dwr-invoker/", "/");
		
		request = new DwrServletRequest(request, uri, url, pathInfo);
		
		super.service(request, res);
	}

	/**
	 * This method is called to remake all of the dwr methods
	 * 
	 * @throws ServletException
	 */
	public void reInitServlet() throws ServletException {
		init(this.getServletConfig());
	}
	
	/**
	 * Our module engine does not cater for servlet init parameters which are
	 * required by the DWR servlet. So this class ensures that we keep
	 * the servlet parameters and pass them over to the DWR engine.
	 */
	public static class DwrServletConfig implements ServletConfig {
		
		private String name;
		
		private ServletContext servletContext;
		
		private Map<String, String> params = new HashMap<String, String>();
		
		public DwrServletConfig(String name, ServletContext servletContext) {
			this.name = name;
			this.servletContext = servletContext;
		}
		
		public String getServletName() {
			return name;
		}
		
		public ServletContext getServletContext() {
			return servletContext;
		}
		
		public String getInitParameter(String paramName) {
			return params.get(paramName);
		}
		
		@SuppressWarnings("unchecked")
		public Enumeration getInitParameterNames() {
			return new IteratorEnumeration(params.keySet().iterator());
		}
		
		public void setInitParameter(String name, String value) {
			params.put(name, value);
		}
	}
}