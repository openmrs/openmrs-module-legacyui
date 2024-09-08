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
 * This class is a reflection object of reference range A concept reference range is typically a
 * range of a {@link ConceptNumeric} for certain factor(s) e.g. age, gender e.t.c.
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
	 * @return Returns the conceptRangeId.
	 */
	public Integer getConceptReferenceRangeId() {
		return conceptReferenceRangeId;
	}
	
	/**
	 * @param conceptReferenceRangeId The conceptReferenceRangeId to set.
	 */
	public void setConceptReferenceRangeId(Integer conceptReferenceRangeId) {
		this.conceptReferenceRangeId = conceptReferenceRangeId;
	}
	
	/**
	 * Returns the criteria of the conceptReferenceRange
	 * 
	 * @return criteria the criteria
	 */
	public String getCriteria() {
		return this.criteria;
	}
	
	/**
	 * Sets the criteria of the conceptReferenceRange
	 * 
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	/**
	 * @return Returns the ConceptNumeric.
	 */
	public ConceptNumeric getConceptNumeric() {
		return conceptNumeric;
	}
	
	/**
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
	 * Returns high absolute value of the referenceRange
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
	 * Returns high critical value of the referenceRange
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
	 * Returns low absolute value of the referenceRange
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
	 * Returns low critical value of the referenceRange
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
	 * Returns low normal value of the referenceRange
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
