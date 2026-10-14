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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.logging.OpenmrsLoggingUtil;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

class ServerLogControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Test
	void formBackingObject_shouldReturnTheLinesHeldByTheMemoryAppender() throws Exception {
		MemoryAppender memoryAppender = OpenmrsLoggingUtil.getMemoryAppender();
		assertNotNull(memoryAppender, "the test logging configuration should define MEMORY_APPENDER");
		String marker = "ServerLogControllerTest-" + UUID.randomUUID();
		LogManager.getRootLogger().error(marker);
		
		List<String> lines = new ServerLogController().formBackingObject(new MockHttpServletRequest());
		
		assertTrue(lines.stream().anyMatch(line -> line.contains(marker)), "expected the logged marker in " + lines);
		assertEquals(memoryAppender.getLogLines(), lines);
	}
}
