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

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.directwebremoting.servlet.EfficientShutdownServletContextAttributeListener;
import org.openmrs.module.web.filter.AdminPageFilter;
import org.openmrs.module.web.filter.ForcePasswordChangeFilter;
import org.openmrs.web.servlet.LogoutServlet;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class WebComponentRegistrar implements ServletContextAware {
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		
		try {
			ServletRegistration openmrsServletReg = servletContext.getServletRegistration("openmrs");
			addMappings(openmrsServletReg, "*.htm", "*.form", "*.list", "*.json", "*.field", "*.portlet", "*.page", "*.action");
			
			addMappings(servletContext.getServletRegistration("jsp"), "*.withjstl");
			
			ServletRegistration servletReg = servletContext.addServlet("logoutServlet", new LogoutServlet());
			servletReg.addMapping("/logout");
			
			Dynamic filter = servletContext.addFilter("forcePasswordChangeFilter", new ForcePasswordChangeFilter());
			filter.setInitParameter("changePasswordForm", "/admin/users/changePassword.form");
			filter.setInitParameter("excludeURL", "changePasswordForm,logout,.js,.css,.gif,.jpg,.jpeg,.png");
			filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
			
			filter = servletContext.addFilter("adminPageFilter", new AdminPageFilter());
			filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/admin");
			
			servletContext.addListener(new SessionListener());
			/*
			 * EfficientShutdownServletContextAttributeListener is used instead of
			 * EfficientShutdownServletContextListener since the latter implements ServletContextListener,
			 * which is not supported by ServletContext.addListener.
			*/
			servletContext.addListener(new EfficientShutdownServletContextAttributeListener());
		}
		catch (Exception ex) {
			//TODO need a work around for: java.lang.IllegalStateException: Started
			//Unable to configure mapping for servlet because this servlet context has already been initialized.
			//This happens on running openmrs after InitializationFilter or UpdateFilter
			//hence requiring a restart to see any page other than index.htm
			//After a restart, all mappings will then happen within Listener.contextInitialized()
			ex.printStackTrace();
		}
	}
	
	private void addMappings(ServletRegistration reg, String... mappings) {
		for (String mapping : mappings) {
			reg.addMapping(mapping);
		}
	}
	
}
