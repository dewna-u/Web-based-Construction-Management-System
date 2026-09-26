package com.buildflow.cms.controller;

import com.buildflow.cms.dto.MaterialRequestForm;
import com.buildflow.cms.entity.MaterialRequest;
import com.buildflow.cms.enums.MaterialRequestStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.service.MaterialRequestService;
import com.buildflow.cms.service.MaterialService;
import com.buildflow.cms.service.ProjectService;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/material-requests")
public class MaterialRequestController {

    private final MaterialRequestService materialRequestService;
    private final ProjectService projectService;
    private final MaterialService materialService;

    public MaterialRequestController(
            MaterialRequestService materialRequestService,
            ProjectService projectService,
            MaterialService materialService) {
        this.materialRequestService = materialRequestService;
        this.projectService = projectService;
        this.materialService = materialService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) MaterialRequestStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<MaterialRequest> requests;
        if (status != null) {
            requests = materialRequestService.findByStatus(status);
            if (projectId != null) {
                requests = requests.stream()
                        .filter(r -> r.getProject() != null && projectId.equals(r.getProject().getId()))
                        .toList();
            }
        } else if (projectId != null) {
            requests = materialRequestService.findByProject(projectId);
        } else {
            requests = materialRequestService.findAll();
        }
        model.addAttribute("page", toPage(requests, pageable));
        model.addAttribute("projectId", projectId);
        model.addAttribute("status", status);
        model.addAttribute("statuses", MaterialRequestStatus.values());
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("pendingCount", materialRequestService.countPending());
        return "material-requests/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    public String createForm(@RequestParam(required = false) Long projectId, Model model) {
        if (!model.containsAttribute("materialRequestForm")) {
            MaterialRequestForm form = new MaterialRequestForm();
            form.setProjectId(projectId);
            form.setRequestDate(LocalDate.now());
            form.setStatus(MaterialRequestStatus.PENDING);
            model.addAttribute("materialRequestForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "New Material Request");
        return "material-requests/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    public String create(
            @Valid @ModelAttribute("materialRequestForm") MaterialRequestForm materialRequestForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "New Material Request");
            return "material-requests/form";
        }
        try {
            MaterialRequest request = materialRequestService.create(materialRequestForm);
            redirectAttributes.addFlashAttribute("successMessage", "Material request submitted.");
            return "redirect:/material-requests/" + request.getId();
        } catch (BusinessException ex) {
            bindingResult.reject("requestError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "New Material Request");
            return "material-requests/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("request", materialRequestService.findById(id));
        return "material-requests/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        MaterialRequest request = materialRequestService.findById(id);
        if (request.getStatus() != MaterialRequestStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only pending requests can be edited.");
            return "redirect:/material-requests/" + id;
        }
        if (!model.containsAttribute("materialRequestForm")) {
            model.addAttribute("materialRequestForm", toForm(request));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Material Request");
        return "material-requests/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("materialRequestForm") MaterialRequestForm materialRequestForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        materialRequestForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Material Request");
            return "material-requests/form";
        }
        try {
            materialRequestService.update(id, materialRequestForm);
            redirectAttributes.addFlashAttribute("successMessage", "Material request updated.");
            return "redirect:/material-requests/" + id;
        } catch (BusinessException ex) {
            bindingResult.reject("requestError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Material Request");
            return "material-requests/form";
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            materialRequestService.approve(id);
            redirectAttributes.addFlashAttribute("successMessage", "Material request approved.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/material-requests/" + id;
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String reject(
            @PathVariable Long id,
            @RequestParam String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            materialRequestService.reject(id, rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Material request rejected.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/material-requests/" + id;
    }

    @PostMapping("/{id}/order")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String order(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            materialRequestService.markOrdered(id);
            redirectAttributes.addFlashAttribute("successMessage", "Material request marked as ordered.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/material-requests/" + id;
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String deliver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            materialRequestService.markDelivered(id);
            redirectAttributes.addFlashAttribute("successMessage", "Material request marked as delivered.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/material-requests/" + id;
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            materialRequestService.cancel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Material request cancelled.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/material-requests/" + id;
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("materials", materialService.findAll());
        model.addAttribute("statuses", MaterialRequestStatus.values());
    }

    private MaterialRequestForm toForm(MaterialRequest request) {
        MaterialRequestForm form = new MaterialRequestForm();
        form.setId(request.getId());
        form.setProjectId(request.getProject() != null ? request.getProject().getId() : null);
        form.setMaterialId(request.getMaterial() != null ? request.getMaterial().getId() : null);
        form.setQuantity(request.getQuantity());
        form.setRequestDate(request.getRequestDate());
        form.setRequiredDate(request.getRequiredDate());
        form.setStatus(request.getStatus());
        form.setNotes(request.getNotes());
        form.setRejectionReason(request.getRejectionReason());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
