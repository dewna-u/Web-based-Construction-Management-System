package com.buildflow.cms.service;

import com.buildflow.cms.dto.ExpenseForm;
import com.buildflow.cms.entity.Expense;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.ExpenseCategory;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.ExpenseRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    public static final BigDecimal BUDGET_WARNING_THRESHOLD = new BigDecimal("80");

    private final ExpenseRepository expenseRepository;
    private final ProjectRepository projectRepository;

    public ExpenseService(ExpenseRepository expenseRepository, ProjectRepository projectRepository) {
        this.expenseRepository = expenseRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Expense create(ExpenseForm form) {
        Expense expense = new Expense();
        applyForm(expense, form);
        SecurityUtils.getCurrentUser().ifPresent(expense::setRecordedBy);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(Long id, ExpenseForm form) {
        Expense expense = findById(id);
        applyForm(expense, form);
        return expenseRepository.save(expense);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = findById(id);
        expenseRepository.delete(expense);
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public List<Expense> findByProject(Long projectId) {
        return expenseRepository.findByProjectIdOrderByExpenseDateDesc(projectId);
    }

    public List<Expense> filter(Long projectId, ExpenseCategory category, LocalDate start, LocalDate end) {
        Stream<Expense> stream = expenseRepository.findAll().stream();

        if (projectId != null) {
            stream = stream.filter(e -> e.getProject() != null && projectId.equals(e.getProject().getId()));
        }
        if (category != null) {
            stream = stream.filter(e -> e.getCategory() == category);
        }
        if (start != null) {
            stream = stream.filter(e -> e.getExpenseDate() != null && !e.getExpenseDate().isBefore(start));
        }
        if (end != null) {
            stream = stream.filter(e -> e.getExpenseDate() != null && !e.getExpenseDate().isAfter(end));
        }

        return stream
                .sorted((a, b) -> {
                    LocalDate da = a.getExpenseDate();
                    LocalDate db = b.getExpenseDate();
                    if (da == null && db == null) {
                        return 0;
                    }
                    if (da == null) {
                        return 1;
                    }
                    if (db == null) {
                        return -1;
                    }
                    return db.compareTo(da);
                })
                .toList();
    }

    public BigDecimal totalByProject(Long projectId) {
        BigDecimal total = expenseRepository.sumAmountByProjectId(projectId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal budgetUsagePercentage(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        BigDecimal budget = project.getBudget();
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal spent = totalByProject(projectId);
        return spent.multiply(new BigDecimal("100")).divide(budget, 2, RoundingMode.HALF_UP);
    }

    public boolean isHighBudgetUsage(Long projectId) {
        return budgetUsagePercentage(projectId).compareTo(BUDGET_WARNING_THRESHOLD) >= 0;
    }

    public List<Project> findHighBudgetWarningProjects() {
        List<Project> warnings = new ArrayList<>();
        for (Project project : projectRepository.findByArchivedFalseOrderByNameAsc()) {
            if (project.getBudget() == null || project.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (budgetUsagePercentage(project.getId()).compareTo(BUDGET_WARNING_THRESHOLD) >= 0) {
                warnings.add(project);
            }
        }
        return warnings;
    }

    public BigDecimal sumAll() {
        BigDecimal total = expenseRepository.sumAllAmounts();
        return total != null ? total : BigDecimal.ZERO;
    }

    private void applyForm(Expense expense, ExpenseForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", form.getProjectId()));
        if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        expense.setProject(project);
        expense.setCategory(form.getCategory());
        expense.setDescription(form.getDescription().trim());
        expense.setAmount(form.getAmount());
        expense.setExpenseDate(form.getExpenseDate());
        expense.setReference(form.getReference());
        expense.setNotes(form.getNotes());
    }
}
