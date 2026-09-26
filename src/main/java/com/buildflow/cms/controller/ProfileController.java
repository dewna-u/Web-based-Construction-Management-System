package com.buildflow.cms.controller;

import com.buildflow.cms.dto.ChangePasswordForm;
import com.buildflow.cms.dto.ProfileForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.UserService;
import com.buildflow.cms.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String view(Model model) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        model.addAttribute("user", currentUser);
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setFullName(currentUser.getFullName());
            form.setEmail(currentUser.getEmail());
            model.addAttribute("profileForm", form);
        }
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        return "profile/index";
    }

    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        if (bindingResult.hasErrors()) {
            return "profile/index";
        }
        try {
            userService.updateProfile(currentUser.getId(), profileForm);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
            return "redirect:/profile";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("profileError", ex.getMessage());
            return "profile/index";
        }
    }

    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        model.addAttribute("user", currentUser);
        ProfileForm profileForm = new ProfileForm();
        profileForm.setFullName(currentUser.getFullName());
        profileForm.setEmail(currentUser.getEmail());
        model.addAttribute("profileForm", profileForm);
        if (bindingResult.hasErrors()) {
            return "profile/index";
        }
        try {
            userService.changePassword(currentUser.getId(), changePasswordForm);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
            return "redirect:/profile";
        } catch (BusinessException ex) {
            bindingResult.reject("passwordError", ex.getMessage());
            return "profile/index";
        }
    }
}
