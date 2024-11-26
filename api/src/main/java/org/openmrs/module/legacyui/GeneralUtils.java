/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.legacyui;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

public class GeneralUtils {
	
	public static boolean isValidUuidFormat(String uuid) {
		return uuid.length() >= 36 && uuid.length() <= 38 && !uuid.contains(" ");
	}
	
	/**
	 * Get the concept by id, the id can either be 1)an integer id like 5090 or 2)mapping type id
	 * like "XYZ:HT" or 3)uuid like "a3e12268-74bf-11df-9768-17cfc9833272"
	 * 
	 * @param id
	 * @return the concept if exist, else null
	 * @should find a concept by its conceptId
	 * @should find a concept by its mapping
	 * @should find a concept by its uuid
	 * @should return null otherwise
	 * @should find a concept by its mapping with a space in between
	 */
	public static Concept getConcept(String id) {
		Concept cpt = null;
		
		if (id != null) {
			
			// see if this is a parseable int; if so, try looking up concept by id
			try { //handle integer: id
				int conceptId = Integer.parseInt(id);
				cpt = Context.getConceptService().getConcept(conceptId);
				
				if (cpt != null) {
					return cpt;
				}
			}
			catch (Exception ex) {
				//do nothing
			}
			
			// handle  mapping id: xyz:ht
			int index = id.indexOf(":");
			if (index != -1) {
				String mappingCode = id.substring(0, index).trim();
				String conceptCode = id.substring(index + 1, id.length()).trim();
				cpt = Context.getConceptService().getConceptByMapping(conceptCode, mappingCode);
				
				if (cpt != null) {
					return cpt;
				}
			}
			
			//handle uuid id: "a3e1302b-74bf-11df-9768-17cfc9833272", if the id matches a uuid format
			if (isValidUuidFormat(id)) {
				cpt = Context.getConceptService().getConceptByUuid(id);
			}
		}
		
		return cpt;
	}
	
	/**
	 * Checks if current version of openmrs is greater or equal to 2.7.0 The aim is to try loading
	 * ConceptReferenceRange class, which is in version 2.7.0. If the ConceptReferenceRange class is
	 * loaded, then the current version is greater than or equal to 2.7.0
	 * 
	 * @return true if current version is greater or equal to 2.7.0 and false otherwise
	 * @since 1.17.0
	 */
	public static boolean isTwoPointSevenAndAbove() {
		try {
			Class.forName("org.openmrs.ConceptReferenceRange");
			
			return true;
		}
		catch (ClassNotFoundException exception) {
			return false;
		}
	}
}
