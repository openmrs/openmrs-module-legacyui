/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.observation.handler;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.openmrs.api.context.Context;
import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;

import org.openmrs.obs.handler.MediaHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Extends functionality of {@link MediaHandler} for web specific views.
 * 
 * @since 1.12
 */
public class WebMediaHandler extends MediaHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.URI_VIEW, ComplexObsHandler.HTML_VIEW, };
	
	/**
	 * Default Constructor
	 */
	public WebMediaHandler() {
		super();
	}
	
	/**
	 * Returns the ComplexData for an Obs depending on the view. Currently supported views are
	 * listed in ComplexObsHandler.*_VIEW. <br>
	 * Currently the only implemented views are those implemented by ancestor plus the following:
	 * <ul>
	 * <li>{@link org.openmrs.web.WebConstants#HYPERLINK_VIEW}: a lightweight alternative to
	 * returning the ComplexData from the parent class since this does not require access to the
	 * service layer. Gives a link to the ComplexServlet for this obs
	 * <li>{@link org.openmrs.web.WebConstants#HTML_VIEW}: An html tag that will display this
	 * complex data. For this MediaHandler, its an html5 audio or video tag.
	 * </ul>
	 * 
	 * @see org.openmrs.obs.handler.MediaHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		if (ComplexObsHandler.URI_VIEW.equals(view)) {
			Locale locale = Context.getLocale();
			ComplexData cd = new ComplexData(obs.getValueAsString(locale), WebHandlerUtils.getHyperlink(obs,
			    ComplexObsHandler.RAW_VIEW));
			obs.setComplexData(cd);
			return obs;
		}
		
		if (ComplexObsHandler.HTML_VIEW.equals(view)) {
			String mediaTag = "";
			String mimeType = OpenmrsUtil.getFileMimeType(getComplexDataFile(obs));
			if (mimeType.contains("video")) {
				mediaTag = "<video controls>" + "<source src=\""
				        + WebHandlerUtils.getHyperlink(obs, ComplexObsHandler.RAW_VIEW) + "\" type=\"" + mimeType
				        + "\"></video>";
			} else {
				mediaTag = "<audio controls>" + "<source src=\""
				        + WebHandlerUtils.getHyperlink(obs, ComplexObsHandler.RAW_VIEW) + "\" type=\"" + mimeType
				        + "\"></audio>";
			}
			Locale locale = Context.getLocale();
			ComplexData cd = new ComplexData(obs.getValueAsString(locale), mediaTag);
			obs.setComplexData(cd);
			return obs;
		}
		
		return super.getObs(obs, view);
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getSupportedViews()
	 */
	@Override
	public String[] getSupportedViews() {
		List view_list = new ArrayList(Arrays.asList(supportedViews));
		view_list.addAll(Arrays.asList(super.getSupportedViews()));
		String[] views = new String[view_list.size()];
		view_list.toArray(views);
		return views;
	}
	
}
