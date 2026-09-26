package com.buildflow.cms.controller;

import com.buildflow.cms.dto.ProjectForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.ExpenseService;
import com.buildflow.cms.service.IssueService;
import com.buildflow.cms.service.MaterialRequestService;
import com.buildflow.cms.service.MilestoneService;
import com.buildflow.cms.service.ProgressUpdateService;
import com.buildflow.cms.service.ProjectService;
import com.buildflow.cms.service.TaskService;
import com.buildflow.cms.service.UserService;
import com.buildflow.cms.service.WorkerService;
import com.buildflow.cms.util.SecurityUtils;
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
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final MilestoneService milestoneService;
    private final TaskService taskService;
    private final ExpenseService expenseService;
    private final IssueService issueService;
    private final WorkerService workerService;
    private final ProgressUpdateService progressUpdateService;
    private final MaterialRequestService materialRequestService;

    public ProjectController(
            ProjectService projectService,
            UserService userService,
            MilestoneService milestoneService,
            TaskService taskService,
            ExpenseService expenseService,
            IssueService issueService,
            WorkerService workerService,
            ProgressUpdateService progressUpdateService,
            MaterialRequestService materialRequestService) {
        this.projectService = projectService;
        this.userService = userService;
        this.milestoneService = milestoneService;
        this.taskService = taskService;
        this.expenseService = expenseService;
        this.issueService = issueService;
        this.workerService = workerService;
        this.progressUpdateService = progressUpdateService;
        this.materialRequestService = materialRequestService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProjectStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        List<Project> projects = projectService.search(search, status).stream()
                .filter(p -> projectService.canAccess(currentUser, p))
                .toList();
        model.addAttribute("page", toPage(projects, pageable));
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ProjectStatus.values());
        return "projects/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String createForm(Model model) {
        if (!model.containsAttribute("projectForm")) {
            model.addAttribute("projectForm", new ProjectForm());
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Create Project");
        return "projects/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String create(
            @Valid @ModelAttribute("projectForm") ProjectForm projectForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Create Project");
            return "projects/form";
        }
        try {
            Project project = projectService.create(projectForm);
            redirectAttributes.addFlashAttribute("successMessage", "Project created successfully.");
            return "redirect:/projects/" + project.getId();
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("projectError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Create Project");
            return "projects/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        Project project = projectService.findById(id);
        if (!projectService.canAccess(currentUser, project)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You do not have access to this project.");
            return "redirect:/projects";
        }
        model.addAttribute("project", project);
        model.addAttribute("milestones", milestoneService.findByProject(id));
        model.addAttribute("tasks", taskService.findByProject(id));
        model.addAttribute("expenses", expenseService.findByProject(id));
        model.addAttribute("totalExpenses", expenseService.totalByProject(id));
        model.addAttribute("budgetRemaining", projectService.getBudgetRemaining(id));
        model.addAttribute("budgetUsagePercentage", expenseService.budgetUsagePercentage(id));
        model.addAttribute("issues", issueService.findByProject(id));
        model.addAttribute("workers", workerService.search(null, null, id));
        model.addAttribute("progressUpdates", progressUpdateService.findByProject(id));
        model.addAttribute("materialRequests", materialRequestService.findByProject(id));
        return "projects/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("projectForm")) {
            model.addAttribute("projectForm", toForm(projectService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Project");
        return "projects/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("projectForm") ProjectForm projectForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        projectForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Project");
            return "projects/form";
        }
        try {
            projectService.update(id, projectForm);
            redirectAttributes.addFlashAttribute("successMessage", "Project updated successfully.");
            return "redirect:/projects/" + id;
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("projectError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Project");
            return "projects/form";
        }
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String archive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            projectService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Project archived.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/projects";
    }

    @PostMapping("/{id}/unarchive")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String unarchive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            projectService.unarchive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Project restored.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("successMessage", projectService.delete(id));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/projects";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("clients", userService.findByRole(Role.CLIENT));
        model.addAttribute("managers", userService.findByRole(Role.PROJECT_MANAGER));
    }

    private ProjectForm toForm(Project project) {
        ProjectForm form = new ProjectForm();
        form.setId(project.getId());
        form.setName(project.getName());
        form.setProjectCode(project.getProjectCode());
        form.setDescription(project.getDescription());
        form.setLocation(project.getLocation());
        form.setClientId(project.getClient() != null ? project.getClient().getId() : null);
        form.setManagerId(project.getManager() != null ? project.getManager().getId() : null);
        form.setStartDate(project.getStartDate());
        form.setExpectedEndDate(project.getExpectedEndDate());
        form.setActualEndDate(project.getActualEndDate());
        form.setStatus(project.getStatus());
        form.setBudget(project.getBudget());
        form.setProgressPercentage(project.getProgressPercentage());
        form.setArchived(project.isArchived());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
