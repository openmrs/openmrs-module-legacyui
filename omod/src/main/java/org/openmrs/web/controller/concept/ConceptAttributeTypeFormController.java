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

import org.openmrs.ConceptAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.validator.ConceptAttributeTypeValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import java.util.Collection;

@Controller
public class ConceptAttributeTypeFormController {

    private static final String CONCEPT_ATTRIBUTE_TYPES_LIST_URL = "redirect:/admin/concepts/conceptAttributeTypes.list";

    @ModelAttribute("datatypes")
    public Collection<String> getDatatypes() {
        return CustomDatatypeUtil.getDatatypeClassnames();
    }

    @ModelAttribute("dataTypeReadOnly")
    public Boolean getDatatypeReadOnly( @RequestParam(value = "id", required = false) ConceptAttributeType attrType) {
        if(attrType != null){
            return Context.getConceptService().hasAnyConceptAttribute(attrType);
        }
        return false;
    }

    @ModelAttribute("handlers")
    public Collection<String> getHandlers() {
        return CustomDatatypeUtil.getHandlerClassnames();
    }

    @ModelAttribute("attributeType")
    public ConceptAttributeType formBackingObject(
            @RequestParam(value = "id", required = false) ConceptAttributeType attrType) {
        if (attrType == null) {
            attrType = new ConceptAttributeType();
        }
        return attrType;
    }

    /**
     * Show existing (or instantiate blank)
     */
    @RequestMapping(value = "/admin/concepts/conceptAttributeType", method = RequestMethod.GET)
    public void showForm() {
    }

    @RequestMapping(value = "/admin/concepts/conceptAttributeType", method = RequestMethod.POST)
    public String handleSubmit(WebRequest request, @ModelAttribute("attributeType") ConceptAttributeType conceptAttributeType,
                               BindingResult errors) {
        if(Context.isAuthenticated()) {
            ConceptService conceptService = Context.getConceptService();
            if (request.getParameter("purge") != null) {
                return purgeConceptAttributeType(request, conceptAttributeType, conceptService);
            }
            new ConceptAttributeTypeValidator().validate(conceptAttributeType, errors);
            if (!errors.hasErrors()) {
                if (request.getParameter("retire") != null) {
                    return retireConceptAttributeType(request, conceptAttributeType, errors);
                }
                if (request.getParameter("save") != null) {
                    return saveConceptAttributeType(request, conceptAttributeType, conceptService);
                }
                if (request.getParameter("unretire") != null) {
                    return unretireConceptAttributeType(request, conceptAttributeType);
                }
            }
        }
        return null;
    }

    private String unretireConceptAttributeType(WebRequest request, @ModelAttribute("attributeType") ConceptAttributeType conceptAttributeType) {
        Context.getConceptService().unretireConceptAttributeType(conceptAttributeType);
        request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
                "ConceptAttributeType.unretired"), WebRequest.SCOPE_SESSION);
        return CONCEPT_ATTRIBUTE_TYPES_LIST_URL;
    }

    private String retireConceptAttributeType(WebRequest request, ConceptAttributeType conceptAttributeType, BindingResult errors) {
        String retireReason = request.getParameter("retireReason");
        if (conceptAttributeType.getId() != null && !(StringUtils.hasText(retireReason))) {
            errors.reject("retireReason", "general.retiredReason.empty");
            return null;
        }
        Context.getConceptService().retireConceptAttributeType(conceptAttributeType, retireReason);
        request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
                "ConceptAttributeType.retired"), WebRequest.SCOPE_SESSION);
        return CONCEPT_ATTRIBUTE_TYPES_LIST_URL;
    }

    private String purgeConceptAttributeType(WebRequest request, ConceptAttributeType conceptAttributeType, ConceptService conceptService) {
        try {
            conceptService.purgeConceptAttributeType(conceptAttributeType);
            request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptAttributeType.purgedSuccessfully",
                    WebRequest.SCOPE_SESSION);
        } catch (Exception e) {
            request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge",
                    WebRequest.SCOPE_SESSION);
        }
        return CONCEPT_ATTRIBUTE_TYPES_LIST_URL;
    }

    private String saveConceptAttributeType(WebRequest request, ConceptAttributeType conceptAttributeType, ConceptService conceptService) {
        conceptService.saveConceptAttributeType(conceptAttributeType);
        request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
                "ConceptAttributeType.saved"), WebRequest.SCOPE_SESSION);

        return CONCEPT_ATTRIBUTE_TYPES_LIST_URL;
    }
}
