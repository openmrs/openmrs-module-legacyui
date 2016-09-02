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

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

/**
 * Controller for the &lt;openmrs:hasPrivilege&gt; taglib used on jsp pages. This taglib restricts evaluation of enclosed
 * code to currently logged in (or anonymous) users that have the given privileges.
 * <p>
 * Example use case 1:
 *
 * Only include the "patientHeader" portlet into the page if the user DOES have ANY of
 * "View Patients", "Edit Patients" privileges.
 *
 * <pre>
 * &lt;openmrs:hasPrivilege privilege="View Patients,Edit Patients" /&gt;
 *   &lt;openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}" /&gt;
 * &lt;openmrs:hasPrivilege/&gt;
 * </pre>
 *
 *
 * Example use case 2:
 *
 * Only include the "patientHeader" portlet into the page if the user DOES NOT have ANY of
 * "View Patients", "Edit Patients" privileges.
 *
 * <pre>
 * &lt;openmrs:hasPrivilege privilege="View Patients,Edit Patients" inverse="true" /&gt;
 *   &lt;openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}" /&gt;
 * &lt;openmrs:hasPrivilege/&gt;
 * </pre>
 *
 * Example use case 3:
 *
 * Only include the "patientHeader" portlet into the page if the user DOES have ALL of
 * "View Patients", "Edit Patients" privileges.
 *
 * <pre>
 * &lt;openmrs:hasPrivilege privilege="View Patients,Edit Patients" hasAll="true" /&gt;
 *   &lt;openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}" /&gt;
 * &lt;openmrs:hasPrivilege/&gt;
 * </pre>
 * </p>
 */
public class PrivilegeTag extends TagSupport {

	public static final long serialVersionUID = 11233L;

	private final Log log = LogFactory.getLog(getClass());

	private String privilege;

	private String inverse;

	private String hasAll;

	/**
	 * Comma separated list in {@code privilege} is checked and the enclosing code is evaluated if the user has any of the
	 * set privileges.
	 * <p>
	 * Returns {@code EVAL_BODY_INCLUDE} if the user satisfies the privilege requirements and {@code SKIP_BODY}
	 * if the user doesn't.
	 * By default (meaning only {@code privilege} is set the user needs to have at least one of the set
	 * privileges. The comma acts as an OR.
	 * The comma acts as an AND if {@code hasAll} is set to "true"/"TRUE".
	 * The tags behavior on how the list of privileges is treated can be inversed by setting {@code inverse} to
	 * "true"/"TRUE".
	 * </p>
	 *
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 * @should include body for user with the privilege
	 * @should skip body for user without the privilege
	 * @should skip body for user with the privilege if inverse is true
	 * @should include body for user with any of the privileges
	 * @should skip body for user with any of the privileges if inverse is true
	 * @should include body for user with all of the privileges if hasAll is true
	 * @should skip body for user with not all of the privileges if hasAll is true
	 * @should skip body for user with all of the privileges if hasAll is true and inverse is true
	 * @should include body for user without the privilege if inverse is true
	 * @should skip body for user without any of the privileges
	 * @should include body for user without any of the privileges if inverse is true
	 * @should skip body for user without any of the privileges if hasAll is true
	 * @should include body for user without any of the privileges if hasAll is true and inverse is true
	 */
	public int doStartTag() {

		UserContext userContext = Context.getUserContext();

		log.debug("Checking user " + userContext.getAuthenticatedUser() + " for privs " + privilege);

		boolean isHasAllSet = Boolean.valueOf(hasAll);
		boolean hasSatisfiedPrivilegeCondition;
		if (isHasAllSet) {
			hasSatisfiedPrivilegeCondition = hasAllPrivileges(userContext);
		} else {
			hasSatisfiedPrivilegeCondition = hasThePrivilegeOrAnyPrivilege(userContext);
		}

		boolean isInverted = Boolean.valueOf(inverse);
		if ((hasSatisfiedPrivilegeCondition && !isInverted) || (!hasSatisfiedPrivilegeCondition && isInverted)) {
			pageContext.setAttribute("authenticatedUser", userContext.getAuthenticatedUser());
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

	/**
	 * Determines if the authenticated user has all of the privileges set in {@code privilege}.
	 *
	 * @param userContext the user context of the authenticated user to be checked for privileges
	 * @return true if user has all the privileges
	 */
	private boolean hasAllPrivileges(UserContext userContext) {

		boolean result;

		if (privilege.contains(",")) {
			String[] privs = privilege.split(",");
			for (String p : privs) {
				if (!userContext.hasPrivilege(p)) {
					return false;
				}
			}
			result = true;
		} else {
			result = userContext.hasPrivilege(privilege);
		}
		return result;
	}

	/**
	 * Determines if the authenticated user has the privilege or any of the privileges set in {@code privilege}.
	 *
	 * @param userContext the user context of the authenticated user to be checked for privileges
	 * @return true if user has the privilege or any of the privileges
	 */
	private boolean hasThePrivilegeOrAnyPrivilege(UserContext userContext) {

		boolean result = false;

		if (privilege.contains(",")) {
			String[] privs = privilege.split(",");
			for (String p : privs) {
				if (userContext.hasPrivilege(p)) {
					result = true;
					break;
				}
			}
		} else {
			result = userContext.hasPrivilege(privilege);
		}
		return result;
	}

	/**
	 * @return Returns the privilege.
	 */
	public String getPrivilege() {
		return privilege;
	}

	/**
	 * @param privilege The privilege to set.
	 */
	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	/**
	 * @return Returns the inverse.
	 */
	public String getInverse() {
		return inverse;
	}

	/**
	 * @param inverse The inverse to set.
	 */
	public void setInverse(String inverse) {
		this.inverse = inverse;
	}

	/**
	 * @return Returns the hasAll.
	 */
	public String getHasAll() {
		return hasAll;
	}

	/**
	 * @param hasAll The hasAll to set.
	 */
	public void setHasAll(String hasAll) {
		this.hasAll = hasAll;
	}
}
