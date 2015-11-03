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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is only here for the registration of the dwr servlet.
 * If we can get the ServletContext from any where in the module, we
 * can move this registration to there and get rid of this class.
 */
public class DWRServletRegistration extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(DWRServletRegistration.class);

	public void init(ServletConfig config) throws ServletException {	
		ServletContext context = config.getServletContext();
		ServletRegistration reg = context.addServlet("dwr-invoker", OpenmrsDWRServlet.class);
		reg.addMapping("/dwr/*");
		reg.setInitParameter("debug", "false");
		reg.setInitParameter("config-modules", "/WEB-INF/dwr-modules.xml");
		
		copyLegacyFiles(config.getServletContext().getRealPath(""));
	}
	
	private void copyLegacyFiles(String path) {
		
		try {
			//copy jsp
			File destDir = new File(path + "/WEB-INF".replace("/", File.separator));
			File srcDir = new File(path + "/WEB-INF/view/module/legacyui".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy scripts
			destDir = new File(path + "/WEB-INF/view/scripts".replace("/", File.separator));
			srcDir = new File(path + "/WEB-INF/view/module/legacyui/resources/scripts".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy openmrs.js
			File destFile = new File(path + "/openmrs.js".replace("/", File.separator));
			File srcFile = new File(path + "/WEB-INF/view/module/legacyui/resources/scripts/openmrs.js".replace("/", File.separator));
			FileUtils.copyFile(srcFile, destFile);
			
			//copy images
			destDir = new File(path + "/images".replace("/", File.separator));
			srcDir = new File(path + "/WEB-INF/view/module/legacyui/resources/images".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
			
			//copy css
			destDir = new File(path);
			srcDir = new File(path + "/WEB-INF/view/module/legacyui/resources/css".replace("/", File.separator));
			FileUtils.copyDirectory(srcDir, destDir);
		}
		catch(IOException ex) {
			log.error("Failed to copy legacy ui files", ex);
		}
	}
}
