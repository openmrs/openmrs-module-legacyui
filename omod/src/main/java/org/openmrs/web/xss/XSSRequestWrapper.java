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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
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
			encodedValues[i] = Encode.forHtml(values[i]);
		}
		
		return encodedValues;
	}
	
	@Override
	public String getParameter(String name) {
		
		String value = super.getParameter(name);
		if (value == null) {
			return null;
		}
		
		return Encode.forHtml(value);
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		
		String requestBody = IOUtils.toString(super.getInputStream(), StandardCharsets.UTF_8.name());
		String sanitizedBody = Encode.forHtmlContent(requestBody);
		
		return new ServletInputStream() {
			
			private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sanitizedBody.getBytes());
			
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		};
	}
}
