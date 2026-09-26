package com.buildflow.cms.controller;

import com.buildflow.cms.dto.MilestoneForm;
import com.buildflow.cms.entity.Milestone;
import com.buildflow.cms.enums.MilestoneStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.service.MilestoneService;
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

import java.util.List;

@Controller
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final ProjectService projectService;

    public MilestoneController(MilestoneService milestoneService, ProjectService projectService) {
        this.milestoneService = milestoneService;
        this.projectService = projectService;
    }

    @GetMapping("/milestones")
    public String listAll(
            @RequestParam(required = false) Long projectId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<Milestone> milestones = projectId != null
                ? milestoneService.findByProject(projectId)
                : milestoneService.findAll();
        model.addAttribute("page", toPage(milestones, pageable));
        model.addAttribute("projectId", projectId);
        model.addAttribute("projects", projectService.findAllActive());
        return "milestones/list";
    }

    @GetMapping("/projects/{projectId}/milestones")
    public String listByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        projectService.findById(projectId);
        model.addAttribute("page", toPage(milestoneService.findByProject(projectId), pageable));
        model.addAttribute("project", projectService.findById(projectId));
        model.addAttribute("projectId", projectId);
        model.addAttribute("projects", projectService.findAllActive());
        return "milestones/list";
    }

    @GetMapping({"/milestones/new", "/projects/{projectId}/milestones/new"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String createForm(
            @PathVariable(required = false) Long projectId,
            @RequestParam(required = false) Long project,
            Model model) {
        Long resolvedProjectId = projectId != null ? projectId : project;
        if (!model.containsAttribute("milestoneForm")) {
            MilestoneForm form = new MilestoneForm();
            form.setProjectId(resolvedProjectId);
            form.setStatus(MilestoneStatus.PENDING);
            model.addAttribute("milestoneForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Create Milestone");
        return "milestones/form";
    }

    @PostMapping({"/milestones", "/projects/{projectId}/milestones"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String create(
            @PathVariable(required = false) Long projectId,
            @Valid @ModelAttribute("milestoneForm") MilestoneForm milestoneForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (projectId != null) {
            milestoneForm.setProjectId(projectId);
        }
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Create Milestone");
            return "milestones/form";
        }
        try {
            Milestone milestone = milestoneService.create(milestoneForm);
            redirectAttributes.addFlashAttribute("successMessage", "Milestone created successfully.");
            return "redirect:/projects/" + milestone.getProject().getId() + "/milestones";
        } catch (BusinessException ex) {
            bindingResult.reject("milestoneError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Create Milestone");
            return "milestones/form";
        }
    }

    @GetMapping("/milestones/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("milestoneForm")) {
            model.addAttribute("milestoneForm", toForm(milestoneService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Milestone");
        return "milestones/form";
    }

    @PostMapping("/milestones/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("milestoneForm") MilestoneForm milestoneForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        milestoneForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Milestone");
            return "milestones/form";
        }
        try {
            Milestone milestone = milestoneService.update(id, milestoneForm);
            redirectAttributes.addFlashAttribute("successMessage", "Milestone updated successfully.");
            return "redirect:/projects/" + milestone.getProject().getId() + "/milestones";
        } catch (BusinessException ex) {
            bindingResult.reject("milestoneError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Milestone");
            return "milestones/form";
        }
    }

    @PostMapping("/milestones/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Milestone milestone = milestoneService.markComplete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Milestone marked as complete.");
            return "redirect:/projects/" + milestone.getProject().getId() + "/milestones";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/milestones";
        }
    }

    @PostMapping("/milestones/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Milestone milestone = milestoneService.findById(id);
        Long projectId = milestone.getProject().getId();
        try {
            milestoneService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Milestone deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/projects/" + projectId + "/milestones";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("statuses", MilestoneStatus.values());
    }

    private MilestoneForm toForm(Milestone milestone) {
        MilestoneForm form = new MilestoneForm();
        form.setId(milestone.getId());
        form.setTitle(milestone.getTitle());
        form.setDescription(milestone.getDescription());
        form.setProjectId(milestone.getProject() != null ? milestone.getProject().getId() : null);
        form.setDueDate(milestone.getDueDate());
        form.setCompletionDate(milestone.getCompletionDate());
        form.setStatus(milestone.getStatus());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
