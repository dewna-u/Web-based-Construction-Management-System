package com.buildflow.cms.controller;

import com.buildflow.cms.dto.TaskForm;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.TaskPriority;
import com.buildflow.cms.enums.TaskStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.service.ProjectService;
import com.buildflow.cms.service.TaskService;
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
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskController(TaskService taskService, ProjectService projectService, UserService userService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Boolean overdueOnly,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<TaskItem> tasks = taskService.search(projectId, status, priority, assigneeId, overdueOnly, search);
        model.addAttribute("page", toPage(tasks, pageable));
        model.addAttribute("projectId", projectId);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("assigneeId", assigneeId);
        model.addAttribute("overdueOnly", overdueOnly);
        model.addAttribute("search", search);
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("assignees", userService.findEnabledUsers());
        model.addAttribute("overdueCount", taskService.countOverdue());
        return "tasks/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String createForm(@RequestParam(required = false) Long projectId, Model model) {
        if (!model.containsAttribute("taskForm")) {
            TaskForm form = new TaskForm();
            form.setProjectId(projectId);
            form.setStatus(TaskStatus.TODO);
            form.setPriority(TaskPriority.MEDIUM);
            model.addAttribute("taskForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Create Task");
        return "tasks/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String create(
            @Valid @ModelAttribute("taskForm") TaskForm taskForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Create Task");
            return "tasks/form";
        }
        try {
            TaskItem task = taskService.create(taskForm);
            redirectAttributes.addFlashAttribute("successMessage", "Task created successfully.");
            return "redirect:/tasks/" + task.getId();
        } catch (BusinessException ex) {
            bindingResult.reject("taskError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Create Task");
            return "tasks/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        TaskItem task = taskService.findById(id);
        model.addAttribute("task", task);
        model.addAttribute("overdue", taskService.isOverdue(task));
        return "tasks/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", toForm(taskService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Task");
        return "tasks/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("taskForm") TaskForm taskForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        taskForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Task");
            return "tasks/form";
        }
        try {
            taskService.update(id, taskForm);
            redirectAttributes.addFlashAttribute("successMessage", "Task updated successfully.");
            return "redirect:/tasks/" + id;
        } catch (BusinessException ex) {
            bindingResult.reject("taskError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Task");
            return "tasks/form";
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR')")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            taskService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Task status updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            taskService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Task deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tasks";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("assignees", userService.findEnabledUsers());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
    }

    private TaskForm toForm(TaskItem task) {
        TaskForm form = new TaskForm();
        form.setId(task.getId());
        form.setTitle(task.getTitle());
        form.setDescription(task.getDescription());
        form.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        form.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        form.setPriority(task.getPriority());
        form.setStartDate(task.getStartDate());
        form.setDueDate(task.getDueDate());
        form.setStatus(task.getStatus());
        form.setCompletionPercentage(task.getCompletionPercentage());
        form.setNotes(task.getNotes());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
