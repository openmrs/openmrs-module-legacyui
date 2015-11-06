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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;

public class CopyLegacyUiContentToWebInf implements ServletContextAware {
	
	private static Log log = LogFactory.getLog(CopyLegacyUiContentToWebInf.class);
	
	private static final String MODULE_ROOT_DIR = "/WEB-INF/view/module/legacyui";
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		
		String basePath = servletContext.getRealPath("");
		
		try {
			List<File> toIgnore = new ArrayList<>();
			//copy error handler
			File destFile = new File(basePath + "/errorhandler.jsp".replace("/", File.separator));
			File errorSrcFile = new File(basePath + MODULE_ROOT_DIR + "/errorhandler.jsp".replace("/", File.separator));
			FileUtils.copyFile(errorSrcFile, destFile);
			toIgnore.add(errorSrcFile);
			
			destFile = new File(basePath + "/index.jsp".replace("/", File.separator));
			File indexSrcFile = new File(basePath + MODULE_ROOT_DIR + "/index.jsp".replace("/", File.separator));
			FileUtils.copyFile(indexSrcFile, destFile);
			toIgnore.add(indexSrcFile);
			
			destFile = new File(basePath + "/memoryUsage.jsp".replace("/", File.separator));
			File memoryUsageSrcFile = new File(basePath + MODULE_ROOT_DIR + "/memoryUsage.jsp".replace("/", File.separator));
			FileUtils.copyFile(memoryUsageSrcFile, destFile);
			toIgnore.add(memoryUsageSrcFile);
			
			//Copy only the jsps under webapp to /WEB-INF/view
			File destDir = new File(basePath + "/WEB-INF/view".replace("/", File.separator));
			File srcDir = new File(basePath + MODULE_ROOT_DIR.replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir,
			    toCopy -> toCopy.getName().endsWith(".jsp") && !toIgnore.contains(toCopy));
			
			//copy scripts
			destDir = new File(basePath + "/WEB-INF/view/scripts".replace("/", File.separator));
			srcDir = new File(basePath + MODULE_ROOT_DIR + "/resources/scripts".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy openmrs.js
			destFile = new File(basePath + "/openmrs.js".replace("/", File.separator));
			File srcFile = new File(basePath + MODULE_ROOT_DIR
			        + "/resources/scripts/openmrs.js".replace("/", File.separator));
			FileUtils.copyFile(srcFile, destFile);
			
			//copy images
			destDir = new File(basePath + "/images".replace("/", File.separator));
			srcDir = new File(basePath + MODULE_ROOT_DIR + "/resources/images".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy css
			destDir = new File(basePath);
			srcDir = new File(basePath + MODULE_ROOT_DIR + "/resources/css".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy the directories except for the files(JSPs) in the src
			destDir = new File(basePath + "/WEB-INF".replace("/", File.separator));
			srcDir = new File(basePath + MODULE_ROOT_DIR.replace("/", File.separator));
			final File src = srcDir;
			FileUtils.copyDirectory(src, destDir, toCopy -> toCopy.isDirectory() || !toCopy.getParentFile().equals(src));
		}
		catch (IOException ex) {
			log.error("Failed to copy legacy ui files", ex);
		}
	}
}
