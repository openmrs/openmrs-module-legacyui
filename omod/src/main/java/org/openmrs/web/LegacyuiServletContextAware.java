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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;

public class LegacyuiServletContextAware implements ServletContextAware {

	private static Log log = LogFactory.getLog(LegacyuiServletContextAware.class);
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		
		String basePath = servletContext.getRealPath("");
		
		try {
			//copy jsp
			File destDir = new File(basePath + "/WEB-INF".replace("/", File.separator));
			File srcDir = new File(basePath + "/WEB-INF/view/module/legacyui".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy error handler
			File destFile = new File(basePath + "/errorhandler.jsp".replace("/", File.separator));
			File srcFile = new File(basePath + "/WEB-INF/view/module/legacyui/errorhandler.jsp".replace("/", File.separator));
			FileUtils.copyFile(srcFile, destFile);
			
			//copy scripts
			destDir = new File(basePath + "/WEB-INF/view/scripts".replace("/", File.separator));
			srcDir = new File(basePath + "/WEB-INF/view/module/legacyui/resources/scripts".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy openmrs.js
			destFile = new File(basePath + "/openmrs.js".replace("/", File.separator));
			srcFile = new File(basePath + "/WEB-INF/view/module/legacyui/resources/scripts/openmrs.js".replace("/", File.separator));
			FileUtils.copyFile(srcFile, destFile);
			
			//copy images
			destDir = new File(basePath + "/images".replace("/", File.separator));
			srcDir = new File(basePath + "/WEB-INF/view/module/legacyui/resources/images".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy css
			destDir = new File(basePath);
			srcDir = new File(basePath + "/WEB-INF/view/module/legacyui/resources/css".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
		}
		catch(IOException ex) {
			log.error("Failed to copy legacy ui files", ex);
		}
	}
}
