package org.openmrs.web.controller;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

@org.springframework.stereotype.Controller
@RequestMapping("/**") // handled via PathPattern with /** mapping in XML
public class LegacyCatchAllController implements Controller {

    @Autowired
    private Controller springController;
    @Autowired
    private Controller fieldGenController;
    @Autowired
    private Controller portletController;

    @Autowired
    private Controller globalPropertyPortletController;

    @Autowired
    private Controller addressLayoutPortletController;

    @Autowired
    private Controller nameLayoutPortletController;

    @Autowired
    private Controller patientProgramsPortletController;

    @Autowired
    private Controller personRelationshipsPortletController;

    @Autowired
    private Controller patientEncountersPortletController;

    @Autowired
    private Controller patientVisitsPortletController;

    @Autowired
    private Controller personFormEntryPortletController;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();
        if (path == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Normalize (strip context path if present)
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        if (path.endsWith("/globalProperties.portlet")) {
            return globalPropertyPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/addressLayout.portlet")) {
            return addressLayoutPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/nameLayout.portlet")) {
            return nameLayoutPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/patientPrograms.portlet")) {
            return patientProgramsPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/personRelationships.portlet")) {
            return personRelationshipsPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/patientEncounters.portlet")) {
            return patientEncountersPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/patientVisits.portlet")) {
            return patientVisitsPortletController.handleRequest(request, response);
        }
        if (path.endsWith("/personFormEntry.portlet")) {
            return personFormEntryPortletController.handleRequest(request, response);
        }

        if (path.endsWith(".field")) {
            return fieldGenController.handleRequest(request, response);
        }
        if (path.endsWith(".htm")) {
            return springController.handleRequest(request, response);
        }
        if (path.endsWith(".portlet")) {
            return portletController.handleRequest(request, response);
        }

        // Default fallback – nothing matched
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "No handler found for " + path);
        return null;
    }


    public void setSpringController(Controller springController) {
        this.springController = springController;
    }

    public void setFieldGenController(Controller fieldGenController) {
        this.fieldGenController = fieldGenController;
    }

    public void setPortletController(Controller portletController) {
        this.portletController = portletController;
    }

    public void setGlobalPropertyPortletController(Controller globalPropertyPortletController) {
        this.globalPropertyPortletController = globalPropertyPortletController;
    }

    public void setAddressLayoutPortletController(Controller addressLayoutPortletController) {
        this.addressLayoutPortletController = addressLayoutPortletController;
    }

    public void setNameLayoutPortletController(Controller nameLayoutPortletController) {
        this.nameLayoutPortletController = nameLayoutPortletController;
    }

    public void setPatientProgramsPortletController(Controller patientProgramsPortletController) {
        this.patientProgramsPortletController = patientProgramsPortletController;
    }

    public void setPersonRelationshipsPortletController(Controller personRelationshipsPortletController) {
        this.personRelationshipsPortletController = personRelationshipsPortletController;
    }

    public void setPatientEncountersPortletController(Controller patientEncountersPortletController) {
        this.patientEncountersPortletController = patientEncountersPortletController;
    }

    public void setPatientVisitsPortletController(Controller patientVisitsPortletController) {
        this.patientVisitsPortletController = patientVisitsPortletController;
    }

    public void setPersonFormEntryPortletController(Controller personFormEntryPortletController) {
        this.personFormEntryPortletController = personFormEntryPortletController;
    }
}

