package com.buildflow.cms.controller;

import com.buildflow.cms.dto.UserAdminForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        model.addAttribute("page", userService.search(search, pageable));
        model.addAttribute("search", search);
        model.addAttribute("roles", Role.values());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("userAdminForm")) {
            UserAdminForm form = new UserAdminForm();
            form.setEnabled(true);
            form.setRole(Role.SITE_ENGINEER);
            model.addAttribute("userAdminForm", form);
        }
        model.addAttribute("roles", Role.values());
        model.addAttribute("pageTitle", "Create User");
        return "admin/users/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("userAdminForm") UserAdminForm userAdminForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Create User");
            return "admin/users/form";
        }
        try {
            AppUser user = userService.createUser(userAdminForm);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
            return "redirect:/admin/users/" + user.getId() + "/edit";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("userError", ex.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Create User");
            return "admin/users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("userAdminForm")) {
            model.addAttribute("userAdminForm", toForm(userService.findById(id)));
        }
        model.addAttribute("roles", Role.values());
        model.addAttribute("pageTitle", "Edit User");
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("userAdminForm") UserAdminForm userAdminForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        userAdminForm.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Edit User");
            return "admin/users/form";
        }
        try {
            userService.updateUser(id, userAdminForm);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
            return "redirect:/admin/users";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("userError", ex.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageTitle", "Edit User");
            return "admin/users/form";
        }
    }

    @PostMapping("/{id}/role")
    public String updateRole(
            @PathVariable Long id,
            @RequestParam Role role,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "User role updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            AppUser user = userService.toggleEnabled(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    user.isEnabled() ? "User enabled." : "User disabled.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    private UserAdminForm toForm(AppUser user) {
        UserAdminForm form = new UserAdminForm();
        form.setId(user.getId());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole());
        form.setEnabled(user.isEnabled());
        return form;
    }
}
