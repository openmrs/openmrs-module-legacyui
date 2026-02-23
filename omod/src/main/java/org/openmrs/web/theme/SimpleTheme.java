/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.theme;

import org.springframework.context.MessageSource;

/**
 * Local replacement for org.springframework.ui.context.support.SimpleTheme which was removed in Spring 6.
 */
public class SimpleTheme implements Theme {
	
	private final String name;
	
	private final MessageSource messageSource;
	
	public SimpleTheme(String name, MessageSource messageSource) {
		this.name = name;
		this.messageSource = messageSource;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public MessageSource getMessageSource() {
		return this.messageSource;
	}
}
