package com.buildflow.cms.repository;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Issue;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.IssueSeverity;
import com.buildflow.cms.enums.IssueStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Override
    @EntityGraph(attributePaths = {"project", "reportedBy", "assignedTo"})
    Optional<Issue> findById(Long id);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Issue> findByProjectOrderByReportedDateDesc(Project project);

    @EntityGraph(attributePaths = {"project", "assignedTo"})
    List<Issue> findByProjectIdOrderByReportedDateDesc(Long projectId);

    List<Issue> findByStatusOrderByReportedDateDesc(IssueStatus status);

    List<Issue> findBySeverityOrderByReportedDateDesc(IssueSeverity severity);

    List<Issue> findByAssignedToOrderByReportedDateDesc(AppUser assignedTo);

    List<Issue> findByProjectAndStatusOrderByReportedDateDesc(Project project, IssueStatus status);

    List<Issue> findBySeverityAndStatusNotOrderByReportedDateDesc(IssueSeverity severity, IssueStatus status);

    long countByStatus(IssueStatus status);

    long countBySeverityAndStatusNot(IssueSeverity severity, IssueStatus status);

    long countByProjectAndStatus(Project project, IssueStatus status);
}
