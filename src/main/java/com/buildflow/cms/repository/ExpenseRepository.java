package com.buildflow.cms.repository;

import com.buildflow.cms.entity.Expense;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Override
    @EntityGraph(attributePaths = {"project", "recordedBy"})
    Optional<Expense> findById(Long id);

    @EntityGraph(attributePaths = {"project"})
    List<Expense> findByProjectOrderByExpenseDateDesc(Project project);

    @EntityGraph(attributePaths = {"project"})
    List<Expense> findByProjectIdOrderByExpenseDateDesc(Long projectId);

    List<Expense> findByCategoryOrderByExpenseDateDesc(ExpenseCategory category);

    List<Expense> findByProjectAndExpenseDateBetweenOrderByExpenseDateDesc(
            Project project, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.project.id = :projectId")
    BigDecimal sumAmountByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.project = :project")
    BigDecimal sumAmountByProject(@Param("project") Project project);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM Expense e
            WHERE e.project.id = :projectId AND e.category = :category
            """)
    BigDecimal sumAmountByProjectIdAndCategory(
            @Param("projectId") Long projectId,
            @Param("category") ExpenseCategory category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAllAmounts();

    long countByProject(Project project);
}
