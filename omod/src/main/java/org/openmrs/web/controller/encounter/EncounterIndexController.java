package org.openmrs.web.controller.encounter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class EncounterIndexController {
	
	@RequestMapping(value = "admin/encounters/index")
	public String displayEncounterIndex() {
		return "module/legacyui/admin/encounters/index";
	}

}
