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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;

/**
 * Verifies the authorization story for {@link DWRAdministrationService}. Both
 * methods are exposed in {@code config.xml} via DWR with no DwrFilter-level
 * authentication and no {@code Context.isAuthenticated()} guard inside the DWR
 * class itself; the only protection is whatever {@code @Authorized} annotations
 * the underlying core {@code AdministrationService} carries.
 */
public class DWRAdministrationServiceTest extends BaseModuleWebContextSensitiveTest {

	private final DWRAdministrationService dwr = new DWRAdministrationService();

	/**
	 * Reported issue: {@code DWRAdministrationService.getGlobalProperty} returns
	 * values to unauthenticated callers because (a) DwrFilter does not
	 * authenticate, (b) the DWR class does not check {@code isAuthenticated()},
	 * and (c) historically {@code AdministrationService.getGlobalProperty(String)}
	 * carried no {@code @Authorized} annotation.
	 *
	 * <p>On OpenMRS Platform 3.0.x the interface method now declares
	 * {@code @Authorized("Get Global Properties")}, so service-layer AOP
	 * rejects unauthenticated callers. The DWR layer itself still does no
	 * authentication, so this protection is entirely incidental: any future
	 * platform change that drops the annotation, or any DWR method that calls
	 * a non-{@code @Authorized} service method (see
	 * {@link DWRHL7ServiceTest}), is reachable by anonymous users.
	 */
	@Test
	public void getGlobalProperty_shouldNotReturnValueWhenCallerIsUnauthenticated() {
		Context.authenticate("admin", "test");
		assertEquals("en_GB", Context.getAdministrationService().getGlobalProperty("locale.allowed.list"));
		Context.logout();

		String value = null;
		try {
			value = dwr.getGlobalProperty("locale.allowed.list");
		}
		catch (APIAuthenticationException expected) {
			// service-layer @Authorized rejected it on Platform 3.0+
		}

		assertEquals(null, value,
		    "DWRAdministrationService.getGlobalProperty must not return a value to an unauthenticated caller. "
		    + "If this fails, the DWR endpoint is leaking configuration to anyone who can reach /dwr/.");
	}

	/**
	 * A logged-in user without {@code Manage Global Properties} must not be able
	 * to mutate a global property through the DWR call. The underlying
	 * {@code saveGlobalProperty} carries {@code @Authorized(MANAGE_GLOBAL_PROPERTIES)}
	 * but the DWR method constructs a fresh {@link org.openmrs.GlobalProperty}
	 * and calls save without first loading any managed entity, so this case is
	 * not subject to the dirty-entity flush issue we observed for HL7 sources.
	 * We assert the DB value is unchanged regardless.
	 */
	@Test
	public void setGlobalProperty_shouldRejectUserWithoutManageGlobalPropertiesPrivilege() throws Exception {
		Context.authenticate("admin", "test");
		String originalValue = Context.getAdministrationService().getGlobalProperty("locale.allowed.list");
		assertNotNull(originalValue);

		// reuse the limited-user fixture from the HL7 test
		executeDataSet("org/openmrs/web/controller/hl7/include/HL7SourceFormControllerTest.xml");

		Context.logout();
		Context.authenticate("limiteduser", "test"); // no privileges

		try {
			dwr.setGlobalProperty("locale.allowed.list", "TAMPERED-VIA-DWR");
		}
		catch (APIAuthenticationException expected) {
			// good — service-layer @Authorized rejected it
		}

		Context.logout();
		Context.authenticate("admin", "test");

		// read straight from the DB so we observe what is actually persisted
		List<List<Object>> rows = Context.getAdministrationService()
		    .executeSQL("select property_value from global_property where property = 'locale.allowed.list'", true);
		assertEquals(1, rows.size(), "expected exactly one global_property row");
		assertEquals(originalValue, rows.get(0).get(0),
		    "A user without 'Manage Global Properties' must not be able to mutate a GP via DWR");
	}
}
