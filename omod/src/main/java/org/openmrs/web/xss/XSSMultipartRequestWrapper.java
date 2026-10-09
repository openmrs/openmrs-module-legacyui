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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public class XSSMultipartRequestWrapper extends DefaultMultipartHttpServletRequest {
	
	private final MultipartHttpServletRequest multipartRequest;
	
	/**
	 * @param request the request to pass on, which another filter may have wrapped around the
	 *            multipart request
	 * @param multipartRequest the multipart request, which holds the files
	 */
	public XSSMultipartRequestWrapper(HttpServletRequest request, MultipartHttpServletRequest multipartRequest) {
		super(request);
		this.multipartRequest = multipartRequest;
	}
	
	@Override
	public String getParameter(String name) {
		
		String value = getRequest().getParameter(name);
		if (value == null) {
			return null;
		}
		
		return XSSUtil.sanitize(this, name, value);
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
			encodedValues[i] = XSSUtil.sanitize(this, name, values[i]);
		}
		
		return encodedValues;
	}
	
	@Override
	public MultipartFile getFile(String name) {
		return multipartRequest.getFile(name);
	}
	
	@Override
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return multipartRequest.getMultiFileMap();
	}
	
	@Override
	public Enumeration<String> getParameterNames() {
		return getRequest().getParameterNames();
	}
	
	@Override
	public List<MultipartFile> getFiles(String name) {
		return multipartRequest.getFiles(name);
	}
	
	@Override
	public Map<String, MultipartFile> getFileMap() {
		return multipartRequest.getFileMap();
	}
	
	@Override
	public Iterator<String> getFileNames() {
		return multipartRequest.getFileNames();
	}
	
	@Override
	public String getMultipartContentType(String paramOrFileName) {
		return multipartRequest.getMultipartContentType(paramOrFileName);
	}
	
	@Override
	public HttpHeaders getMultipartHeaders(String paramOrFileName) {
		return multipartRequest.getMultipartHeaders(paramOrFileName);
	}
}
