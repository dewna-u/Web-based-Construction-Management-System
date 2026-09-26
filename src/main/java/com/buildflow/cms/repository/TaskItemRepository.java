package com.buildflow.cms.repository;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.TaskPriority;
import com.buildflow.cms.enums.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {

    @Override
    @EntityGraph(attributePaths = {"project", "assignee"})
    Optional<TaskItem> findById(Long id);

    @EntityGraph(attributePaths = {"project", "assignee"})
    List<TaskItem> findByProjectOrderByDueDateAsc(Project project);

    @EntityGraph(attributePaths = {"project", "assignee"})
    List<TaskItem> findByProjectIdOrderByDueDateAsc(Long projectId);

    List<TaskItem> findByAssigneeOrderByDueDateAsc(AppUser assignee);

    List<TaskItem> findByStatusOrderByDueDateAsc(TaskStatus status);

    List<TaskItem> findByProjectAndStatusOrderByDueDateAsc(Project project, TaskStatus status);

    List<TaskItem> findByPriorityOrderByDueDateAsc(TaskPriority priority);

    long countByProjectAndStatus(Project project, TaskStatus status);

    boolean existsByProjectId(Long projectId);

    long countByAssigneeAndStatusNot(AppUser assignee, TaskStatus status);

    @EntityGraph(attributePaths = {"project", "assignee"})
    @Query("""
            SELECT t FROM TaskItem t
            WHERE t.dueDate < :today
              AND t.status <> com.buildflow.cms.enums.TaskStatus.COMPLETED
            ORDER BY t.dueDate ASC
            """)
    List<TaskItem> findOverdueTasks(@Param("today") LocalDate today);

    @Query("""
            SELECT t FROM TaskItem t
            WHERE t.project.id = :projectId
              AND t.dueDate < :today
              AND t.status <> com.buildflow.cms.enums.TaskStatus.COMPLETED
            ORDER BY t.dueDate ASC
            """)
    List<TaskItem> findOverdueByProject(@Param("projectId") Long projectId, @Param("today") LocalDate today);

    @Query("""
            SELECT t FROM TaskItem t
            WHERE t.assignee.id = :userId
              AND t.dueDate < :today
              AND t.status <> com.buildflow.cms.enums.TaskStatus.COMPLETED
            ORDER BY t.dueDate ASC
            """)
    List<TaskItem> findOverdueByAssignee(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(t) FROM TaskItem t
            WHERE t.dueDate < :today
              AND t.status <> com.buildflow.cms.enums.TaskStatus.COMPLETED
            """)
    long countOverdueTasks(@Param("today") LocalDate today);
}
