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

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.util.EnumSet;

import org.openmrs.contrib.dwr.servlet.EfficientShutdownServletContextAttributeListener;
import org.openmrs.module.web.filter.AdminPageFilter;
import org.openmrs.module.web.filter.ForcePasswordChangeFilter;
import org.openmrs.module.web.filter.RedirectAfterLoginFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class WebComponentRegistrar implements ServletContextAware {
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		
		try {
			String[] mappings = { "*.htm", "*.form", "*.list", "*.json", "*.field", "*.portlet", "*.page", "*.action" };
			
			ServletRegistration openmrsServletReg = servletContext.getServletRegistration("openmrs");
			addMappings(openmrsServletReg, mappings);
			
			addMappings(servletContext.getServletRegistration("jsp"), "*.withjstl");
			
			Dynamic filter = servletContext.addFilter("redirectAfterLoginFilter", new RedirectAfterLoginFilter());
			filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, mappings);
			
			filter = servletContext.addFilter("forcePasswordChangeFilter", new ForcePasswordChangeFilter());
			filter.setInitParameter("changePasswordForm", "/admin/users/changePassword.form");
			filter.setInitParameter("excludeURL", "changePasswordForm,csrfguard,logout,.js,.css,.gif,.jpg,.jpeg,.png");
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
		if (reg == null) {
			return;
		}
		for (String mapping : mappings) {
			reg.addMapping(mapping);
		}
	}
	
}
