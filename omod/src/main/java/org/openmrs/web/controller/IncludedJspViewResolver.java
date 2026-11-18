/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Resolves the JSP for an HTML fragment that is included into a JSP page, such as a portlet or a
 * fieldGen field. Such a view must not go through core's ContentNegotiatingViewResolver: that picks
 * a view from the Accept header and treats a wildcard Accept as application/json, rendering the
 * model (with its Hibernate object graph) as JSON or XML into the page instead of the fragment.
 */
public class IncludedJspViewResolver {

	private static final String JSP_VIEW_RESOLVER = "jspViewResolver";

	private static final Log log = LogFactory.getLog(IncludedJspViewResolver.class);

	private IncludedJspViewResolver() {
	}

	/**
	 * @return the JSP view from core's jspViewResolver, or null if that resolver is not available
	 */
	public static View resolveJsp(HttpServletRequest request, String viewName) {
		WebApplicationContext wac = RequestContextUtils.findWebApplicationContext(request);
		if (wac == null || !wac.containsBean(JSP_VIEW_RESOLVER)) {
			return null;
		}
		try {
			return wac.getBean(JSP_VIEW_RESOLVER, ViewResolver.class).resolveViewName(viewName,
			    RequestContextUtils.getLocale(request));
		}
		catch (Exception e) {
			log.warn("Unable to resolve the JSP for " + viewName, e);
			return null;
		}
	}
}
