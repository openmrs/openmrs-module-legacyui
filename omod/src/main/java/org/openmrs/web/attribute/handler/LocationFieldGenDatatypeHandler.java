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

import org.openmrs.Location;
import org.openmrs.customdatatype.datatype.LocationDatatype;
import org.springframework.stereotype.Component;

/**
 * Handler for the Location custom datatype
 */
@Component
public class LocationFieldGenDatatypeHandler extends BaseMetadataFieldGenDatatypeHandler<LocationDatatype, Location> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(String)
	 */
	@Override
	public void setHandlerConfiguration(String handlerConfig) {
		// not used
	}
	
	/**
	 * @see FieldGenDatatypeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "org.openmrs.Location";
	}
	
	/**
	 * @see FieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
}
