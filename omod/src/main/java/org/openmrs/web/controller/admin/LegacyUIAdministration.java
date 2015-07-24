package org.openmrs.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LegacyUIAdministration {
	@RequestMapping(value = "admin/index")
	public String displayAdminIndex(){
	return "module/legacyui/admin/index";
	}
}
