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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.owasp.encoder.Encode;

public class XSSRequestWrapper extends HttpServletRequestWrapper {
	
	public XSSRequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	@Override
	public String[] getParameterValues(String parameter) {
		String[] values = super.getParameterValues(parameter);
		if (values == null) {
			return null;
		}
		
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++) {
			encodedValues[i] = Encode.forHtmlContent(values[i]);
		}
		
		return encodedValues;
	}
	
	@Override
	public String getParameter(String name) {
		String value = super.getParameter(name);
		if (value == null) {
			return null;
		}
		
		return Encode.forHtmlContent(value);
	}
}
