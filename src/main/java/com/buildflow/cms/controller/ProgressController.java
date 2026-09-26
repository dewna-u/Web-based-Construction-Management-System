package com.buildflow.cms.controller;

import com.buildflow.cms.dto.ProgressUpdateForm;
import com.buildflow.cms.entity.ProgressUpdate;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.service.ProgressUpdateService;
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
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressUpdateService progressUpdateService;
    private final ProjectService projectService;

    public ProgressController(ProgressUpdateService progressUpdateService, ProjectService projectService) {
        this.progressUpdateService = progressUpdateService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Long projectId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<ProgressUpdate> updates = projectId != null
                ? progressUpdateService.findByProject(projectId)
                : progressUpdateService.findAll();
        model.addAttribute("page", toPage(updates, pageable));
        model.addAttribute("projectId", projectId);
        model.addAttribute("projects", projectService.findAllActive());
        return "progress/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String createForm(@RequestParam(required = false) Long projectId, Model model) {
        if (!model.containsAttribute("progressUpdateForm")) {
            ProgressUpdateForm form = new ProgressUpdateForm();
            form.setProjectId(projectId);
            form.setUpdateDate(LocalDate.now());
            if (projectId != null) {
                form.setProgressPercentage(projectService.findById(projectId).getProgressPercentage());
            }
            model.addAttribute("progressUpdateForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Add Progress Update");
        return "progress/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String create(
            @Valid @ModelAttribute("progressUpdateForm") ProgressUpdateForm progressUpdateForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Progress Update");
            return "progress/form";
        }
        try {
            ProgressUpdate update = progressUpdateService.create(progressUpdateForm);
            redirectAttributes.addFlashAttribute("successMessage", "Progress update recorded.");
            return "redirect:/progress?projectId=" + update.getProject().getId();
        } catch (BusinessException ex) {
            bindingResult.reject("progressError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Progress Update");
            return "progress/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("progressUpdate", progressUpdateService.findById(id));
        return "progress/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("progressUpdateForm")) {
            model.addAttribute("progressUpdateForm", toForm(progressUpdateService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Progress Update");
        return "progress/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("progressUpdateForm") ProgressUpdateForm progressUpdateForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        progressUpdateForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Progress Update");
            return "progress/form";
        }
        try {
            ProgressUpdate update = progressUpdateService.update(id, progressUpdateForm);
            redirectAttributes.addFlashAttribute("successMessage", "Progress update saved.");
            return "redirect:/progress?projectId=" + update.getProject().getId();
        } catch (BusinessException ex) {
            bindingResult.reject("progressError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Progress Update");
            return "progress/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ProgressUpdate update = progressUpdateService.findById(id);
        Long projectId = update.getProject().getId();
        try {
            progressUpdateService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Progress update deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/progress?projectId=" + projectId;
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
    }

    private ProgressUpdateForm toForm(ProgressUpdate update) {
        ProgressUpdateForm form = new ProgressUpdateForm();
        form.setId(update.getId());
        form.setProjectId(update.getProject() != null ? update.getProject().getId() : null);
        form.setUpdateDate(update.getUpdateDate());
        form.setProgressPercentage(update.getProgressPercentage());
        form.setSummary(update.getSummary());
        form.setWorkCompleted(update.getWorkCompleted());
        form.setNextPlannedWork(update.getNextPlannedWork());
        form.setImagePath(update.getImagePath());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
