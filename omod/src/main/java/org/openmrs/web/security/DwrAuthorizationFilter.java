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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Servlet filter that enforces {@link RequirePrivilege} on every DWR method invocation served by
 * the legacyui module. DWR has its own request pipeline ({@code /dwr/*},
 * {@code /ms/call/plaincall/*}) outside Spring's {@code DispatcherServlet}, so
 * {@link AuthorizationHandlerInterceptor} does not see these calls. This filter is the
 * request-boundary check for that pipeline.
 * <p>
 * At {@link #init(FilterConfig)} the filter parses {@code config.xml} from the classpath, walks
 * every {@code <create>}/{@code <include method="...">} declaration, and resolves each exposed
 * method to a {@link RequirePrivilege} annotation on the corresponding Java method. Methods
 * without an annotation are rejected at request time with {@code 403 Forbidden}; a warning is
 * logged at startup so any new DWR endpoint that ships without an annotation is visible.
 * <p>
 * For each request:
 * <ol>
 * <li>If the URL is not a DWR method call (e.g. {@code /dwr/engine.js},
 * {@code /dwr/interface/Foo.js}), pass it through unchanged.</li>
 * <li>If the URL is a method call, require authentication. Unauthenticated requests get
 * {@code 401 Unauthorized}.</li>
 * <li>Look up {@code {Script}.{method}}; if no annotation is registered (either because the
 * pair is unknown or because the method is exposed in {@code config.xml} without
 * {@link RequirePrivilege}), reject with {@code 403 Forbidden}.</li>
 * <li>Enforce the annotation's privilege list. Missing privileges yield {@code 403 Forbidden}.</li>
 * </ol>
 * <p>
 * The 401/403 responses are JSON, not the redirect-to-login flow the form-controller interceptor
 * uses, because DWR clients are AJAX callers that handle authorization failures locally rather
 * than following redirects.
 */
public class DwrAuthorizationFilter implements Filter {

	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Map keyed by {@code {scriptName}.{methodName}}. Value is the resolved
	 * {@link RequirePrivilege} for that method. Methods declared in {@code config.xml} but
	 * lacking an annotation are present in {@link #unannotatedMethods}; lookups fall back to
	 * authentication-only for those.
	 */
	private Map<String, RequirePrivilege> privilegesByScriptMethod = Collections.emptyMap();

	private Map<String, String> unannotatedMethods = Collections.emptyMap();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			Map<String, RequirePrivilege> annotated = new HashMap<>();
			Map<String, String> unannotated = new LinkedHashMap<>();
			loadDwrAnnotations(annotated, unannotated);
			this.privilegesByScriptMethod = Collections.unmodifiableMap(annotated);
			this.unannotatedMethods = Collections.unmodifiableMap(unannotated);

			log.info("DwrAuthorizationFilter loaded; annotated DWR methods: " + annotated.size()
			        + ", unannotated (will be rejected with 403): " + unannotated.size());
			if (!unannotated.isEmpty()) {
				log.warn("The following DWR methods are exposed in config.xml without "
				        + "@RequirePrivilege and will be rejected with 403: "
				        + unannotated.keySet());
			}
		}
		catch (Exception e) {
			throw new ServletException("Failed to initialize DwrAuthorizationFilter", e);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String scriptMethod = extractScriptMethod(httpRequest);
		if (scriptMethod == null) {
			// not a DWR method invocation (e.g. /dwr/engine.js, /dwr/interface/Foo.js); allow
			chain.doFilter(request, response);
			return;
		}

		UserContext userContext = Context.getUserContext();
		if (userContext == null || !userContext.isAuthenticated()) {
			deny(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
			    "Authentication required to invoke " + scriptMethod);
			return;
		}

		RequirePrivilege annotation = privilegesByScriptMethod.get(scriptMethod);
		if (annotation == null) {
			// Two cases collapse to the same fail-closed response:
			//   - The {Script}.{method} is declared in config.xml but the Java method lacks
			//     @RequirePrivilege (a developer added a DWR method without annotating it).
			//   - The {Script}.{method} pair is not declared in config.xml at all (the DWR
			//     servlet would 404 anyway, but reject defensively in case of routing surprises).
			if (unannotatedMethods.containsKey(scriptMethod)) {
				log.warn("DWR method " + scriptMethod
				        + " is declared in config.xml but has no @RequirePrivilege; rejecting");
			}
			deny(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			return;
		}

		if (!hasRequiredPrivileges(userContext, annotation)) {
			log.warn("User '" + userContext.getAuthenticatedUser() + "' attempted to invoke DWR method "
			        + scriptMethod + " without required privilege(s): " + String.join(",", annotation.value()));
			deny(httpResponse, HttpServletResponse.SC_FORBIDDEN,
			    "Insufficient privileges for " + scriptMethod);
			return;
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// no-op
	}

	/**
	 * Pulls {@code {Script}.{method}} out of a DWR call URL. DWR's plain-call grammar puts the
	 * script and method name immediately before the {@code .dwr} extension, with one of the
	 * transport names ({@code plaincall}, {@code plainjs}, {@code htmlfile}, ...) earlier in
	 * the path. Anything that doesn't match is a static asset or a JS-interface generation
	 * request and gets returned as {@code null} so the caller knows to skip enforcement.
	 */
	String extractScriptMethod(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null) {
			return null;
		}
		// strip query string
		int q = uri.indexOf('?');
		if (q >= 0) {
			uri = uri.substring(0, q);
		}
		// only DWR method calls end in .dwr
		if (!uri.endsWith(".dwr")) {
			return null;
		}
		// the path segment immediately before .dwr is "{Script}.{method}"
		int lastSlash = uri.lastIndexOf('/');
		if (lastSlash < 0 || lastSlash >= uri.length() - 1) {
			return null;
		}
		String segment = uri.substring(lastSlash + 1, uri.length() - ".dwr".length());
		int dot = segment.indexOf('.');
		if (dot <= 0 || dot >= segment.length() - 1) {
			return null;
		}
		return segment;
	}

	private boolean hasRequiredPrivileges(UserContext userContext, RequirePrivilege annotation) {
		String[] privileges = annotation.value();
		if (privileges.length == 0) {
			return true; // authentication-only annotation
		}
		if (annotation.requireAll()) {
			for (String privilege : privileges) {
				String trimmed = privilege.trim();
				if (!trimmed.isEmpty() && !userContext.hasPrivilege(trimmed)) {
					return false;
				}
			}
			return true;
		}
		for (String privilege : privileges) {
			String trimmed = privilege.trim();
			if (!trimmed.isEmpty() && userContext.hasPrivilege(trimmed)) {
				return true;
			}
		}
		return false;
	}

	private void deny(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
	}

	private String escapeJson(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	/**
	 * Parses {@code config.xml} from the legacyui classpath and resolves each declared DWR
	 * method to its {@link RequirePrivilege} annotation. Methods without the annotation are
	 * collected separately so the filter can warn at startup rather than fail outright while
	 * the rollout is in progress.
	 */
	private void loadDwrAnnotations(Map<String, RequirePrivilege> annotated, Map<String, String> unannotated)
	        throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		try (InputStream in = classLoader.getResourceAsStream("config.xml")) {
			if (in == null) {
				log.warn("config.xml not found on classpath; DwrAuthorizationFilter will allow all DWR calls");
				return;
			}
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(in);

			NodeList creates = doc.getElementsByTagName("create");
			for (int i = 0; i < creates.getLength(); i++) {
				Element create = (Element) creates.item(i);
				String script = create.getAttribute("javascript");
				if (script == null || script.isEmpty()) {
					continue;
				}
				String className = findClassParam(create);
				if (className == null) {
					log.warn("DWR script '" + script + "' has no <param name=\"class\"/>; skipping");
					continue;
				}
				Class<?> dwrClass;
				try {
					dwrClass = Class.forName(className, false, classLoader);
				}
				catch (ClassNotFoundException e) {
					log.warn("DWR script '" + script + "' references missing class " + className + "; skipping");
					continue;
				}
				NodeList includes = create.getElementsByTagName("include");
				for (int j = 0; j < includes.getLength(); j++) {
					Element include = (Element) includes.item(j);
					String methodName = include.getAttribute("method");
					if (methodName == null || methodName.isEmpty()) {
						continue;
					}
					String key = script + "." + methodName;
					RequirePrivilege annotation = findMethodAnnotation(dwrClass, methodName);
					if (annotation != null) {
						annotated.put(key, annotation);
					} else {
						unannotated.put(key, className + "#" + methodName);
					}
				}
			}
		}
	}

	private String findClassParam(Element create) {
		NodeList params = create.getElementsByTagName("param");
		for (int i = 0; i < params.getLength(); i++) {
			Node node = params.item(i);
			if (!(node instanceof Element)) {
				continue;
			}
			Element param = (Element) node;
			if ("class".equals(param.getAttribute("name"))) {
				String value = param.getAttribute("value");
				if (value != null && !value.isEmpty()) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the first method on {@code dwrClass} (or any superclass) whose name matches and
	 * returns its {@link RequirePrivilege}, if any. DWR resolves overloads by argument count
	 * at runtime; for our purposes we treat all overloads of the same name as sharing one
	 * privilege requirement, which matches every real-world legacyui DWR script.
	 */
	private RequirePrivilege findMethodAnnotation(Class<?> dwrClass, String methodName) {
		Class<?> cursor = dwrClass;
		while (cursor != null && cursor != Object.class) {
			for (Method method : cursor.getDeclaredMethods()) {
				if (method.getName().equals(methodName)) {
					RequirePrivilege annotation = method.getAnnotation(RequirePrivilege.class);
					if (annotation != null) {
						return annotation;
					}
				}
			}
			cursor = cursor.getSuperclass();
		}
		return null;
	}

	// visible for tests
	Map<String, RequirePrivilege> getPrivilegesByScriptMethod() {
		return privilegesByScriptMethod;
	}

	// visible for tests
	Map<String, String> getUnannotatedMethods() {
		return unannotatedMethods;
	}
}
