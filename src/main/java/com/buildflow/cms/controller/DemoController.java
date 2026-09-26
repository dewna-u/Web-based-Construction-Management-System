package com.buildflow.cms.controller;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.service.DemoDataService;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class DemoController {

    private final ObjectProvider<DemoDataService> demoDataServiceProvider;

    public DemoController(ObjectProvider<DemoDataService> demoDataServiceProvider) {
        this.demoDataServiceProvider = demoDataServiceProvider;
    }

    @PostMapping("/demo-data")
    @PreAuthorize("hasRole('ADMIN')")
    public String generateDemoData(RedirectAttributes redirectAttributes) {
        DemoDataService demoDataService = demoDataServiceProvider.getIfAvailable();
        if (demoDataService == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Demo data generation is disabled. Set app.demo.enabled=true to enable it.");
            return "redirect:/dashboard";
        }

        if (!demoDataService.isDemoEnabled()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Demo data generation is disabled. Set app.demo.enabled=true to enable it.");
            return "redirect:/dashboard";
        }
        if (demoDataService.isDemoDataPresent()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Demo data is already loaded. Use accounts like pm@demo.buildflow.local / Demo@123.");
            return "redirect:/dashboard";
        }

        AppUser adminUser = SecurityUtils.requireCurrentUser();
        boolean created = demoDataService.generateDemoData(adminUser);
        if (created) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Demo data generated successfully. Demo account password: Demo@123");
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Demo data could not be generated. Check application logs for details.");
        }
        return "redirect:/dashboard";
    }
}
