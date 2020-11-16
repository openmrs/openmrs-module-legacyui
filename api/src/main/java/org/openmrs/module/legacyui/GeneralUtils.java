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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.springframework.transaction.annotation.Transactional;

public class GeneralUtils {
	
	private static final Log log = LogFactory.getLog(GeneralUtils.class);
	
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
	 * @see OrderExtensionService#getProviderForUser(User)
	 */
	@Transactional(readOnly = true)
	public static Provider getProviderForUser(User user) {
		ProviderService ps = Context.getProviderService();
		Collection<Provider> providers = ps.getProvidersByPerson(user.getPerson(), true);
		if (providers.isEmpty()) {
			throw new IllegalStateException("User " + user + " has no provider accounts, unable to create orders");
		}
		return providers.iterator().next();
	}
	
	/**
	 * Discontinues all current orders for the given <code>patient</code>
	 * 
	 * @param patient
	 * @param discontinueReason
	 * @param discontinueDate
	 * @see OrderService#discontinueOrder(org.openmrs.Order, Concept, Date)
	 * @should discontinue all orders for the given patient if none are yet discontinued
	 * @should not affect orders that were already discontinued on the specified date
	 * @should not affect orders that end before the specified date
	 * @should not affect orders that start after the specified date
	 */
	public static void discontinueAllDrugOrders(Patient patient, Concept discontinueReason, Date discontinueDate) {
		if (log.isDebugEnabled())
			log.debug("In discontinueAll with patient " + patient + " and concept " + discontinueReason + " and date "
			        + discontinueDate);
		
		OrderService orderService = Context.getOrderService();
		int durgOrderType = 2; //Default OpenMRS core drug order type ID
		try {
			durgOrderType = Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
			    "orderextension.drugOrderType"));
		}
		catch (Exception e) {
			log.error("orderextension.drugOrderType global property value should be an integer");
		}
		List<Order> drugOrders = orderService.getOrders(patient, orderService.getCareSetting(2), Context.getOrderService()
		        .getOrderType(durgOrderType), false);
		// loop over all of this patient's drug orders to discontinue each
		if (drugOrders != null) {
			for (Order drugOrder : drugOrders) {
				if (log.isDebugEnabled())
					log.debug("discontinuing order: " + drugOrder);
				// do the stuff to the database
				if (drugOrder.isActive() || drugOrder.getEffectiveStopDate() == null) {
					try {
						Context.getOrderService().discontinueOrder(drugOrder, discontinueReason, discontinueDate,
						    getProviderForUser(Context.getAuthenticatedUser()), drugOrder.getEncounter());
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					
				}
			}
		}
	}
}
