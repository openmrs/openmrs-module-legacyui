/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.attribute.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.springframework.stereotype.Component;

/**
 * A handler for the {@link RegexValidatedTextDatatype}.
 */
@Component
public class RegexValidatedTextDatatypeHandler implements FieldGenDatatypeHandler<RegexValidatedTextDatatype, java.lang.String> {
	
	
	private static final Log log = LogFactory.getLog(RegexValidatedTextDatatypeHandler.class);
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(String)
	 */
	@Override
	public void setHandlerConfiguration(String arg0) {
		// not used
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "java.lang.String";
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(CustomDatatype,
	 *      HttpServletRequest, String)
	 */
	@Override
	public String getValue(RegexValidatedTextDatatype datatype, HttpServletRequest request, String formFieldName)
	        throws InvalidCustomValueException {
		String stringVal = request.getParameter(formFieldName);
		try {
			datatype.validate(stringVal);
		}
		catch (InvalidCustomValueException ex) {
			throw new InvalidCustomValueException("Invalid value: " + stringVal);
		}
		return stringVal;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtmlSummary(CustomDatatype,
	 *      String)
	 */
	@Override
	public CustomDatatype.Summary toHtmlSummary(CustomDatatype<String> datatype, String valueReference) {
		return new CustomDatatype.Summary(toHtml(datatype, valueReference), true);
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(CustomDatatype,
	 *      String)
	 */
	@Override
	public String toHtml(CustomDatatype<String> datatype, String valueReference) {
		return valueReference;
	}
}
