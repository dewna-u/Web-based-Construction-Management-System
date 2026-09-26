package com.buildflow.cms.repository;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.ProjectStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Override
    @EntityGraph(attributePaths = {"client", "manager"})
    Optional<Project> findById(Long id);

    Optional<Project> findByProjectCodeIgnoreCase(String projectCode);

    boolean existsByProjectCodeIgnoreCase(String projectCode);

    @EntityGraph(attributePaths = {"client", "manager"})
    List<Project> findByArchivedFalseOrderByNameAsc();

    List<Project> findByArchivedTrueOrderByNameAsc();

    @EntityGraph(attributePaths = {"client", "manager"})
    List<Project> findByStatusAndArchivedFalseOrderByNameAsc(ProjectStatus status);

    List<Project> findByClientAndArchivedFalseOrderByNameAsc(AppUser client);

    List<Project> findByManagerAndArchivedFalseOrderByNameAsc(AppUser manager);

    List<Project> findByNameContainingIgnoreCaseAndArchivedFalseOrderByNameAsc(String name);

    long countByArchivedFalse();

    long countByStatusAndArchivedFalse(ProjectStatus status);

    @Query("SELECT COALESCE(SUM(p.budget), 0) FROM Project p WHERE p.archived = false")
    BigDecimal sumActiveBudgets();

    @Query("SELECT COALESCE(SUM(p.budget), 0) FROM Project p WHERE p.id = :projectId")
    BigDecimal sumBudgetByProjectId(@Param("projectId") Long projectId);

    @Query("""
            SELECT p FROM Project p
            WHERE p.archived = false
              AND p.expectedEndDate < CURRENT_DATE
              AND p.status NOT IN (com.buildflow.cms.enums.ProjectStatus.COMPLETED,
                                   com.buildflow.cms.enums.ProjectStatus.CANCELLED)
            ORDER BY p.expectedEndDate ASC
            """)
    List<Project> findOverdueProjects();
}
