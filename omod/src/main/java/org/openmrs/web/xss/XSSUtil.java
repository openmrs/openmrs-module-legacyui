/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.xss;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang33.StringUtils;
import org.owasp.encoder.Encode;

public class XSSUtil {
	
	public static String sanitize(HttpServletRequest request, String name, String value) {
		String queryString = request.getQueryString();
		if (StringUtils.isNotBlank(queryString)
		        && (queryString.contains("&" + name + "=") || queryString.contains("?" + name + "="))) {
			return Encode.forUri(value);
		}
		
		return Encode.forHtmlContent(value);
	}
}
