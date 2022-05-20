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

import org.openmrs.scheduler.TaskDefinition;

public class TaskAction extends TaskServiceWrapper {
	
	private TaskServiceWrapper taskServiceWrapper = new TaskServiceWrapper();
	
	public void setTaskServiceWrapper(TaskServiceWrapper taskServiceWrapper) {
		this.taskServiceWrapper = taskServiceWrapper;
	}
	
	public TaskDefinition getByUniqueId(String uniqueId) {
		TaskDefinition taskDefinition = taskServiceWrapper.getTaskByName(uniqueId);
		if (taskDefinition == null) {
			taskDefinition = taskServiceWrapper.getTaskById(Integer.parseInt(uniqueId));
		}
		return taskDefinition;
	}
}
