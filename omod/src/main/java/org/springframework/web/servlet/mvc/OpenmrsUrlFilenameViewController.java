/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.springframework.web.servlet.mvc;

public class OpenmrsUrlFilenameViewController extends UrlFilenameViewController {
	
	@Override
	protected String getViewNameForUrlPath(String uri) {
		if ("/".equals(uri)) {
			return "index";
		}
		return super.getViewNameForUrlPath(uri);
	}
	
}
