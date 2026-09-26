package com.buildflow.cms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", "You do not have permission to view this page.");
        return "error/403";
    }
}
