/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.maintenance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests the {@link SearchIndexController} controller
 */
public class SearchIndexControllerTest extends BaseModuleWebContextSensitiveTest {

    private SearchIndexController controller;

    @Mock
    private ContextDAO contextDao;

    @Mock
    private UserContext userContext;

    @Before
    public void before() {
        controller = new SearchIndexController();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * @verifies return the search index view
     * @see SearchIndexController#showPage()
     */
    @Test
    public void showPage_shouldReturnTheSearchIndexView() throws Exception {
        String viewName = controller.showPage();
        assertEquals("/module/legacyui/admin/maintenance/searchIndex", viewName);
    }

    /**
     * @verifies return true for success if the update does not fail and authenticated user makes call
     * @see SearchIndexController#rebuildSearchIndex()
     */
    @Test
    public void rebuildSearchIndex_shouldReturnTrueForSuccessIfTheUpdateDoesNotFail() throws Exception {
        // this test depends on being a authenticated user
        when(userContext.isAuthenticated()).thenReturn(true);
        assertTrue(Context.getUserContext().isAuthenticated());

        Mockito.when(contextDao.updateSearchIndexAsync()).thenReturn(null);
        Map<String, Object> response = controller.rebuildSearchIndex();
        assertEquals(true, response.get("success"));
    }

    /**
     * @verifies return false for success if a RuntimeException is thrown
     * @see SearchIndexController#rebuildSearchIndex()
     */
    @Test
    public void rebuildSearchIndex_shouldReturnFalseForSuccessIfARuntimeExceptionIsThrown() throws Exception {
        // this test depends on being a authenticated user
        when(userContext.isAuthenticated()).thenReturn(true);
        assertTrue(Context.getUserContext().isAuthenticated());

        Mockito.doThrow(new RuntimeException("boom")).when(contextDao).updateSearchIndexAsync();
        Map<String, Object> response = controller.rebuildSearchIndex();
        assertEquals(false, response.get("success"));
    }

    /**
     * @verifies return false for success if a un-authenticated user makes call
     * @see SearchIndexController#rebuildSearchIndex()
     */
    @Test
    public void rebuildSearchIndex_shouldReturnFalseForSuccessIfAUnAuthenticatedUserMakesCall() throws Exception {
        // this test depends on not being a authenticated user
        when(userContext.isAuthenticated()).thenReturn(false);
        assertFalse(Context.getUserContext().isAuthenticated());

        Mockito.when(contextDao.updateSearchIndexAsync()).thenReturn(null);
        Map<String, Object> response = controller.rebuildSearchIndex();
        assertEquals(false, response.get("success"));
    }

    /**
     * @verifies return inProgress for status if a rebuildSearchIndex is not completed
     * @see SearchIndexController#getStatus()
     */
    @Test
    public void getStatus_shouldReturnInProgressForStatusIfRebuildSearchIndexIsInProgress() throws Exception {
        Mockito.when(contextDao.updateSearchIndexAsync()).thenAnswer((Answer<Future>) invocationOnMock -> {
            Future<String> future = mock(FutureTask.class);
            when(future.isDone()).thenReturn(false);
            return future;
        });
        controller.rebuildSearchIndex();
        Map<String, String> response = controller.getStatus();
        assertEquals("inProgress", response.get("status"));
    }

    /**
     * @verifies return success for status if a rebuildSearchIndex is completed successfully
     * @see SearchIndexController#getStatus()
     */
    @Test
    public void getStatus_shouldReturnSuccessForStatusIfRebuildSearchIndexIsCompletedSuccessfully() throws Exception {
        Mockito.when(contextDao.updateSearchIndexAsync()).thenAnswer((Answer<Future>) invocationOnMock -> {
            Future<String> future = mock(FutureTask.class);
            when(future.isDone()).thenReturn(true);
            when(future.isCancelled()).thenReturn(false);
            return future;
        });
        controller.rebuildSearchIndex();
        Map<String, String> response = controller.getStatus();
        assertEquals("success", response.get("status"));
    }

    /**
     * @verifies return error for status if a rebuildSearchIndex is not completed normally
     * @see SearchIndexController#getStatus()
     */
    @Test
    public void getStatus_shouldReturnErrorForStatusIfRebuildSearchIndexIsCompletedUnsuccessfully() throws Exception {
        Mockito.when(contextDao.updateSearchIndexAsync()).thenAnswer((Answer<Future>) invocationOnMock -> {
            Future<String> future = mock(FutureTask.class);
            when(future.isDone()).thenReturn(true);
            when(future.isCancelled()).thenReturn(true);
            return future;
        });
        controller.rebuildSearchIndex();
        Map<String, String> response = controller.getStatus();
        assertEquals("error", response.get("status"));
    }

    /**
     * @verifies throws API exception if getStatus called before rebuildSearchIndex
     * @see SearchIndexController#getStatus()
     */
    @Test
    public void getStatus_shouldThrowApiExceptionWhenRebuildSearchIndexNotHaveBeenCalledBefore() throws Exception {
        expectedException.expect(APIException.class);
        expectedException.expectMessage("There was a problem rebuilding the search index");
        controller.getStatus();
    }
}
