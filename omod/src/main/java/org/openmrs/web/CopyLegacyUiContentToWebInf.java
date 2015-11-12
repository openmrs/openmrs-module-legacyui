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
			toIgnore.add(new File(basePath + "/index.jsp".replace("/", File.separator)));
			
			String[] jspsToCopy = { "errorhandler", "memoryUsage" };
			//copy these files to root of the webapp
			for (String jsp : jspsToCopy) {
				File dest = new File(basePath + "/" + jsp + ".jsp".replace("/", File.separator));
				File src = new File(basePath + MODULE_ROOT_DIR + "/" + jsp + ".jsp".replace("/", File.separator));
				FileUtils.copyFile(src, dest);
				toIgnore.add(src);
			}
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
			File destFile = new File(basePath + "/openmrs.js".replace("/", File.separator));
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
			
			//Later we need to ignore everything in resources folder
			toIgnore.add(new File(basePath + MODULE_ROOT_DIR + "/resources".replace("/", File.separator)));
			
			//copy tags
			destDir = new File(basePath + "/WEB-INF/tags".replace("/", File.separator));
			srcDir = new File(basePath + "/WEB-INF/tags/module/legacyui".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy these directories to WEB-INF
			String[] directoriesToCopy = { "taglibs", "template" };
			for (String dir : directoriesToCopy) {
				File dest = new File(basePath + "/WEB-INF/" + dir.replace("/", File.separator));
				File src = new File(basePath + MODULE_ROOT_DIR + "/" + dir.replace("/", File.separator));
				FileUtils.copyDirectory(src, dest);
				toIgnore.add(src);
			}
			
			//copy all other un copied folders to WEB-INF/view
			destDir = new File(basePath + "/WEB-INF/view".replace("/", File.separator));
			srcDir = new File(basePath + MODULE_ROOT_DIR.replace("/", File.separator));
			final File src = srcDir;
			FileUtils.copyDirectory(src, destDir, toCopy -> !toIgnore.contains(toCopy)
			        && (toCopy.isDirectory() || !toCopy.getParentFile().equals(src)));
		}
		catch (IOException ex) {
			log.error("Failed to copy legacy ui files", ex);
		}
	}
}
