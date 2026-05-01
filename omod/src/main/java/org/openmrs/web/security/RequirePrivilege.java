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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the OpenMRS privileges required to dispatch a web request to a controller method or to
 * invoke a DWR-exposed method. The annotation is the request-boundary counterpart to the
 * {@code <openmrs:require>} JSP tag and is enforced by
 * {@link AuthorizationHandlerInterceptor} for Spring controllers and by
 * {@code DwrAuthorizationFilter} for DWR endpoints. Both enforcers reject requests <em>before</em>
 * any controller code, view, or service method runs - in particular, before a Hibernate session
 * loads any entity, which closes the dirty-entity exploitation pattern that service-layer
 * {@code @Authorized} alone cannot defend against.
 * <p>
 * Placement:
 * <ul>
 * <li><strong>{@code @Controller}/{@code @RequestMapping} controllers</strong> &mdash; annotate
 * the handler method. Each method's annotation is independent.</li>
 * <li><strong>Legacy {@code SimpleFormController}-style controllers</strong> &mdash; annotate the
 * controller class. The annotation applies to every dispatch the controller receives. Use the
 * privilege the matching JSP's {@code <openmrs:require>} declares (typically the write
 * privilege).</li>
 * <li><strong>DWR-exposed methods</strong> &mdash; annotate the method on the DWR script class.
 * Methods exposed in {@code config.xml} that lack this annotation are rejected at startup by
 * {@code DwrAuthorizationFilter}.</li>
 * </ul>
 * <p>
 * Semantics mirror the JSP tag:
 * <ul>
 * <li>{@code value()} empty &mdash; the caller must be authenticated; no specific privilege is
 * required (matches {@code <openmrs:require>} with no privilege attribute).</li>
 * <li>{@code value()} non-empty, {@code requireAll() == false} &mdash; the caller must hold at
 * least one of the listed privileges (matches {@code anyPrivilege="..."}).</li>
 * <li>{@code value()} non-empty, {@code requireAll() == true} &mdash; the caller must hold every
 * listed privilege (matches {@code allPrivileges="..."}).</li>
 * </ul>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePrivilege {

	/**
	 * The privileges to check. An empty array means no specific privilege is required, only that
	 * the caller is authenticated.
	 */
	String[] value() default {};

	/**
	 * If {@code true}, the caller must hold every privilege in {@link #value()}. If {@code false}
	 * (default), the caller must hold at least one.
	 */
	boolean requireAll() default false;
}
