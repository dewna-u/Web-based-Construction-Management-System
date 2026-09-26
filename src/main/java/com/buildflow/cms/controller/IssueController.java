package com.buildflow.cms.controller;

import com.buildflow.cms.dto.IssueForm;
import com.buildflow.cms.entity.Issue;
import com.buildflow.cms.enums.IssueSeverity;
import com.buildflow.cms.enums.IssueStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.service.IssueService;
import com.buildflow.cms.service.ProjectService;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/issues")
public class IssueController {

    private final IssueService issueService;
    private final ProjectService projectService;
    private final UserService userService;

    public IssueController(IssueService issueService, ProjectService projectService, UserService userService) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) IssueStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<Issue> issues = issueService.filter(projectId, severity, status);
        model.addAttribute("page", toPage(issues, pageable));
        model.addAttribute("projectId", projectId);
        model.addAttribute("severity", severity);
        model.addAttribute("status", status);
        model.addAttribute("severities", IssueSeverity.values());
        model.addAttribute("statuses", IssueStatus.values());
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("openCount", issueService.countOpen());
        model.addAttribute("criticalOpenCount", issueService.countCriticalOpen());
        return "issues/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR', 'CLIENT')")
    public String createForm(@RequestParam(required = false) Long projectId, Model model) {
        if (!model.containsAttribute("issueForm")) {
            IssueForm form = new IssueForm();
            form.setProjectId(projectId);
            form.setReportedDate(LocalDate.now());
            form.setStatus(IssueStatus.OPEN);
            form.setSeverity(IssueSeverity.MEDIUM);
            model.addAttribute("issueForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Report Issue");
        return "issues/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'CONTRACTOR', 'CLIENT')")
    public String create(
            @Valid @ModelAttribute("issueForm") IssueForm issueForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Report Issue");
            return "issues/form";
        }
        try {
            Issue issue = issueService.create(issueForm);
            redirectAttributes.addFlashAttribute("successMessage", "Issue reported successfully.");
            return "redirect:/issues/" + issue.getId();
        } catch (BusinessException ex) {
            bindingResult.reject("issueError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Report Issue");
            return "issues/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("issue", issueService.findById(id));
        model.addAttribute("assignees", userService.findEnabledUsers());
        return "issues/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("issueForm")) {
            model.addAttribute("issueForm", toForm(issueService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Issue");
        return "issues/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("issueForm") IssueForm issueForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        issueForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Issue");
            return "issues/form";
        }
        try {
            issueService.update(id, issueForm);
            redirectAttributes.addFlashAttribute("successMessage", "Issue updated successfully.");
            return "redirect:/issues/" + id;
        } catch (BusinessException ex) {
            bindingResult.reject("issueError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Issue");
            return "issues/form";
        }
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String assign(
            @PathVariable Long id,
            @RequestParam(required = false) Long assigneeId,
            RedirectAttributes redirectAttributes) {
        try {
            issueService.assign(id, assigneeId);
            redirectAttributes.addFlashAttribute("successMessage", "Issue assignment updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String resolve(
            @PathVariable Long id,
            @RequestParam(required = false) String resolutionNotes,
            RedirectAttributes redirectAttributes) {
        try {
            issueService.resolve(id, resolutionNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Issue resolved.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            issueService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Issue deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/issues";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("assignees", userService.findEnabledUsers());
        model.addAttribute("severities", IssueSeverity.values());
        model.addAttribute("statuses", IssueStatus.values());
    }

    private IssueForm toForm(Issue issue) {
        IssueForm form = new IssueForm();
        form.setId(issue.getId());
        form.setProjectId(issue.getProject() != null ? issue.getProject().getId() : null);
        form.setTitle(issue.getTitle());
        form.setDescription(issue.getDescription());
        form.setAssignedToId(issue.getAssignedTo() != null ? issue.getAssignedTo().getId() : null);
        form.setSeverity(issue.getSeverity());
        form.setStatus(issue.getStatus());
        form.setReportedDate(issue.getReportedDate());
        form.setResolvedDate(issue.getResolvedDate());
        form.setResolutionNotes(issue.getResolutionNotes());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
