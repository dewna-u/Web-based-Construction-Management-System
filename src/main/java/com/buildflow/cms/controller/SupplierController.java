package com.buildflow.cms.controller;

import com.buildflow.cms.dto.SupplierForm;
import com.buildflow.cms.entity.Supplier;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.SupplierService;
import com.buildflow.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final UserService userService;

    public SupplierController(SupplierService supplierService, UserService userService) {
        this.supplierService = supplierService;
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<Supplier> suppliers = supplierService.search(search);
        model.addAttribute("page", toPage(suppliers, pageable));
        model.addAttribute("search", search);
        model.addAttribute("activeCount", supplierService.countActive());
        return "suppliers/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String createForm(Model model) {
        if (!model.containsAttribute("supplierForm")) {
            model.addAttribute("supplierForm", new SupplierForm());
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Add Supplier");
        return "suppliers/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String create(
            @Valid @ModelAttribute("supplierForm") SupplierForm supplierForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Supplier");
            return "suppliers/form";
        }
        try {
            Supplier supplier = supplierService.create(supplierForm);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier created successfully.");
            return "redirect:/suppliers/" + supplier.getId();
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("supplierError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Supplier");
            return "suppliers/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.findById(id));
        return "suppliers/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("supplierForm")) {
            model.addAttribute("supplierForm", toForm(supplierService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Supplier");
        return "suppliers/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("supplierForm") SupplierForm supplierForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        supplierForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Supplier");
            return "suppliers/form";
        }
        try {
            supplierService.update(id, supplierForm);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier updated successfully.");
            return "redirect:/suppliers/" + id;
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("supplierError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Supplier");
            return "suppliers/form";
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        supplierService.activate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier activated.");
        return "redirect:/suppliers/" + id;
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        supplierService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier deactivated.");
        return "redirect:/suppliers/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("successMessage", supplierService.delete(id));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/suppliers";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("linkableUsers", userService.findByRole(Role.SUPPLIER));
    }

    private SupplierForm toForm(Supplier supplier) {
        SupplierForm form = new SupplierForm();
        form.setId(supplier.getId());
        form.setName(supplier.getName());
        form.setContactPerson(supplier.getContactPerson());
        form.setPhone(supplier.getPhone());
        form.setEmail(supplier.getEmail());
        form.setAddress(supplier.getAddress());
        form.setActive(supplier.isActive());
        form.setLinkedUserId(supplier.getLinkedUser() != null ? supplier.getLinkedUser().getId() : null);
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
