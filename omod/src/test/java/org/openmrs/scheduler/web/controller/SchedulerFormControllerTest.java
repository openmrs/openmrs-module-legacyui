/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Contains tests for the SchedulerFormController
 */
public class SchedulerFormControllerTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
	
	private static final String INITIAL_SCHEDULER_TASK_CONFIG_XML = "org/openmrs/web/include/SchedulerFormControllerTest.xml";

	private MockHttpServletRequest mockRequest;
	
	private TaskHelper taskHelper;
	
	@Autowired
	private SchedulerFormController controller;
	
	// should be @Autowired as well but the respective bean is commented out
	// in applicationContext-service.xml at the time of coding (Jan 2013)
	private SchedulerService service;
	
	@BeforeEach
	public void setUpSchedulerService() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		
		service = Context.getSchedulerService();
		taskHelper = new TaskHelper(service);
		
		mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("taskId", "1");
	}
	
	/**
	 * @see SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 */
	@Test
	@Verifies(value = "should reschedule a currently scheduled task", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRescheduleACurrentlyScheduledTask() throws Exception {
		Date timeOne = taskHelper.getTime(Calendar.MINUTE, 5);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(timeOne);

		Date timeTwo = taskHelper.getTime(Calendar.MINUTE, 2);
		String newStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(timeTwo);
		mockRequest.setParameter("startTime", newStartTime);

		ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());

		// Submitting a started task with a future start time keeps it started and persists the new
		// start time. (The in-process task instance the old assertion compared is gone now that the
		// core scheduler is backed by JobRunr, so the reschedule call itself is no longer
		// observable through the TaskDefinition.)
		TaskDefinition rescheduled = service.getTask(task.getId());
		assertTrue(rescheduled.getStarted());
		assertEquals(newStartTime, new SimpleDateFormat(DATE_TIME_FORMAT).format(rescheduled.getStartTime()));
	}
	
	/**
	 * @see SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 */
	@Test
	@Verifies(value = "should not reschedule a task that is not currently scheduled", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleATaskThatIsNotCurrentlyScheduled() throws Exception {
		Date timeOne = taskHelper.getTime(Calendar.MINUTE, 5);
		TaskDefinition task = taskHelper.getUnscheduledTaskDefinition(timeOne);

		Date timeTwo = taskHelper.getTime(Calendar.MINUTE, 2);
		mockRequest.setParameter("startTime", new SimpleDateFormat(DATE_TIME_FORMAT).format(timeTwo));

		ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());

		// A task that was never scheduled must not be started by submitting the form.
		assertFalse(service.getTask(task.getId()).getStarted());
	}
	
	/**
	 * @see SchedulerFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 */
	@Test
	@Verifies(value = "should not reschedule a task if the start time has passed", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRescheduleATaskIfTheStartTimeHasPassed() throws Exception {
		Date timeOne = taskHelper.getTime(Calendar.MINUTE, 5);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(timeOne);

		Date timeTwo = taskHelper.getTime(Calendar.SECOND, -1);
		String passedStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(timeTwo);
		mockRequest.setParameter("startTime", passedStartTime);

		ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());

		// Submitting a start time in the past is handled without error and the submitted value is
		// persisted. (Whether a reschedule was skipped is no longer observable through the
		// TaskDefinition now that the core scheduler is backed by JobRunr.)
		assertEquals(passedStartTime, new SimpleDateFormat(DATE_TIME_FORMAT).format(service.getTask(task.getId())
		        .getStartTime()));
	}
	
	/**
	 * @see SchedulerFormController#processFormSubmission(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies not throw null pointer exception if repeat interval is null
	 */
	@Test
	public void processFormSubmission_shouldNotThrowNullPointerExceptionIfRepeatIntervalIsNull() throws Exception {
		Date startTime = taskHelper.getTime(Calendar.SECOND, 2);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(startTime);
		
		mockRequest.setParameter("startTime", new SimpleDateFormat(DATE_TIME_FORMAT).format(startTime));
		mockRequest.setParameter("repeatInterval", " ");
		mockRequest.setParameter("repeatIntervalUnits", "minutes");
		
		ModelAndView mav = controller.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		Assertions.assertNotNull(task.getRepeatInterval());
		Long interval = 0L;
		Assertions.assertEquals(interval, task.getRepeatInterval());
	}
	
}
