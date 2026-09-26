package com.buildflow.cms.controller;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.service.DashboardService;
import com.buildflow.cms.service.NotificationService;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationService notificationService;

    public DashboardController(DashboardService dashboardService, NotificationService notificationService) {
        this.dashboardService = dashboardService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String root() {
        if (SecurityUtils.getCurrentUser().isPresent()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        model.addAllAttributes(dashboardService.getDashboardData());
        model.addAttribute("notifications", notificationService.listForUser(currentUser.getId()).stream().limit(8).toList());
        model.addAttribute("unreadCount", notificationService.countUnread(currentUser.getId()));
        model.addAttribute("currentUser", currentUser);
        return "dashboard/index";
    }
}
