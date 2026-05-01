/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.hl7;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Source;
import org.openmrs.web.test.WebTestHelper;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Verifies that {@link HL7SourceFormController} enforces authorization on POST
 * requests, not just on GET-time JSP rendering. The form JSP carries an
 * {@code <openmrs:require privilege="Update HL7 Source"/>} guard, but that tag
 * only fires when the JSP is rendered. A successful POST is dispatched through
 * {@code onSubmit} and returned via a {@code RedirectView}, so the JSP never
 * renders and the privilege check is bypassed unless something else enforces
 * it.
 */
public class HL7SourceFormControllerTest extends BaseModuleWebContextSensitiveTest {

	private static final String LIMITED_USER_DATASET = "org/openmrs/web/controller/hl7/include/HL7SourceFormControllerTest.xml";

	private static final Integer EXISTING_HL7_SOURCE_ID = 1;

	@Autowired
	@Qualifier("webTestHelper")
	private WebTestHelper webTestHelper;

	@Autowired
	@Qualifier("legacyUiUrlMapping")
	private HandlerMapping legacyUiUrlMapping;

	/**
	 * Dispatches the request through {@link #legacyUiUrlMapping} so that any
	 * interceptors registered on the mapping (in particular,
	 * {@code AuthorizationHandlerInterceptor}) actually run before the handler.
	 * {@link WebTestHelper#handle} resolves the handler but skips
	 * {@code preHandle}, which means it does not exercise the interceptor and
	 * cannot prove the authorization wiring is correct.
	 */
	private void dispatchThroughInterceptors(MockHttpServletRequest request) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		HandlerExecutionChain chain = legacyUiUrlMapping.getHandler(request);
		if (chain != null && chain.getInterceptorList() != null) {
			for (HandlerInterceptor interceptor : chain.getInterceptorList()) {
				if (!interceptor.preHandle(request, response, chain.getHandler())) {
					return; // interceptor stopped the request - do not invoke the handler
				}
			}
		}
		webTestHelper.handle(request);
	}

	/**
	 * A user with no HL7 privileges must not be able to mutate an HL7 source by
	 * POSTing to {@code /admin/hl7/hl7Source.form}. If this test fails, the
	 * vulnerability described in the report is exploitable on master.
	 */
	@Test
	public void onSubmit_shouldRejectUnprivilegedUserAttemptingToSaveHL7Source() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);

		HL7Source originalSource = Context.getHL7Service().getHL7Source(EXISTING_HL7_SOURCE_ID);
		assertNotNull(originalSource, "standardTestDataset should provide hl7_source #1");
		String originalName = originalSource.getName();
		String originalDescription = originalSource.getDescription();

		Context.logout();
		Context.authenticate("limiteduser", "test");

		MockHttpServletRequest post = webTestHelper.newPOST("/admin/hl7/hl7Source.form");
		post.addParameter("hl7SourceId", EXISTING_HL7_SOURCE_ID.toString());
		post.addParameter("name", "TAMPERED-by-unauthorized-user");
		post.addParameter("description", "TAMPERED-DESCRIPTION");
		post.addParameter("save", "Save");

		try {
			dispatchThroughInterceptors(post);
		}
		catch (Exception expected) {
			// any layer rejecting the request (controller / AOP / interceptor) is acceptable
			System.out.println(expected.getMessage());
		}

		Context.logout();
		Context.authenticate("admin", "test");
		Context.flushSession();
		Context.clearSession();

		HL7Source after = Context.getHL7Service().getHL7Source(EXISTING_HL7_SOURCE_ID);
		assertEquals(originalName, after.getName(),
		    "An authenticated user without 'Update HL7 Source' must not be able to modify an HL7 source via POST");
		assertEquals(originalDescription, after.getDescription(),
		    "An authenticated user without 'Update HL7 Source' must not be able to modify an HL7 source via POST");
	}

	/**
	 * Models the reporter's actual scenario: a user with read access to HL7
	 * sources ({@code Get HL7 Source}) but not write access ({@code Update HL7
	 * Source}). With this privilege set, {@code formBackingObject} succeeds
	 * (because {@code getHL7Source} only requires {@code Get HL7 Source}), so
	 * the dispatch reaches {@code onSubmit}; only the service-layer
	 * {@code @Authorized} on {@code saveHL7Source} stands between the attacker
	 * and the database write.
	 */
	@Test
	public void onSubmit_shouldRejectUserWithReadOnlyHL7PrivilegeAttemptingToSave() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);

		HL7Source originalSource = Context.getHL7Service().getHL7Source(EXISTING_HL7_SOURCE_ID);
		assertNotNull(originalSource, "standardTestDataset should provide hl7_source #1");
		final String originalName = originalSource.getName();

		Context.logout();
		Context.authenticate("hl7reader", "test");

		MockHttpServletRequest post = webTestHelper.newPOST("/admin/hl7/hl7Source.form");
		post.addParameter("hl7SourceId", EXISTING_HL7_SOURCE_ID.toString());
		post.addParameter("name", "TAMPERED-by-readonly-user");
		post.addParameter("description", "TAMPERED-DESCRIPTION");
		post.addParameter("save", "Save");

		try {
			dispatchThroughInterceptors(post);
		}
		catch (Exception expected) {
			System.out.println("[hl7reader save] " + expected.getClass().getSimpleName() + ": " + expected.getMessage());
		}

		Context.logout();
		Context.authenticate("admin", "test");

		// Read straight from the DB via raw SQL so we observe what is actually persisted,
		// not the still-attached (and possibly dirty) entity in the Hibernate L1 cache. We
		// deliberately do NOT call Context.flushSession() - that would force a flush
		// ourselves and mask whether the surrounding container would have flushed the
		// dirty entity on its own.
		java.util.List<java.util.List<Object>> rows = Context.getAdministrationService()
		    .executeSQL("select name from hl7_source where hl7_source_id = " + EXISTING_HL7_SOURCE_ID, true);
		assertEquals(1, rows.size(), "expected exactly one row");
		assertEquals(originalName, rows.get(0).get(0),
		    "A user with 'Get HL7 Source' but not 'Update HL7 Source' must not be able to persist a modified HL7 source via POST. "
		    + "If this fails, the dirty entity from the data binder was flushed despite the @Authorized exception.");
	}

	/**
	 * Drift-proof variant: same exploit attempt as
	 * {@link #onSubmit_shouldRejectUserWithReadOnlyHL7PrivilegeAttemptingToSave()}
	 * but the test explicitly calls {@link Context#flushSession()} after the
	 * dispatch, simulating any future (or already-deployed) configuration in
	 * which a dirty Hibernate session ends up flushed before request close.
	 * <p>
	 * Without {@link AuthorizationHandlerInterceptor} this fails: the data
	 * binder mutates the managed entity in the L1 cache, then our explicit
	 * flush pushes that change to the database. With the interceptor wired,
	 * {@code preHandle} rejects the request before {@code formBackingObject}
	 * runs, so no entity is loaded and there is nothing dirty to flush. This
	 * is the test that proves the interceptor closes the architectural
	 * defect, not just the current happy-path behaviour.
	 */
	@Test
	public void onSubmit_shouldRejectReadOnlyUserEvenWhenSessionIsExplicitlyFlushed() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);

		HL7Source originalSource = Context.getHL7Service().getHL7Source(EXISTING_HL7_SOURCE_ID);
		assertNotNull(originalSource, "standardTestDataset should provide hl7_source #1");
		final String originalName = originalSource.getName();

		Context.logout();
		Context.authenticate("hl7reader", "test");

		MockHttpServletRequest post = webTestHelper.newPOST("/admin/hl7/hl7Source.form");
		post.addParameter("hl7SourceId", EXISTING_HL7_SOURCE_ID.toString());
		post.addParameter("name", "TAMPERED-flush-bypass");
		post.addParameter("description", "TAMPERED-DESCRIPTION");
		post.addParameter("save", "Save");

		try {
			dispatchThroughInterceptors(post);
		}
		catch (Exception expected) {
			// the interceptor should reject before the handler ever runs
		}

		Context.logout();
		Context.authenticate("admin", "test");
		Context.flushSession();

		java.util.List<java.util.List<Object>> rows = Context.getAdministrationService()
		    .executeSQL("select name from hl7_source where hl7_source_id = " + EXISTING_HL7_SOURCE_ID, true);
		assertEquals(1, rows.size(), "expected exactly one row");
		assertEquals(originalName, rows.get(0).get(0),
		    "Even with an explicit Hibernate flush after the request, an unprivileged caller must not be "
		    + "able to mutate an HL7 source via POST. If this fails, the request reached formBackingObject "
		    + "and dirtied the managed entity - meaning AuthorizationHandlerInterceptor is not wired or "
		    + "did not match the URL.");
	}

	/**
	 * A user with no HL7 privileges must not be able to purge an HL7 source by
	 * POSTing the {@code purge} action.
	 */
	@Test
	public void onSubmit_shouldRejectUnprivilegedUserAttemptingToPurgeHL7Source() throws Exception {
		executeDataSet(LIMITED_USER_DATASET);

		HL7Source originalSource = Context.getHL7Service().getHL7Source(EXISTING_HL7_SOURCE_ID);
		assertNotNull(originalSource, "standardTestDataset should provide hl7_source #1");

		Context.logout();
		Context.authenticate("limiteduser", "test");

		MockHttpServletRequest post = webTestHelper.newPOST("/admin/hl7/hl7Source.form");
		post.addParameter("hl7SourceId", EXISTING_HL7_SOURCE_ID.toString());
		post.addParameter("name", originalSource.getName());
		post.addParameter("description", originalSource.getDescription());
		post.addParameter("purge", "Purge");

		try {
			dispatchThroughInterceptors(post);
		}
		catch (Exception expected) {
			// any layer rejecting the request (controller / AOP / interceptor) is acceptable
		}

		Context.logout();
		Context.authenticate("admin", "test");
		Context.flushSession();
		Context.clearSession();

		HL7Source after = Context.getHL7Service().getHL7Source(EXISTING_HL7_SOURCE_ID);
		assertNotNull(after,
		    "An authenticated user without 'Purge HL7 Source' must not be able to purge an HL7 source via POST");
	}
}
