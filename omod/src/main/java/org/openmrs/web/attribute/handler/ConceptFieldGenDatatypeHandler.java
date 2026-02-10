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

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.datatype.ConceptDatatype;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for the Concept custom datatype
 */
@Component
public class ConceptFieldGenDatatypeHandler extends SerializingFieldGenDatatypeHandler<ConceptDatatype, Concept> {

	ObjectMapper objectMapper = new ObjectMapper();
	Map<String, Object> widgetConfiguration = new HashMap<>();

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
		return widgetConfiguration;
	}
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#setHandlerConfiguration(String)
	 */
	@Override
	public void setHandlerConfiguration(String handlerConfig) {
		widgetConfiguration = new HashMap<>();
		if (StringUtils.isNotBlank(handlerConfig)) {
			try {
				widgetConfiguration.putAll(objectMapper.readValue(handlerConfig, Map.class));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse widget configuration", e);
			}
			Object showAnswersRef = widgetConfiguration.remove("showAnswers");
			if (showAnswersRef != null) {
				// Concept concept = Context.getConceptService().getConceptByReference(showAnswersRef.toString());
				Concept concept = Context.getConceptService().getConceptByUuid(showAnswersRef.toString());
				if (concept != null) {
					widgetConfiguration.put("showAnswers", concept.getConceptId());
				}
				else {
					widgetConfiguration.put("showAnswers", Integer.parseInt(showAnswersRef.toString()));
				}
			}
		}
	}
}
