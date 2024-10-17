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

import org.openmrs.ConceptNumeric;

/**
 * This class represents a reference range for a {@link ConceptNumeric}. It is used to store and
 * manage reference range values, to be able to allow backward compatibility in openmrs core
 * versions. A concept reference range defines the acceptable numeric values/ranges for specific
 * factors such as age, gender, e.t.c.
 * 
 * @since 1.17.0
 */
public class ConceptReferenceRange {
	
	private Integer conceptReferenceRangeId;
	
	private String criteria;
	
	private ConceptNumeric conceptNumeric;
	
	private String uuid;
	
	private Double hiAbsolute;
	
	private Double hiCritical;
	
	private Double hiNormal;
	
	private Double lowAbsolute;
	
	private Double lowCritical;
	
	private Double lowNormal;
	
	public ConceptReferenceRange() {
	}
	
	/**
	 * Gets id of conceptReferenceRange
	 * 
	 * @return Returns the ConceptReferenceRangeId.
	 */
	public Integer getConceptReferenceRangeId() {
		return conceptReferenceRangeId;
	}
	
	/**
	 * Sets conceptReferenceRangeId
	 * 
	 * @param conceptReferenceRangeId The conceptReferenceRangeId to set.
	 */
	public void setConceptReferenceRangeId(Integer conceptReferenceRangeId) {
		this.conceptReferenceRangeId = conceptReferenceRangeId;
	}
	
	/**
	 * Gets the criteria of conceptReferenceRange
	 * 
	 * @return criteria
	 */
	public String getCriteria() {
		return this.criteria;
	}
	
	/**
	 * Sets the criteria of conceptReferenceRange
	 * 
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	/**
	 * Gets conceptNumeric of conceptReferenceRange
	 * 
	 * @return Returns the ConceptNumeric.
	 */
	public ConceptNumeric getConceptNumeric() {
		return conceptNumeric;
	}
	
	/**
	 * Sets conceptNumeric
	 * 
	 * @param conceptNumeric concept to set.
	 */
	public void setConceptNumeric(ConceptNumeric conceptNumeric) {
		this.conceptNumeric = conceptNumeric;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptReferenceRangeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptReferenceRangeId(id);
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getUuid()
	 */
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setUuid(java.lang.String)
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * Gets high absolute value of the referenceRange
	 * 
	 * @return hiAbsolute the high absolute value
	 */
	public Double getHiAbsolute() {
		return this.hiAbsolute;
	}
	
	/**
	 * Sets high absolute value of the referenceRange
	 * 
	 * @param hiAbsolute high absolute value to set
	 */
	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}
	
	/**
	 * Gets high critical value of the referenceRange
	 * 
	 * @return the high critical value
	 */
	public Double getHiCritical() {
		return this.hiCritical;
	}
	
	/**
	 * Sets high critical value of the referenceRange
	 * 
	 * @param hiCritical high critical value to set
	 */
	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}
	
	/**
	 * Returns high normal value of the referenceRange
	 * 
	 * @return the high normal value
	 */
	public Double getHiNormal() {
		return this.hiNormal;
	}
	
	/**
	 * Sets high normal value of the referenceRange
	 * 
	 * @param hiNormal high normal value to set
	 */
	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}
	
	/**
	 * Gets low absolute value of the referenceRange
	 * 
	 * @return the low absolute value
	 */
	public Double getLowAbsolute() {
		return this.lowAbsolute;
	}
	
	/**
	 * Sets low absolute value of the referenceRange
	 * 
	 * @param lowAbsolute low absolute value to set
	 */
	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}
	
	/**
	 * Gets low critical value of the referenceRange
	 * 
	 * @return the low critical value
	 */
	public Double getLowCritical() {
		return this.lowCritical;
	}
	
	/**
	 * Sets low critical value of the referenceRange
	 * 
	 * @param lowCritical low critical value to set
	 */
	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}
	
	/**
	 * Gets low normal value of the referenceRange
	 * 
	 * @return the low normal value
	 */
	public Double getLowNormal() {
		return this.lowNormal;
	}
	
	/**
	 * Sets low normal value of the referenceRange
	 * 
	 * @param lowNormal low normal value to set
	 */
	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}
}
