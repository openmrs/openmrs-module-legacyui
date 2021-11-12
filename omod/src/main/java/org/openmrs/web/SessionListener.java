/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.user.CurrentUsers;
import sun.security.provider.SecureRandom;
import sun.security.util.Cache;

import java.util.UUID;

/**
 * Handles events of session life cycle. <br>
 * <br>
 * This is set by the web.xml class
 */
public class SessionListener implements HttpSessionListener {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		// Check the user session for the salt cache, if none is present we create one
		Cache<String, Boolean> csrfPreventionSaltCache = (Cache<String, Boolean>) httpSessionEvent.getSession()
		        .getAttribute("csrfPreventionSaltCache");
		
		if (csrfPreventionSaltCache == null) {
			csrfPreventionSaltCache = Cache.newSoftMemoryCache(750, 20);
			httpSessionEvent.getSession().setAttribute("csrfPreventionSaltCache", csrfPreventionSaltCache);
		}
		
		// Generate the salt and store it in the users cache
		String salt = UUID.randomUUID().toString();
		csrfPreventionSaltCache.put(salt, Boolean.TRUE);
		
		// Add the salt to the current request so it can be used
		// by the page rendered in this request
		//httpSessionEvent.setAttribute("csrfPreventionSalt", salt);
		
	}
	
	/**
	 * Called whenever a session times out or a user logs out (and so the session is closed)
	 * 
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		Cache<String, Boolean> csrfPreventionSaltCache = (Cache<String, Boolean>) httpSessionEvent.getSession()
		        .getAttribute("csrfPreventionSaltCache");
		csrfPreventionSaltCache.clear();
		httpSessionEvent.getSession().removeAttribute("csrfPreventionSaltCache");
		CurrentUsers.removeSessionFromList(httpSessionEvent.getSession());
	}
}
