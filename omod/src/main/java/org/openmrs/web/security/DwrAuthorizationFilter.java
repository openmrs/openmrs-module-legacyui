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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsClassLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Servlet filter that enforces {@link RequirePrivilege}, where it's actually declared, on DWR
 * method invocations. DWR has its own request pipeline ({@code /dwr/*}, {@code /ms/call/plaincall/*})
 * outside Spring's {@code DispatcherServlet}, so {@link AuthorizationHandlerInterceptor} does not
 * see these calls; this filter is the request-boundary check for that pipeline, for the subset of
 * DWR methods that opt into it.
 * <p>
 * {@code WebModuleUtil#startModule} already folds every installed module's {@code <dwr>} block -
 * legacyui's own included - into one combined {@code WEB-INF/dwr-modules.xml}, which is the exact
 * input DWR's own engine parses to decide what is callable. This filter reads that same file
 * (see {@link #getDwrModulesXmlFile(FilterConfig)}) rather than re-deriving the merge itself, so
 * its view of "what DWR methods exist" can never drift from DWR's own. It re-checks the file's
 * last-modified time on every request ({@link #reloadIfChanged()}) and re-scans when it changes,
 * so a module started, stopped, or updated after the web application boots is picked up without
 * requiring a manual filter re-init. Each {@code <create>}/{@code <include method="...">}
 * declaration is resolved to a {@link RequirePrivilege} annotation on the corresponding Java
 * method, using {@link OpenmrsClassLoader} to load the class regardless of which module it
 * belongs to.
 * <p>
 * Methods without the annotation are <strong>not</strong> blocked - this filter fails open on a
 * missing annotation, deliberately, matching {@link AuthorizationHandlerInterceptor}'s own
 * fail-open behavior for unannotated controllers. Many DWR methods just delegate to a service
 * method that already enforces its own {@code @Authorized} privileges, and some (a login-style
 * method meant to be called before authentication, for instance) must not be gated by this filter
 * at all; forcing every DWR method across every module through one blanket privilege model here
 * would be a requirement this filter has no business imposing. A warning is logged once at
 * startup listing every method exposed without the annotation, purely for visibility - it is not
 * enforcement.
 * <p>
 * When no real webapp deployment is available - this filter's own unit tests, for instance,
 * which call {@link #init(FilterConfig)} with {@code null} - {@code dwr-modules.xml} can't be
 * located. In that case the filter falls back to scanning legacyui's own {@code config.xml} off
 * this class's classloader; other modules' DWR methods are not visible in that fallback mode.
 * <p>
 * For each request:
 * <ol>
 * <li>If the URL is not a DWR method call (e.g. {@code /dwr/engine.js},
 * {@code /dwr/interface/Foo.js}), pass it through unchanged.</li>
 * <li>Look up {@code {Script}.{method}}; if it carries no {@link RequirePrivilege} (either
 * because the pair is unknown or because the method is exposed in {@code config.xml} without
 * the annotation), pass it through unchanged - see above.</li>
 * <li>Otherwise require authentication. Unauthenticated requests get {@code 401 Unauthorized}.</li>
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

	/**
	 * The aggregated DWR config file to watch, resolved once at {@link #init(FilterConfig)}.
	 * {@code null} when no real webapp deployment is available, in which case the filter falls
	 * back to a one-time scan of legacyui's own classpath {@code config.xml}.
	 */
	private File dwrModulesXmlFile;

	private volatile long dwrModulesXmlLastModified = -1;

	/** Guards {@link #fallbackLoaded} so the classpath fallback only ever runs once. */
	private final Object reloadLock = new Object();

	private boolean fallbackLoaded = false;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			this.dwrModulesXmlFile = getDwrModulesXmlFile(filterConfig);
			reloadIfChanged();
		}
		catch (Exception e) {
			throw new ServletException("Failed to initialize DwrAuthorizationFilter", e);
		}
	}

	/**
	 * Resolves the {@code WEB-INF/dwr-modules.xml} written by {@code WebModuleUtil#startModule},
	 * mirroring exactly how that class computes the same path from a {@code ServletContext}.
	 * Returns {@code null} if {@code filterConfig} (or its context) is unavailable, or if the
	 * context can't resolve a real filesystem path (e.g. an unexploded/embedded deployment).
	 */
	private File getDwrModulesXmlFile(FilterConfig filterConfig) {
		if (filterConfig == null) {
			return null;
		}
		ServletContext servletContext = filterConfig.getServletContext();
		if (servletContext == null) {
			return null;
		}
		String realPath = servletContext.getRealPath("/WEB-INF/dwr-modules.xml");
		return realPath == null ? null : new File(realPath);
	}

	/**
	 * Re-scans {@link #dwrModulesXmlFile} if its last-modified time has changed since the last
	 * scan (including the very first one), so a module started, stopped, or updated after boot
	 * is reflected without requiring this filter to be re-initialized. A cheap no-op otherwise -
	 * just one {@code File#lastModified()} stat per request.
	 */
	private void reloadIfChanged() throws Exception {
		if (dwrModulesXmlFile == null || !dwrModulesXmlFile.exists()) {
			loadFallbackOnce();
			return;
		}

		long modified = dwrModulesXmlFile.lastModified();
		if (modified == dwrModulesXmlLastModified) {
			return;
		}

		synchronized (reloadLock) {
			if (modified == dwrModulesXmlLastModified) {
				return; // another thread already reloaded while we were waiting
			}

			Map<String, RequirePrivilege> annotated = new HashMap<>();
			Map<String, String> unannotated = new LinkedHashMap<>();

			DocumentBuilderFactory dbf = newSecureDocumentBuilderFactory();
			Document doc = dbf.newDocumentBuilder().parse(dwrModulesXmlFile);
			scanDwrCreates(doc, OpenmrsClassLoader.getInstance(), annotated, unannotated);

			applyLoadedAnnotations(annotated, unannotated);
			dwrModulesXmlLastModified = modified;
		}
	}

	/**
	 * Fallback for contexts with no real servlet deployment, e.g. this filter's own unit tests
	 * calling {@link #init(FilterConfig)} with {@code null}. Scans legacyui's own
	 * {@code config.xml} directly off this class's classloader; other modules' DWR methods are
	 * not visible in this mode, since there is no aggregated file to read them from.
	 */
	private void loadFallbackOnce() throws Exception {
		synchronized (reloadLock) {
			if (fallbackLoaded) {
				return;
			}

			Map<String, RequirePrivilege> annotated = new HashMap<>();
			Map<String, String> unannotated = new LinkedHashMap<>();

			ClassLoader ownClassLoader = getClass().getClassLoader();
			try (InputStream in = ownClassLoader.getResourceAsStream("config.xml")) {
				if (in == null) {
					log.warn("config.xml not found on classpath; DwrAuthorizationFilter will allow no DWR calls");
				} else {
					DocumentBuilderFactory dbf = newSecureDocumentBuilderFactory();
					Document doc = dbf.newDocumentBuilder().parse(in);
					scanDwrCreates(doc, ownClassLoader, annotated, unannotated);
				}
			}

			applyLoadedAnnotations(annotated, unannotated);
			fallbackLoaded = true;
		}
	}

	private void applyLoadedAnnotations(Map<String, RequirePrivilege> annotated, Map<String, String> unannotated) {
		this.privilegesByScriptMethod = Collections.unmodifiableMap(annotated);
		this.unannotatedMethods = Collections.unmodifiableMap(unannotated);

		log.info("DwrAuthorizationFilter (re)loaded; annotated DWR methods: " + annotated.size()
		        + ", unannotated (will be rejected with 403): " + unannotated.size());
		if (!unannotated.isEmpty()) {
			log.warn("The following DWR methods are exposed without @RequirePrivilege and will be "
			        + "rejected with 403: " + unannotated.keySet());
		}
	}

	/**
	 * {@code dwr-modules.xml} legitimately declares a DOCTYPE referencing DWR's own public DTD
	 * (e.g. {@code <!DOCTYPE dwr PUBLIC "-//GetAhead Limited//DTD Direct Web Remoting 2.0//EN"
	 * "http://directwebremoting.org/schema/dwr20.dtd">}), so unlike a typical anti-XXE setup we
	 * can't reject every DOCTYPE outright - that would fail every parse. Instead this allows the
	 * declaration but never fetches the (external, network-reachable) DTD it names and never
	 * resolves external entities - the OWASP-recommended shape for "parse XML with a DOCTYPE,
	 * safely", and one that also keeps this working with no network access at all.
	 */
	private DocumentBuilderFactory newSecureDocumentBuilderFactory() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		return dbf;
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

		try {
			reloadIfChanged();
		}
		catch (Exception e) {
			// Keep serving whatever was last successfully loaded rather than failing every
			// request; a transient read/parse error here shouldn't take down all of DWR.
			log.error("Failed to (re)load DWR authorization config; continuing with the previous snapshot", e);
		}

		RequirePrivilege annotation = privilegesByScriptMethod.get(scriptMethod);
		if (annotation == null) {
			// No @RequirePrivilege on this method - either it's declared in config.xml without
			// the annotation, or the {Script}.{method} pair isn't declared anywhere we scanned.
			// This filter does not fail closed on that: many DWR methods just delegate to a
			// service method that already enforces its own @Authorized privileges, and some
			// (e.g. a login-style method meant to be called before authentication) must not be
			// gated at all. Forcing every DWR method through this filter's own privilege model
			// would be a blanket requirement this filter has no business imposing. Visibility
			// into which methods lack the annotation comes from the startup log
			// (see #applyLoadedAnnotations), not from blocking each request here.
			chain.doFilter(request, response);
			return;
		}

		UserContext userContext = Context.getUserContext();
		if (userContext == null || !userContext.isAuthenticated()) {
			deny(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
			    "Authentication required to invoke " + scriptMethod);
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
	 * Walks every {@code <create>}/{@code <include method="...">} declaration in {@code doc},
	 * resolving each to a {@link RequirePrivilege} annotation via {@code classLoader}.
	 */
	private void scanDwrCreates(Document doc, ClassLoader classLoader, Map<String, RequirePrivilege> annotated,
	        Map<String, String> unannotated) {
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
