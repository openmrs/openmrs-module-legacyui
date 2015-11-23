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

import org.directwebremoting.servlet.EfficientShutdownServletContextAttributeListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class LegacyUiServletContextSetup implements ServletContextAware {

	/**
	 * Setup ServletContext by adding listeners and filters.
	 * 
	 * Note that there's no way to remove listeners and filters from ServletContext thus
	 * restarting a container is needed after uninstalling the legacyui module.
	 */
	@Override
    public void setServletContext(ServletContext servletContext) {
		/* 
		 * EfficientShutdownServletContextAttributeListener is used instead of 
		 * EfficientShutdownServletContextListener since the latter implements ServletContextListener,
		 * which is not supported by ServletContext.addListener.
		*/
		servletContext.addListener(EfficientShutdownServletContextAttributeListener.class); 
	}
}
