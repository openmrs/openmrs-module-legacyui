package org.openmrs.web.controller.person;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PersonController {
	
	@RequestMapping(value = "admin/person/index")
	public String displayIndex() {
		return "module/legacyui/admin/person/index";
	}
	
}
