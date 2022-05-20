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

import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;

public class TaskServiceWrapper {
	
	public TaskDefinition getTaskByUuid(String taskUuid) {
		return ((TaskServiceWrapper) Context.getSchedulerService()).getTaskByUuid(taskUuid);
	}
	
	public TaskDefinition getTaskById(Integer id) {
		return Context.getSchedulerService().getTask(id);
	}
	
	public TaskDefinition getTaskByName(String taskName) {
		return Context.getSchedulerService().getTaskByName(taskName);
	}
}
