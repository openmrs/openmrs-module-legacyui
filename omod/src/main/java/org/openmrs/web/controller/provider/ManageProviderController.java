package org.openmrs.web.controller.provider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManageProviderController {
	@RequestMapping(value = "admin/provider/index")
	public String displayIndex(){
	return "module/legacyui/admin/provider/index";
	}
}
