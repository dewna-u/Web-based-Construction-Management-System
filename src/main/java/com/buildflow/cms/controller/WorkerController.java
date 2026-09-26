package com.buildflow.cms.controller;

import com.buildflow.cms.dto.WorkerForm;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.EmploymentType;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.enums.WorkerStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.ProjectService;
import com.buildflow.cms.service.UserService;
import com.buildflow.cms.service.WorkerService;
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
@RequestMapping("/workforce")
public class WorkerController {

    private final WorkerService workerService;
    private final ProjectService projectService;
    private final UserService userService;

    public WorkerController(WorkerService workerService, ProjectService projectService, UserService userService) {
        this.workerService = workerService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) WorkerStatus status,
            @RequestParam(required = false) Long projectId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<Worker> workers = workerService.search(search, status, projectId);
        model.addAttribute("page", toPage(workers, pageable));
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("projectId", projectId);
        model.addAttribute("statuses", WorkerStatus.values());
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("activeCount", workerService.countActive());
        return "workforce/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String createForm(Model model) {
        if (!model.containsAttribute("workerForm")) {
            WorkerForm form = new WorkerForm();
            form.setStatus(WorkerStatus.ACTIVE);
            form.setEmploymentType(EmploymentType.FULL_TIME);
            model.addAttribute("workerForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Add Worker");
        return "workforce/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String create(
            @Valid @ModelAttribute("workerForm") WorkerForm workerForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Worker");
            return "workforce/form";
        }
        try {
            Worker worker = workerService.create(workerForm);
            redirectAttributes.addFlashAttribute("successMessage", "Worker created successfully.");
            return "redirect:/workforce/" + worker.getId();
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("workerError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Worker");
            return "workforce/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("worker", workerService.findById(id));
        return "workforce/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("workerForm")) {
            model.addAttribute("workerForm", toForm(workerService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Worker");
        return "workforce/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("workerForm") WorkerForm workerForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        workerForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Worker");
            return "workforce/form";
        }
        try {
            workerService.update(id, workerForm);
            redirectAttributes.addFlashAttribute("successMessage", "Worker updated successfully.");
            return "redirect:/workforce/" + id;
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("workerError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Worker");
            return "workforce/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workerService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Worker deactivated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/workforce/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("successMessage", workerService.delete(id));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/workforce";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("statuses", WorkerStatus.values());
        model.addAttribute("employmentTypes", EmploymentType.values());
        model.addAttribute("linkableUsers", userService.findByRole(Role.CONTRACTOR));
    }

    private WorkerForm toForm(Worker worker) {
        WorkerForm form = new WorkerForm();
        form.setId(worker.getId());
        form.setWorkerCode(worker.getWorkerCode());
        form.setFullName(worker.getFullName());
        form.setNic(worker.getNic());
        form.setPhone(worker.getPhone());
        form.setEmail(worker.getEmail());
        form.setTradeSkill(worker.getTradeSkill());
        form.setEmploymentType(worker.getEmploymentType());
        form.setDailyRate(worker.getDailyRate());
        form.setStatus(worker.getStatus());
        form.setAssignedProjectId(worker.getAssignedProject() != null ? worker.getAssignedProject().getId() : null);
        form.setLinkedUserId(worker.getLinkedUser() != null ? worker.getLinkedUser().getId() : null);
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
