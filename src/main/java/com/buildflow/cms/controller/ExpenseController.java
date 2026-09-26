package com.buildflow.cms.controller;

import com.buildflow.cms.dto.ExpenseForm;
import com.buildflow.cms.entity.Expense;
import com.buildflow.cms.enums.ExpenseCategory;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.service.ExpenseService;
import com.buildflow.cms.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ProjectService projectService;

    public ExpenseController(ExpenseService expenseService, ProjectService projectService) {
        this.expenseService = expenseService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<Expense> expenses = expenseService.filter(projectId, category, start, end);
        model.addAttribute("page", toPage(expenses, pageable));
        model.addAttribute("projectId", projectId);
        model.addAttribute("category", category);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("totalExpenses", expenseService.sumAll());
        model.addAttribute("budgetWarnings", expenseService.findHighBudgetWarningProjects());
        if (projectId != null) {
            model.addAttribute("projectTotal", expenseService.totalByProject(projectId));
            model.addAttribute("budgetUsagePercentage", expenseService.budgetUsagePercentage(projectId));
            model.addAttribute("budgetRemaining", projectService.getBudgetRemaining(projectId));
            model.addAttribute("highBudgetUsage", expenseService.isHighBudgetUsage(projectId));
        }
        return "expenses/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String createForm(@RequestParam(required = false) Long projectId, Model model) {
        if (!model.containsAttribute("expenseForm")) {
            ExpenseForm form = new ExpenseForm();
            form.setProjectId(projectId);
            form.setExpenseDate(LocalDate.now());
            model.addAttribute("expenseForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Record Expense");
        return "expenses/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String create(
            @Valid @ModelAttribute("expenseForm") ExpenseForm expenseForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Record Expense");
            return "expenses/form";
        }
        try {
            Expense expense = expenseService.create(expenseForm);
            redirectAttributes.addFlashAttribute("successMessage", "Expense recorded successfully.");
            return "redirect:/expenses?projectId=" + expense.getProject().getId();
        } catch (BusinessException ex) {
            bindingResult.reject("expenseError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Record Expense");
            return "expenses/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("expenseForm")) {
            model.addAttribute("expenseForm", toForm(expenseService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Expense");
        return "expenses/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("expenseForm") ExpenseForm expenseForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        expenseForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Expense");
            return "expenses/form";
        }
        try {
            Expense expense = expenseService.update(id, expenseForm);
            redirectAttributes.addFlashAttribute("successMessage", "Expense updated successfully.");
            return "redirect:/expenses?projectId=" + expense.getProject().getId();
        } catch (BusinessException ex) {
            bindingResult.reject("expenseError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Expense");
            return "expenses/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Expense expense = expenseService.findById(id);
        Long projectId = expense.getProject().getId();
        try {
            expenseService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Expense deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/expenses?projectId=" + projectId;
    }

    private void addFormLookups(Model model) {
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("categories", ExpenseCategory.values());
    }

    private ExpenseForm toForm(Expense expense) {
        ExpenseForm form = new ExpenseForm();
        form.setId(expense.getId());
        form.setProjectId(expense.getProject() != null ? expense.getProject().getId() : null);
        form.setCategory(expense.getCategory());
        form.setDescription(expense.getDescription());
        form.setAmount(expense.getAmount());
        form.setExpenseDate(expense.getExpenseDate());
        form.setReference(expense.getReference());
        form.setNotes(expense.getNotes());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
