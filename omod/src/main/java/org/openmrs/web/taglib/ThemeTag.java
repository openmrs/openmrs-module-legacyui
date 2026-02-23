/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.openmrs.web.OpenmrsCookieThemeResolver;
import org.openmrs.web.theme.Theme;
import org.openmrs.web.theme.ThemeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Custom replacement for org.springframework.web.servlet.tags.ThemeTag which was removed in
 * Spring 6. Resolves theme message codes using the custom ThemeSource and OpenmrsCookieThemeResolver.
 */
public class ThemeTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(ThemeTag.class);
	
	private String code;
	
	private String text;
	
	private String var;
	
	private String scope;
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setVar(String var) {
		this.var = var;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@Override
	public int doEndTag() throws JspException {
		try {
			String message = resolveThemeMessage();
			
			if (var != null) {
				int scopeValue = getScopeValue();
				pageContext.setAttribute(var, message, scopeValue);
			} else {
				writeMessage(message);
			}
		}
		catch (IOException ex) {
			throw new JspTagException(ex.getMessage(), ex);
		}
		return EVAL_PAGE;
	}
	
	private String resolveThemeMessage() {
		WebApplicationContext wac = RequestContextUtils.findWebApplicationContext(
				(jakarta.servlet.http.HttpServletRequest) pageContext.getRequest(),
				pageContext.getServletContext());
		
		if (wac == null) {
			return getDefaultText();
		}
		
		try {
			// Get the theme resolver to determine current theme name
			OpenmrsCookieThemeResolver themeResolver = wac.getBean("themeResolver",
					OpenmrsCookieThemeResolver.class);
			String themeName = themeResolver.getDefaultThemeName();
			
			// Get the theme source to resolve the theme
			ThemeSource themeSource = wac.getBean("themeSource", ThemeSource.class);
			Theme theme = themeSource.getTheme(themeName);
			
			if (theme != null) {
				MessageSource messageSource = theme.getMessageSource();
				Locale locale = RequestContextUtils.getLocale(
						(jakarta.servlet.http.HttpServletRequest) pageContext.getRequest());
				try {
					return messageSource.getMessage(code, null, locale);
				}
				catch (NoSuchMessageException ex) {
					return getDefaultText();
				}
			}
		}
		catch (Exception ex) {
			log.debug("Failed to resolve theme message for code '{}': {}", code, ex.getMessage());
		}
		
		return getDefaultText();
	}
	
	private String getDefaultText() {
		return (text != null) ? text : code;
	}
	
	private void writeMessage(String message) throws IOException {
		if (message != null) {
			pageContext.getOut().write(message);
		}
	}
	
	private int getScopeValue() {
		if ("request".equals(scope)) {
			return jakarta.servlet.jsp.PageContext.REQUEST_SCOPE;
		} else if ("session".equals(scope)) {
			return jakarta.servlet.jsp.PageContext.SESSION_SCOPE;
		} else if ("application".equals(scope)) {
			return jakarta.servlet.jsp.PageContext.APPLICATION_SCOPE;
		}
		return jakarta.servlet.jsp.PageContext.PAGE_SCOPE;
	}
	
	@Override
	public void release() {
		super.release();
		code = null;
		text = null;
		var = null;
		scope = null;
	}
}
