package org.openmrs.template.web.controller;

import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class FooterFullController {
	

	@RequestMapping("/template/footerFull.htm")
	public String footerFull(Model model) {

		if ((Context.getMessageSourceService().getMessage("legacyui.footer.extradata")) != null) {

			String footerExtraData = Context.getMessageSourceService().getMessage("legacyui.footer.extradata");

			model.addAttribute("footerExtraData", footerExtraData);
		}

		return "module/legacyui/template/footerFull";
	}

}
