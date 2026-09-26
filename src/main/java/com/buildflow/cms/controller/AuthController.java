package com.buildflow.cms.controller;

import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.dto.SignupForm;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/setup")
    public String setupForm(Model model) {
        if (userService.existsUsers()) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("setupForm")) {
            model.addAttribute("setupForm", new SetupForm());
        }
        return "auth/setup";
    }

    @PostMapping("/setup")
    public String setupSubmit(
            @Valid @ModelAttribute("setupForm") SetupForm setupForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (userService.existsUsers()) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            return "auth/setup";
        }
        try {
            userService.createFirstAdmin(setupForm);
            redirectAttributes.addFlashAttribute("successMessage", "Admin account created. Please sign in.");
            return "redirect:/login";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("setupError", ex.getMessage());
            return "auth/setup";
        }
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        if (!model.containsAttribute("signupForm")) {
            SignupForm form = new SignupForm();
            form.setRole(Role.CLIENT);
            model.addAttribute("signupForm", form);
        }
        model.addAttribute("signupRoles", new Role[]{Role.CLIENT, Role.CONTRACTOR});
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(
            @Valid @ModelAttribute("signupForm") SignupForm signupForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("signupRoles", new Role[]{Role.CLIENT, Role.CONTRACTOR});
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            userService.signup(signupForm);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully. Please sign in.");
            return "redirect:/login";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("signupError", ex.getMessage());
            return "auth/signup";
        }
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been signed out.");
        }
        return "auth/login";
    }
}
