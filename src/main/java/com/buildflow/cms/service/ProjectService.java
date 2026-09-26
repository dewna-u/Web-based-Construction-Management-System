package com.buildflow.cms.service;

import com.buildflow.cms.dto.ProjectForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.ExpenseRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.repository.TaskItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final ExpenseRepository expenseRepository;
    private final TaskItemRepository taskItemRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            AppUserRepository appUserRepository,
            ExpenseRepository expenseRepository,
            TaskItemRepository taskItemRepository) {
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
        this.expenseRepository = expenseRepository;
        this.taskItemRepository = taskItemRepository;
    }

    @Transactional
    public Project create(ProjectForm form) {
        String code = form.getProjectCode().trim();
        if (projectRepository.existsByProjectCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Project", "projectCode", code);
        }
        Project project = new Project();
        applyForm(project, form);
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, ProjectForm form) {
        Project project = findById(id);
        String code = form.getProjectCode().trim();
        if (!project.getProjectCode().equalsIgnoreCase(code)
                && projectRepository.existsByProjectCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Project", "projectCode", code);
        }
        applyForm(project, form);
        return projectRepository.save(project);
    }

    @Transactional
    public Project archive(Long id) {
        Project project = findById(id);
        project.setArchived(true);
        return projectRepository.save(project);
    }

    @Transactional
    public Project unarchive(Long id) {
        Project project = findById(id);
        project.setArchived(false);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProgress(Long id, Integer progressPercentage) {
        if (progressPercentage == null || progressPercentage < 0 || progressPercentage > 100) {
            throw new BusinessException("Progress percentage must be between 0 and 100");
        }
        Project project = findById(id);
        project.setProgressPercentage(progressPercentage);
        return projectRepository.save(project);
    }

    /**
     * Soft-deletes by archiving. Hard delete is unsafe once related site data exists.
     *
     * @return user-facing result message
     */
    @Transactional
    public String delete(Long id) {
        Project project = findById(id);
        project.setArchived(true);
        if (project.getStatus() == ProjectStatus.ACTIVE || project.getStatus() == ProjectStatus.PLANNING) {
            project.setStatus(ProjectStatus.ON_HOLD);
        }
        projectRepository.save(project);
        return "Project archived successfully. Related tasks and history were kept.";
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    public List<Project> findAllActive() {
        return projectRepository.findByArchivedFalseOrderByNameAsc();
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public List<Project> search(String name, ProjectStatus status) {
        List<Project> projects;
        if (status != null) {
            projects = projectRepository.findByStatusAndArchivedFalseOrderByNameAsc(status);
        } else {
            projects = projectRepository.findByArchivedFalseOrderByNameAsc();
        }
        if (!StringUtils.hasText(name)) {
            return projects;
        }
        String q = name.trim().toLowerCase(Locale.ROOT);
        return projects.stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(q))
                        || (p.getProjectCode() != null && p.getProjectCode().toLowerCase(Locale.ROOT).contains(q)))
                .toList();
    }

    public BigDecimal getTotalExpenses(Long projectId) {
        return expenseRepository.sumAmountByProjectId(projectId);
    }

    public BigDecimal getBudgetRemaining(Long projectId) {
        Project project = findById(projectId);
        BigDecimal budget = project.getBudget() != null ? project.getBudget() : BigDecimal.ZERO;
        BigDecimal spent = expenseRepository.sumAmountByProjectId(projectId);
        return budget.subtract(spent != null ? spent : BigDecimal.ZERO);
    }

    public boolean canAccess(AppUser user, Project project) {
        if (user == null || project == null) {
            return false;
        }
        Role role = user.getRole();
        if (role == Role.ADMIN || role == Role.PROJECT_MANAGER) {
            return true;
        }
        if (role == Role.CLIENT) {
            return project.getClient() != null && project.getClient().getId().equals(user.getId());
        }
        if (project.getManager() != null && project.getManager().getId().equals(user.getId())) {
            return true;
        }
        if (role == Role.SITE_ENGINEER || role == Role.CONTRACTOR) {
            List<TaskItem> tasks = taskItemRepository.findByProjectOrderByDueDateAsc(project);
            return tasks.stream()
                    .anyMatch(t -> t.getAssignee() != null && t.getAssignee().getId().equals(user.getId()));
        }
        if (role == Role.SUPPLIER) {
            return false;
        }
        return false;
    }

    public List<Project> findAccessibleProjects(AppUser user) {
        return findAllActive().stream()
                .filter(p -> canAccess(user, p))
                .toList();
    }

    public long countActive() {
        return projectRepository.countByArchivedFalse();
    }

    private void applyForm(Project project, ProjectForm form) {
        if (form.getStartDate() != null && form.getExpectedEndDate() != null
                && form.getExpectedEndDate().isBefore(form.getStartDate())) {
            throw new BusinessException("Expected end date must be on or after the start date");
        }

        AppUser client = appUserRepository.findById(form.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", form.getClientId()));
        if (client.getRole() != Role.CLIENT) {
            throw new BusinessException("Selected client must have CLIENT role");
        }

        AppUser manager = null;
        if (form.getManagerId() != null) {
            manager = appUserRepository.findById(form.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", form.getManagerId()));
        }

        project.setName(form.getName().trim());
        project.setProjectCode(form.getProjectCode().trim());
        project.setDescription(form.getDescription());
        project.setLocation(form.getLocation());
        project.setClient(client);
        project.setManager(manager);
        project.setStartDate(form.getStartDate());
        project.setExpectedEndDate(form.getExpectedEndDate());
        project.setActualEndDate(form.getActualEndDate());
        project.setStatus(form.getStatus() != null ? form.getStatus() : ProjectStatus.PLANNING);
        project.setBudget(form.getBudget());
        project.setProgressPercentage(form.getProgressPercentage() != null ? form.getProgressPercentage() : 0);
        project.setArchived(form.isArchived());
    }
}
