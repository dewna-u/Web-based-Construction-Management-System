package com.buildflow.cms.repository;

import com.buildflow.cms.entity.Milestone;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.MilestoneStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    @Override
    @EntityGraph(attributePaths = {"project"})
    Optional<Milestone> findById(Long id);

    @EntityGraph(attributePaths = {"project"})
    List<Milestone> findByProjectOrderByDueDateAsc(Project project);

    @EntityGraph(attributePaths = {"project"})
    List<Milestone> findByProjectIdOrderByDueDateAsc(Long projectId);

    List<Milestone> findByStatusOrderByDueDateAsc(MilestoneStatus status);

    List<Milestone> findByProjectAndStatusOrderByDueDateAsc(Project project, MilestoneStatus status);

    long countByProjectAndStatus(Project project, MilestoneStatus status);

    @EntityGraph(attributePaths = {"project"})
    @Query("""
            SELECT m FROM Milestone m
            WHERE m.dueDate < :today
              AND m.status <> com.buildflow.cms.enums.MilestoneStatus.COMPLETED
            ORDER BY m.dueDate ASC
            """)
    List<Milestone> findOverdueMilestones(@Param("today") LocalDate today);

    @Query("""
            SELECT m FROM Milestone m
            WHERE m.project.id = :projectId
              AND m.dueDate < :today
              AND m.status <> com.buildflow.cms.enums.MilestoneStatus.COMPLETED
            ORDER BY m.dueDate ASC
            """)
    List<Milestone> findOverdueByProject(@Param("projectId") Long projectId, @Param("today") LocalDate today);

    @EntityGraph(attributePaths = {"project"})
    @Query("""
            SELECT m FROM Milestone m
            WHERE m.dueDate BETWEEN :from AND :to
              AND m.status <> com.buildflow.cms.enums.MilestoneStatus.COMPLETED
            ORDER BY m.dueDate ASC
            """)
    List<Milestone> findUpcomingBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
