/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptNumeric;
import org.openmrs.web.controller.ConceptFormController;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;

public class ConceptFormControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Test
	public void ConceptFormBackingObject_shouldCopyNumericAttributes() {
		ConceptNumeric concept = Mockito.mock(ConceptNumeric.class);
		Mockito.when(concept.getHiAbsolute()).thenReturn(5.2);
		Mockito.when(concept.getLowAbsolute()).thenReturn(1.0);
		
		Mockito.when(concept.getHiCritical()).thenReturn(4.1);
		Mockito.when(concept.getLowCritical()).thenReturn(2.1);
		
		Mockito.when(concept.getLowNormal()).thenReturn(3.1);
		Mockito.when(concept.getHiNormal()).thenReturn(3.9);
		
		Mockito.when(concept.getDisplayPrecision()).thenReturn(42);
		
		Mockito.when(concept.getUnits()).thenReturn("ml");
		
		Mockito.when(concept.getConceptMappings()).thenReturn(new ArrayList<ConceptMap>());
		
		ConceptFormController controller = new ConceptFormController();
		ConceptFormController.ConceptFormBackingObject conceptFormBackingObject = controller.new ConceptFormBackingObject(
		                                                                                                                  concept);
		
		org.junit.jupiter.api.Assertions.assertEquals(Double.valueOf(5.2), conceptFormBackingObject.getHiAbsolute());
		org.junit.jupiter.api.Assertions.assertEquals(Double.valueOf(1.0), conceptFormBackingObject.getLowAbsolute());
		
		org.junit.jupiter.api.Assertions.assertEquals(Double.valueOf(4.1), conceptFormBackingObject.getHiCritical());
		org.junit.jupiter.api.Assertions.assertEquals(Double.valueOf(2.1), conceptFormBackingObject.getLowCritical());
		
		org.junit.jupiter.api.Assertions.assertEquals(Double.valueOf(3.1), conceptFormBackingObject.getLowNormal());
		org.junit.jupiter.api.Assertions.assertEquals(Double.valueOf(3.9), conceptFormBackingObject.getHiNormal());
		
		org.junit.jupiter.api.Assertions.assertEquals(Integer.valueOf(42), conceptFormBackingObject.getDisplayPrecision());
		
		org.junit.jupiter.api.Assertions.assertEquals("ml", conceptFormBackingObject.getUnits());
	}
	
}
