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

import org.openmrs.Concept;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.datatype.ConceptDatatype;
import org.springframework.stereotype.Component;

/**
 * Handler for the Concept custom datatype
 */
@Component
public class ConceptFieldGenDatatypeHandler extends SerializingFieldGenDatatypeHandler<ConceptDatatype, Concept> {
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "org.openmrs.Concept";
	}
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#toHtml(CustomDatatype, String)
	 */
	@Override
	public String toHtml(CustomDatatype datatype, String valueReference) {
		return toHtmlSummary(datatype, valueReference).getSummary();
	}
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#toHtmlSummary(CustomDatatype, String)
	 */
	@Override
	public CustomDatatype.Summary toHtmlSummary(CustomDatatype datatype, String valueReference) {
		return datatype.getTextSummary(valueReference);
	}
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#setHandlerConfiguration(String)
	 */
	@Override
	public void setHandlerConfiguration(String s) {
		
	}
}
