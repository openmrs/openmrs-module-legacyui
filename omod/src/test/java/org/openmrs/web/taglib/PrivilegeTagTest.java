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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockPageContext;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;

import jakarta.servlet.jsp.tagext.Tag;

import java.sql.SQLException;

/**
 * Tests for the {@link PrivilegeTag} taglib controller.
 */
public class PrivilegeTagTest extends BaseModuleWebContextSensitiveTest {
	
	@BeforeEach
	public void setUpUsersAndRoles() throws SQLException {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		// RolePrivilegeCache loads roles on a daemon thread with its own session, which can't reliably
		// see this test's uncommitted rows, so commit them (tearDownUsersAndRoles deletes them again).
		getConnection().commit();
	}
	
	@AfterEach
	public void tearDownUsersAndRoles() {
		Context.logout();
		deleteAllData();
	}
	
	/**
	 * @verifies include body for user with the privilege
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithThePrivilege() {
		
		Context.authenticate("dataclerk1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients");
		
		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));
	}
	
	/**
	 * @verifies skip body for user without the privilege
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithoutThePrivilege() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Manage Patients");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies skip body for user with the privilege if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithThePrivilegeIfInverseIsTrue() {
		
		Context.authenticate("dataclerk1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients");
		tag.setInverse("true");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies include body for user with any of the privileges
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithAnyOfThePrivileges() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		
		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));
	}
	
	/**
	 * @verifies skip body for user with any of the privileges if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithAnyOfThePrivilegesIfInverseIsTrue() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setInverse("true");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies include body for user with all of the privileges if hasAll is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithAllOfThePrivilegesIfHasAllIsTrue() {
		
		Context.authenticate("dataclerk1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setHasAll("true");
		
		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));
	}
	
	/**
	 * @verifies skip body for user with not all of the privileges if hasAll is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithNotAllOfThePrivilegesIfHasAllIsTrue() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setHasAll("true");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies skip body for user with all of the privileges if hasAll is true and inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithAllOfThePrivilegesIfHasAllIsTrueAndInverseIsTrue() {
		
		Context.authenticate("dataclerk1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setHasAll("true");
		tag.setInverse("true");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies include body for user without the privilege if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithoutThePrivilegeIfInverseIsTrue() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Manage Patients");
		tag.setInverse("true");
		
		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));
	}
	
	/**
	 * @verifies skip body for user without any of the privileges
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithoutAnyOfThePrivileges() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies include body for user without any of the privileges if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithoutAnyOfThePrivilegesIfInverseIsTrue() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		tag.setInverse("true");
		
		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));
	}
	
	/**
	 * @verifies skip body for user without any of the privileges if hasAll is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithoutAnyOfThePrivilegesIfHasAllIsTrue() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		tag.setHasAll("true");
		
		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));
	}
	
	/**
	 * @verifies include body for user without any of the privileges if hasAll is true and inverse
	 *           is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithoutAnyOfThePrivilegesIfHasAllIsTrueAndInverseIsTrue() {
		
		Context.authenticate("clinician1", "test");
		
		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		tag.setHasAll("true");
		tag.setInverse("true");
		
		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));
	}
}
