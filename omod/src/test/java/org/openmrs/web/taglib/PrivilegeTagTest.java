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

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockPageContext;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;

import javax.servlet.jsp.tagext.Tag;

/**
 * Tests for the {@link PrivilegeTag} taglib controller.
 */
public class PrivilegeTagTest extends BaseModuleWebContextSensitiveTest {

	/**
	 * @verifies include body for user with the privilege
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithThePrivilege() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("dataclerk1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients");

		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));

		Context.logout();
	}

	/**
	 * @verifies skip body for user without the privilege
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithoutThePrivilege() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Manage Patients");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies skip body for user with the privilege if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithThePrivilegeIfInverseIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("dataclerk1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients");
		tag.setInverse("true");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies include body for user with any of the privileges
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithAnyOfThePrivileges() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");

		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));

		Context.logout();
	}

	/**
	 * @verifies skip body for user with any of the privileges if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithAnyOfThePrivilegesIfInverseIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setInverse("true");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies include body for user with all of the privileges if hasAll is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithAllOfThePrivilegesIfHasAllIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("dataclerk1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setHasAll("true");

		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));

		Context.logout();
	}

	/**
	 * @verifies skip body for user with not all of the privileges if hasAll is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithNotAllOfThePrivilegesIfHasAllIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setHasAll("true");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies skip body for user with all of the privileges if hasAll is true and inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithAllOfThePrivilegesIfHasAllIsTrueAndInverseIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("dataclerk1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("View Patients,Edit Patients,Manage Patients");
		tag.setHasAll("true");
		tag.setInverse("true");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies include body for user without the privilege if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithoutThePrivilegeIfInverseIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Manage Patients");
		tag.setInverse("true");

		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));

		Context.logout();
	}

	/**
	 * @verifies skip body for user without any of the privileges
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithoutAnyOfThePrivileges() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies include body for user without any of the privileges if inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithoutAnyOfThePrivilegesIfInverseIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		tag.setInverse("true");

		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));

		Context.logout();
	}

	/**
	 * @verifies skip body for user without any of the privileges if hasAll is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldSkipBodyForUserWithoutAnyOfThePrivilegesIfHasAllIsTrue() throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		tag.setHasAll("true");

		assertThat(tag.doStartTag(), is(Tag.SKIP_BODY));

		Context.logout();
	}

	/**
	 * @verifies include body for user without any of the privileges if hasAll is true and inverse is true
	 * @see PrivilegeTag#doStartTag()
	 */
	@Test
	@SkipBaseSetup
	public void doStartTag_shouldIncludeBodyForUserWithoutAnyOfThePrivilegesIfHasAllIsTrueAndInverseIsTrue()
			throws Exception {

		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/PrivilegeTagTest.xml");
		Context.authenticate("clinician1", "test");

		PrivilegeTag tag = new PrivilegeTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Edit Patients,Manage Patients");
		tag.setHasAll("true");
		tag.setInverse("true");

		assertThat(tag.doStartTag(), is(Tag.EVAL_BODY_INCLUDE));

		Context.logout();
	}
}
