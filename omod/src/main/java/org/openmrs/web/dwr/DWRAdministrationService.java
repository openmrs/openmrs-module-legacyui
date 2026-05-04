/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.security.RequirePrivilege;

/**
 *
 */
public class DWRAdministrationService {
	
	/**
	 * Gets the value of a global property
	 * 
	 * @param name
	 * @return property value
	 */
	@RequirePrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public String getGlobalProperty(String name) {
		return Context.getAdministrationService().getGlobalProperty(name);
	}
	
	/**
	 * Sets the value of a global property
	 * 
	 * @param name
	 * @param newValue
	 */
	@RequirePrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public void setGlobalProperty(String name, String newValue) {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(name, newValue));
	}
	
}
