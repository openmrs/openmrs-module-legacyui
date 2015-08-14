package org.openmrs.web.controller.patient;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FindDuplicatePatientsController {
	@RequestMapping(value = "admin/patients/findDuplicatePatients")
	public String displayDuplicatePatients(){
	return "module/legacyui/admin/patients/findDuplicatePatients";
	}
}
