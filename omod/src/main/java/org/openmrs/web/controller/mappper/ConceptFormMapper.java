/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.mappper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptNumeric;
import org.openmrs.web.controller.concept.ConceptReferenceRange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class maps the web-based concept attributes to their corresponding internal concepts and
 * vice versa.
 * 
 * @since 1.17.0
 */
public class ConceptFormMapper {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	/**
     * Maps web's reference range to reference range in openMRS core
	 * 
	 * @param webReferenceRange webReferenceRange
	 * @param cn ConceptNumeric
	 * @return reference range
	 */
	public Object mapToConceptReferenceRange(
            ConceptReferenceRange webReferenceRange,
            ConceptNumeric cn) {

        try {
            Class<?> referenceRangeClass = Class.forName("org.openmrs.ConceptReferenceRange");
            Object referenceRange = referenceRangeClass.getDeclaredConstructor().newInstance();

            referenceRangeClass.getMethod("setCriteria", String.class).invoke(referenceRange, webReferenceRange.getCriteria());
            referenceRangeClass.getMethod("setConceptNumeric", ConceptNumeric.class).invoke(referenceRange, cn);
            referenceRangeClass.getMethod("setUuid", String.class).invoke(referenceRange, webReferenceRange.getUuid());
            referenceRangeClass.getMethod("setHiAbsolute", Double.class).invoke(referenceRange, webReferenceRange.getHiAbsolute());
            referenceRangeClass.getMethod("setHiCritical", Double.class).invoke(referenceRange, webReferenceRange.getHiCritical());
            referenceRangeClass.getMethod("setHiNormal", Double.class).invoke(referenceRange, webReferenceRange.getHiNormal());
            referenceRangeClass.getMethod("setLowAbsolute", Double.class).invoke(referenceRange, webReferenceRange.getLowAbsolute());
            referenceRangeClass.getMethod("setLowCritical", Double.class).invoke(referenceRange, webReferenceRange.getLowCritical());
            referenceRangeClass.getMethod("setLowNormal", Double.class).invoke(referenceRange, webReferenceRange.getLowNormal());
            referenceRangeClass.getMethod("setConceptReferenceRangeId", Integer.class).invoke(referenceRange, webReferenceRange.getConceptReferenceRangeId());

            return referenceRange;

        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                 ClassNotFoundException exception) {
            logger.error("Failed to add reference range: Exception: " + exception.getMessage(), exception);
        }
        return null;
    }
	
	/**
     * Maps reference ranges in openMRS core to web's reference ranges
	 * 
	 * @param cn ConceptNumeric
	 * @return list of reference range
	 */
	public List<ConceptReferenceRange> mapToWebReferenceRanges(ConceptNumeric cn) {
        List<ConceptReferenceRange> webReferenceRanges = new ArrayList<>();

        try {
            Method getReferenceRangesMethod = ConceptNumeric.class.getMethod("getReferenceRanges");
            Object referenceRanges = getReferenceRangesMethod.invoke(cn);

            for (Object referenceRange : (Set<?>) referenceRanges) {
                ConceptReferenceRange webReferenceRange = new ConceptReferenceRange();

                webReferenceRange.setConceptReferenceRangeId((Integer) referenceRange.getClass().getMethod("getConceptReferenceRangeId").invoke(referenceRange));
                webReferenceRange.setUuid((String) referenceRange.getClass().getMethod("getUuid").invoke(referenceRange));
                webReferenceRange.setCriteria((String) referenceRange.getClass().getMethod("getCriteria").invoke(referenceRange));
                webReferenceRange.setHiAbsolute((Double) referenceRange.getClass().getMethod("getHiAbsolute").invoke(referenceRange));
                webReferenceRange.setHiCritical((Double) referenceRange.getClass().getMethod("getHiCritical").invoke(referenceRange));
                webReferenceRange.setHiNormal((Double) referenceRange.getClass().getMethod("getHiNormal").invoke(referenceRange));
                webReferenceRange.setLowAbsolute((Double) referenceRange.getClass().getMethod("getLowAbsolute").invoke(referenceRange));
                webReferenceRange.setLowCritical((Double) referenceRange.getClass().getMethod("getLowCritical").invoke(referenceRange));
                webReferenceRange.setLowNormal((Double) referenceRange.getClass().getMethod("getLowNormal").invoke(referenceRange));
                webReferenceRange.setConceptNumeric((ConceptNumeric) referenceRange.getClass().getMethod("getConceptNumeric").invoke(referenceRange));

                webReferenceRanges.add(webReferenceRange);
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            logger.error("Failed to map to web reference range: Exception: " + e.getMessage(), e);
        }

        return webReferenceRanges;
    }
}
