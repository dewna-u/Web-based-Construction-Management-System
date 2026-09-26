package com.buildflow.cms.service;

import com.buildflow.cms.dto.TaskForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.TaskPriority;
import com.buildflow.cms.enums.TaskStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.repository.TaskItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskItemRepository taskItemRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(
            TaskItemRepository taskItemRepository,
            ProjectRepository projectRepository,
            AppUserRepository appUserRepository) {
        this.taskItemRepository = taskItemRepository;
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public TaskItem create(TaskForm form) {
        TaskItem task = new TaskItem();
        applyForm(task, form);
        return taskItemRepository.save(task);
    }

    @Transactional
    public TaskItem update(Long id, TaskForm form) {
        TaskItem task = findById(id);
        applyForm(task, form);
        return taskItemRepository.save(task);
    }

    @Transactional
    public TaskItem assign(Long taskId, Long assigneeId) {
        TaskItem task = findById(taskId);
        if (assigneeId == null) {
            task.setAssignee(null);
        } else {
            AppUser assignee = appUserRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", assigneeId));
            task.setAssignee(assignee);
        }
        return taskItemRepository.save(task);
    }

    @Transactional
    public TaskItem updateStatus(Long taskId, TaskStatus status) {
        if (status == null) {
            throw new BusinessException("Status is required");
        }
        TaskItem task = findById(taskId);
        task.setStatus(status);
        if (status == TaskStatus.COMPLETED && (task.getCompletionPercentage() == null || task.getCompletionPercentage() < 100)) {
            task.setCompletionPercentage(100);
        }
        return taskItemRepository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        TaskItem task = findById(id);
        taskItemRepository.delete(task);
    }

    public TaskItem findById(Long id) {
        return taskItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    public List<TaskItem> findByProject(Long projectId) {
        return taskItemRepository.findByProjectIdOrderByDueDateAsc(projectId);
    }

    public List<TaskItem> findAll() {
        return taskItemRepository.findAll();
    }

    public List<TaskItem> findOverdue() {
        return taskItemRepository.findOverdueTasks(LocalDate.now());
    }

    public boolean isOverdue(TaskItem task) {
        if (task == null || task.getDueDate() == null) {
            return false;
        }
        return task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED;
    }

    public List<TaskItem> search(
            Long projectId,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId,
            Boolean overdueOnly,
            String keyword) {
        Stream<TaskItem> stream = taskItemRepository.findAll().stream();

        if (projectId != null) {
            stream = stream.filter(t -> t.getProject() != null && projectId.equals(t.getProject().getId()));
        }
        if (status != null) {
            stream = stream.filter(t -> t.getStatus() == status);
        }
        if (priority != null) {
            stream = stream.filter(t -> t.getPriority() == priority);
        }
        if (assigneeId != null) {
            stream = stream.filter(t -> t.getAssignee() != null && assigneeId.equals(t.getAssignee().getId()));
        }
        if (Boolean.TRUE.equals(overdueOnly)) {
            LocalDate today = LocalDate.now();
            stream = stream.filter(t -> t.getDueDate() != null
                    && t.getDueDate().isBefore(today)
                    && t.getStatus() != TaskStatus.COMPLETED);
        }
        if (StringUtils.hasText(keyword)) {
            String q = keyword.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase(Locale.ROOT).contains(q))
                    || (t.getDescription() != null && t.getDescription().toLowerCase(Locale.ROOT).contains(q)));
        }

        return stream
                .sorted((a, b) -> {
                    LocalDate da = a.getDueDate();
                    LocalDate db = b.getDueDate();
                    if (da == null && db == null) {
                        return 0;
                    }
                    if (da == null) {
                        return 1;
                    }
                    if (db == null) {
                        return -1;
                    }
                    return da.compareTo(db);
                })
                .toList();
    }

    public long countOverdue() {
        return taskItemRepository.countOverdueTasks(LocalDate.now());
    }

    private void applyForm(TaskItem task, TaskForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", form.getProjectId()));

        AppUser assignee = null;
        if (form.getAssigneeId() != null) {
            assignee = appUserRepository.findById(form.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", form.getAssigneeId()));
        }

        task.setTitle(form.getTitle().trim());
        task.setDescription(form.getDescription());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setPriority(form.getPriority() != null ? form.getPriority() : TaskPriority.MEDIUM);
        task.setStartDate(form.getStartDate());
        task.setDueDate(form.getDueDate());
        task.setStatus(form.getStatus() != null ? form.getStatus() : TaskStatus.TODO);
        task.setCompletionPercentage(form.getCompletionPercentage() != null ? form.getCompletionPercentage() : 0);
        task.setNotes(form.getNotes());
    }
}
