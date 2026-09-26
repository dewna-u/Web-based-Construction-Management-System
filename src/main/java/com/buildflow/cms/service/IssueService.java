package com.buildflow.cms.service;

import com.buildflow.cms.dto.IssueForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Issue;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.IssueSeverity;
import com.buildflow.cms.enums.IssueStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.IssueRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;

    public IssueService(
            IssueRepository issueRepository,
            ProjectRepository projectRepository,
            AppUserRepository appUserRepository) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Issue create(IssueForm form) {
        Issue issue = new Issue();
        applyForm(issue, form);
        AppUser reporter = SecurityUtils.requireCurrentUser();
        issue.setReportedBy(reporter);
        if (issue.getReportedDate() == null) {
            issue.setReportedDate(LocalDate.now());
        }
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue update(Long id, IssueForm form) {
        Issue issue = findById(id);
        applyForm(issue, form);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue assign(Long issueId, Long assigneeId) {
        Issue issue = findById(issueId);
        if (assigneeId == null) {
            issue.setAssignedTo(null);
        } else {
            AppUser assignee = appUserRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", assigneeId));
            issue.setAssignedTo(assignee);
            if (issue.getStatus() == IssueStatus.OPEN) {
                issue.setStatus(IssueStatus.IN_PROGRESS);
            }
        }
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue resolve(Long issueId, String resolutionNotes) {
        Issue issue = findById(issueId);
        if (issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED) {
            throw new BusinessException("Issue is already resolved or closed");
        }
        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolvedDate(LocalDate.now());
        issue.setResolutionNotes(resolutionNotes);
        return issueRepository.save(issue);
    }

    @Transactional
    public void delete(Long id) {
        Issue issue = findById(id);
        issueRepository.delete(issue);
    }

    public Issue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", id));
    }

    public List<Issue> findAll() {
        return issueRepository.findAll();
    }

    public List<Issue> findByProject(Long projectId) {
        return issueRepository.findByProjectIdOrderByReportedDateDesc(projectId);
    }

    public List<Issue> filter(Long projectId, IssueSeverity severity, IssueStatus status) {
        Stream<Issue> stream = issueRepository.findAll().stream();

        if (projectId != null) {
            stream = stream.filter(i -> i.getProject() != null && projectId.equals(i.getProject().getId()));
        }
        if (severity != null) {
            stream = stream.filter(i -> i.getSeverity() == severity);
        }
        if (status != null) {
            stream = stream.filter(i -> i.getStatus() == status);
        }

        return stream
                .sorted((a, b) -> {
                    LocalDate da = a.getReportedDate();
                    LocalDate db = b.getReportedDate();
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

    public long countOpen() {
        return issueRepository.countByStatus(IssueStatus.OPEN)
                + issueRepository.countByStatus(IssueStatus.IN_PROGRESS);
    }

    public long countCriticalOpen() {
        return issueRepository.findBySeverityOrderByReportedDateDesc(IssueSeverity.CRITICAL).stream()
                .filter(i -> i.getStatus() != IssueStatus.RESOLVED && i.getStatus() != IssueStatus.CLOSED)
                .count();
    }

    private void applyForm(Issue issue, IssueForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", form.getProjectId()));

        AppUser assignedTo = null;
        if (form.getAssignedToId() != null) {
            assignedTo = appUserRepository.findById(form.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", form.getAssignedToId()));
        }

        issue.setProject(project);
        issue.setTitle(form.getTitle().trim());
        issue.setDescription(form.getDescription());
        issue.setAssignedTo(assignedTo);
        issue.setSeverity(form.getSeverity() != null ? form.getSeverity() : IssueSeverity.MEDIUM);
        issue.setStatus(form.getStatus() != null ? form.getStatus() : IssueStatus.OPEN);
        issue.setReportedDate(form.getReportedDate() != null ? form.getReportedDate() : LocalDate.now());
        issue.setResolvedDate(form.getResolvedDate());
        issue.setResolutionNotes(form.getResolutionNotes());

        if ((issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED)
                && issue.getResolvedDate() == null) {
            issue.setResolvedDate(LocalDate.now());
        }
    }
}
