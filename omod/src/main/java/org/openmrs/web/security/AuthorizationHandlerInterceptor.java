/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.web.WebConstants;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring {@link HandlerInterceptor} that enforces {@link RequirePrivilege} declarations on handler
 * classes and methods before any controller code runs. The check happens in
 * {@link #preHandle(HttpServletRequest, HttpServletResponse, Object)}, which Spring invokes after
 * URL resolution but before {@code formBackingObject}, {@code @ModelAttribute} methods, or any
 * data binding. Rejecting the request here means no Hibernate entity is loaded for the attacker's
 * payload to bind onto, which closes the dirty-entity bypass that service-layer
 * {@code @Authorized} alone cannot defend against.
 * <p>
 * Behaviour mirrors {@code org.openmrs.web.taglib.RequireTag}:
 * <ul>
 * <li>An unauthenticated caller is redirected to {@code /login.htm} with the original URL stashed
 * in {@link WebConstants#OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR} so the post-login redirect
 * returns them to where they were going.</li>
 * <li>An authenticated caller missing the required privilege gets the same redirect, plus the
 * standard privilege-related session attributes ({@link WebConstants#INSUFFICIENT_PRIVILEGES},
 * {@link WebConstants#REQUIRED_PRIVILEGES}, {@link WebConstants#DENIED_PAGE}) so existing
 * downstream UX (eg. the access-denied page) keeps working unchanged.</li>
 * </ul>
 * <p>
 * Resolution rules for the annotation:
 * <ol>
 * <li>If the handler is a {@link HandlerMethod} (annotation-driven controller), look at the method
 * first, then fall back to the declaring class.</li>
 * <li>Otherwise (legacy {@code SimpleFormController}-style bean), look at the class.</li>
 * <li>If no annotation is present, the interceptor allows the request through (fail-open).
 * Annotations are being rolled out incrementally; unannotated handlers retain pre-rollout
 * behaviour.</li>
 * </ol>
 */
public class AuthorizationHandlerInterceptor implements HandlerInterceptor {

	private final Log log = LogFactory.getLog(getClass());

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

		RequirePrivilege annotation = resolveAnnotation(handler);
		if (annotation == null) {
			return true;
		}

		UserContext userContext = Context.getUserContext();
		if (userContext == null) {
			log.error("UserContext is null; OpenmrsFilter must run before this interceptor");
			return denyUnauthenticated(request, response);
		}

		if (!userContext.isAuthenticated()) {
			return denyUnauthenticated(request, response);
		}

		List<String> missing = missingPrivileges(userContext, annotation);
		if (missing.isEmpty()) {
			return true;
		}

		return denyInsufficientPrivilege(request, response, missing);
	}

	private RequirePrivilege resolveAnnotation(Object handler) {
		if (handler instanceof HandlerMethod) {
			HandlerMethod hm = (HandlerMethod) handler;
			RequirePrivilege onMethod = AnnotationUtils.findAnnotation(hm.getMethod(), RequirePrivilege.class);
			if (onMethod != null) {
				return onMethod;
			}
			return AnnotationUtils.findAnnotation(hm.getBeanType(), RequirePrivilege.class);
		}
		if (handler == null) {
			return null;
		}
		return AnnotationUtils.findAnnotation(handler.getClass(), RequirePrivilege.class);
	}

	private List<String> missingPrivileges(UserContext userContext, RequirePrivilege annotation) {
		String[] privileges = annotation.value();
		if (privileges.length == 0) {
			// the annotation requires authentication only; we already verified that
			return new ArrayList<>();
		}

		List<String> missing = new ArrayList<>();
		if (annotation.requireAll()) {
			for (String privilege : privileges) {
				String trimmed = privilege.trim();
				if (!trimmed.isEmpty() && !userContext.hasPrivilege(trimmed)) {
					missing.add(trimmed);
				}
			}
			return missing;
		}

		// any-of: pass if the user holds at least one
		for (String privilege : privileges) {
			String trimmed = privilege.trim();
			if (!trimmed.isEmpty() && userContext.hasPrivilege(trimmed)) {
				return new ArrayList<>();
			}
		}
		// none matched - report the full set as missing for the diagnostic page
		for (String privilege : privileges) {
			String trimmed = privilege.trim();
			if (!trimmed.isEmpty()) {
				missing.add(trimmed);
			}
		}
		return missing;
	}

	private boolean denyUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "require.login");
		stashLoginRedirect(request, session);
		response.sendRedirect(request.getContextPath() + "/login.htm");
		return false;
	}

	private boolean denyInsufficientPrivilege(HttpServletRequest request, HttpServletResponse response,
	        List<String> missing) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, true);
		session.setAttribute(WebConstants.REQUIRED_PRIVILEGES, String.join(",", missing));

		String referer = request.getHeader("Referer");
		if (referer != null && !referer.isEmpty()) {
			session.setAttribute(WebConstants.REFERER_URL, referer);
			session.setAttribute(WebConstants.DENIED_PAGE, referer);
		}

		log.warn("User '" + Context.getAuthenticatedUser() + "' attempted to access "
		        + request.getRequestURI() + " without required privilege(s): " + missing);

		stashLoginRedirect(request, session);
		response.sendRedirect(request.getContextPath() + "/login.htm");
		return false;
	}

	private void stashLoginRedirect(HttpServletRequest request, HttpSession session) {
		String url = request.getRequestURI();
		if (request.getQueryString() != null) {
			url = url + "?" + request.getQueryString();
		}
		session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, url);
	}

}
