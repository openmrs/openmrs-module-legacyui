/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LegacyUIJavaConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var localeChangeInterceptor = new org.springframework.web.servlet.i18n.LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);

        var themeChangeInterceptor = new org.springframework.web.servlet.theme.ThemeChangeInterceptor();
        themeChangeInterceptor.setParamName("theme");
        registry.addInterceptor(themeChangeInterceptor);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("findPatient.htm").setViewName("module/legacyui/findPatient");
        registry.addViewController("admin/index.htm").setViewName("module/legacyui/admin/index");
        registry.addViewController("dictionary/index.htm").setViewName("module/legacyui/dictionary/index");
        registry.addViewController("patients/index.htm").setViewName("module/legacyui/admin/patients/index");
        registry.addViewController("encounters/index.htm").setViewName("module/legacyui/admin/encounters/index");
        registry.addViewController("observations/index.htm").setViewName("module/legacyui/admin/observations/index");
        registry.addViewController("help.htm").setViewName("module/legacyui/help");
    }
}
