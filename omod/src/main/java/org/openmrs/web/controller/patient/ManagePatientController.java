package org.openmrs.web.controller.patient;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManagePatientController {
	@RequestMapping(value = "admin/patients/index")
	public String displayPatientsIndex(){
	return "module/legacyui/admin/patients/index";
	}
}
