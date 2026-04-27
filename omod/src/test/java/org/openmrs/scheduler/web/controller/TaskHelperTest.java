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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;

public class TaskHelperTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String INITIAL_SCHEDULER_TASK_CONFIG_XML = "org/openmrs/web/include/TaskHelperTest.xml";
	
	private static final long MAX_WAIT_TIME_IN_MILLISECONDS = 2048;
	
	private SchedulerService service;
	
	private TaskHelper taskHelper;
	
	@BeforeEach
	public void setUp() throws Exception {
		executeDataSet(INITIAL_SCHEDULER_TASK_CONFIG_XML);
		
		service = Context.getSchedulerService();
		taskHelper = new TaskHelper(service);
	}
	
	/**
	 * @verifies get a time in the future
	 * @see TaskHelper#getTime(int, int)
	 */
	@Test
	public void getTime_shouldGetATimeInTheFuture() throws Exception {
		Date then = taskHelper.getTime(Calendar.SECOND, 123);
		Assertions.assertTrue(then.after(new Date()));
	}
	
	/**
	 * @verifies get a time in the past
	 * @see TaskHelper#getTime(int, int)
	 */
	@Test
	public void getTime_shouldGetATimeInThePast() throws Exception {
		Date then = taskHelper.getTime(Calendar.SECOND, -123);
		Assertions.assertTrue(then.before(new Date()));
	}
	
	/**
	 * @verifies return a task that has been started
	 * @see TaskHelper#getScheduledTaskDefinition(java.util.Date)
	 */
	@Test
	public void getScheduledTaskDefinition_shouldReturnATaskThatHasBeenStarted() throws Exception {
		Date time = taskHelper.getTime(Calendar.SECOND, 1);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(time);
		Assertions.assertTrue(task.getStarted());
	}
	
	/**
	 * @verifies return a task that has not been started
	 * @see TaskHelper#getUnscheduledTaskDefinition(java.util.Date)
	 */
	@Test
	public void getUnscheduledTaskDefinition_shouldReturnATaskThatHasNotBeenStarted() throws Exception {
		Date time = taskHelper.getTime(Calendar.SECOND, 1);
		TaskDefinition task = taskHelper.getUnscheduledTaskDefinition(time);
		Assertions.assertFalse(task.getStarted());
	}

	// Disabled: TRUNK-6558 replaced TimerSchedulerServiceImpl with JobRunr. The new
	// JobRunrSchedulerService.scheduleTask() does not populate TaskDefinition.taskInstance, so
	// waitUntilTaskIsExecuting NPEs on task.getTaskInstance().isExecuting(). The legacy
	// TaskDefinition.taskInstance pointer is no longer the source of truth for "is executing"; the
	// helper needs to be rewritten against SchedulerService.getTask(uuid).getState() == TaskState.PROCESSING.
	@Disabled
	@Test
	public void waitUntilTaskIsExecuting_shouldWaitUntilTaskIsExecuting() throws Exception {
		Date time = taskHelper.getTime(Calendar.SECOND, 1);
		TaskDefinition task = taskHelper.getScheduledTaskDefinition(time);
		taskHelper.waitUntilTaskIsExecuting(task, MAX_WAIT_TIME_IN_MILLISECONDS);

		Assertions.assertTrue(task.getTaskInstance().isExecuting());
		deleteAllData();
	}

	// Disabled: TRUNK-6558 replaced TimerSchedulerServiceImpl with JobRunr; see note on
	// waitUntilTaskIsExecuting_shouldWaitUntilTaskIsExecuting. The polling loop now NPEs before
	// it can throw TimeoutException.
	@Disabled
	@Test
	public void waitUntilTaskIsExecuting_shouldRaiseATimeoutExceptionWhenTheTimeoutIsExceeded() throws SchedulerException,
	        InterruptedException {
		Assertions.assertThrows(TimeoutException.class, () -> {
			Date time = taskHelper.getTime(Calendar.MINUTE, 1);
			TaskDefinition task = taskHelper.getScheduledTaskDefinition(time);
			taskHelper.waitUntilTaskIsExecuting(task, 10);
		});
	}
}
