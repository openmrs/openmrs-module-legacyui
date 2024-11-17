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

import java.util.Enumeration;
import java.util.List;

import org.owasp.encoder.Encode;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public class XSSMultipartRequestWrapper extends DefaultMultipartHttpServletRequest {
	
	public XSSMultipartRequestWrapper(DefaultMultipartHttpServletRequest request) {
		super(request);
	}
	
	@Override
	public String getParameter(String name) {
		
		String value = getRequest().getParameter(name);
		if (value == null) {
			return null;
		}
		
		return Encode.forHtmlContent(value);
	}
	
	@Override
	public String[] getParameterValues(String name) {
		
		String[] values = getRequest().getParameterValues(name);
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
	public DefaultMultipartHttpServletRequest getRequest() {
		return (DefaultMultipartHttpServletRequest) super.getRequest();
	}
	
	@Override
	public MultipartFile getFile(String name) {
		return getRequest().getFile(name);
	}
	
	@Override
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return getRequest().getMultiFileMap();
	}
	
	@Override
	public Enumeration<String> getParameterNames() {
		return getRequest().getParameterNames();
	}
	
	@Override
	public List<MultipartFile> getFiles(String name) {
		return getRequest().getFiles(name);
	}
}
