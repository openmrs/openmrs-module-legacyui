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

import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

import java.util.Calendar;
import java.util.Date;

/**
 * Provides helper methods for creating scheduled and unscheduled tasks.
 */
public class TaskHelper {

	private SchedulerService service;
	
	public TaskHelper(SchedulerService service) {
		this.service = service;
	}
	
	/**
	 * @param unit defines the unit of the offset
	 * @param value defines the value of the offset
	 * @return a date object based on an offset relative to the current date and time
	 * @should get a time in the future
	 * @should get a time in the past
	 */
	public Date getTime(int unit, int value) {
		Calendar cal = Calendar.getInstance();
		cal.add(unit, value);
		return cal.getTime();
	}
	
	/**
	 * @param startTime defines the start time for a scheduled task
	 * @return a task that has been scheduled and started
	 * @throws SchedulerException if the task cannot be scheduled
	 * @should return a task that has been started
	 */
	public TaskDefinition getScheduledTaskDefinition(Date startTime) throws SchedulerException {
		TaskDefinition taskDefinition = getTaskDefinition(startTime);
		service.scheduleTask(taskDefinition);
		return taskDefinition;
	}
	
	/**
	 * @param startTime defines the start time for a scheduled task
	 * @return a task that has not been scheduled and has not started
	 * @should return a task that has not been started
	 */
	public TaskDefinition getUnscheduledTaskDefinition(Date startTime) {
		return getTaskDefinition(startTime);
	}
	
	private TaskDefinition getTaskDefinition(Date startTime) {
		TaskDefinition task = service.getTaskByName("Hello World Task");
		
		task.setStartTime(startTime);
		service.saveTaskDefinition(task);

		return task;
	}

}
