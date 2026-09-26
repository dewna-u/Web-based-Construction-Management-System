package com.buildflow.cms.service;

import com.buildflow.cms.entity.Issue;
import com.buildflow.cms.entity.MaterialRequest;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.IssueSeverity;
import com.buildflow.cms.enums.IssueStatus;
import com.buildflow.cms.enums.MaterialRequestStatus;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.TaskStatus;
import com.buildflow.cms.enums.WorkerStatus;
import com.buildflow.cms.repository.ExpenseRepository;
import com.buildflow.cms.repository.IssueRepository;
import com.buildflow.cms.repository.MaterialRepository;
import com.buildflow.cms.repository.MaterialRequestRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.repository.TaskItemRepository;
import com.buildflow.cms.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskItemRepository taskItemRepository;
    private final WorkerRepository workerRepository;
    private final MaterialRepository materialRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final IssueRepository issueRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardService(
            ProjectRepository projectRepository,
            TaskItemRepository taskItemRepository,
            WorkerRepository workerRepository,
            MaterialRepository materialRepository,
            MaterialRequestRepository materialRequestRepository,
            IssueRepository issueRepository,
            ExpenseRepository expenseRepository) {
        this.projectRepository = projectRepository;
        this.taskItemRepository = taskItemRepository;
        this.workerRepository = workerRepository;
        this.materialRepository = materialRepository;
        this.materialRequestRepository = materialRequestRepository;
        this.issueRepository = issueRepository;
        this.expenseRepository = expenseRepository;
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        long activeProjects = projectRepository.countByStatusAndArchivedFalse(ProjectStatus.ACTIVE);
        long totalActiveProjects = projectRepository.countByArchivedFalse();
        long overdueTasks = taskItemRepository.countOverdueTasks(LocalDate.now());
        long activeWorkers = workerRepository.countByStatus(WorkerStatus.ACTIVE);
        long lowStock = materialRepository.countLowStockMaterials();
        long pendingRequests = materialRequestRepository.countByStatus(MaterialRequestStatus.PENDING);
        long openIssues = issueRepository.countByStatus(IssueStatus.OPEN)
                + issueRepository.countByStatus(IssueStatus.IN_PROGRESS);
        BigDecimal totalExpenses = expenseRepository.sumAllAmounts();
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        data.put("activeProjects", activeProjects);
        data.put("totalActiveProjects", totalActiveProjects);
        data.put("totalTasks", taskItemRepository.count());
        data.put("overdueTasks", overdueTasks);
        data.put("activeWorkers", activeWorkers);
        data.put("lowStockMaterials", lowStock);
        data.put("pendingRequests", pendingRequests);
        data.put("openIssues", openIssues);
        data.put("totalExpenses", totalExpenses);

        data.put("tasksByStatus", buildTasksByStatus());
        data.put("projectsByStatus", buildProjectsByStatus());
        data.put("issuesBySeverity", buildIssuesBySeverity());

        data.put("recentProjects", recentProjects(5));
        data.put("recentTasks", recentTasks(5));
        data.put("recentIssues", recentIssues(5));
        data.put("recentRequests", recentRequests(5));
        data.put("overdueTaskList", taskItemRepository.findOverdueTasks(LocalDate.now()).stream().limit(5).toList());
        data.put("lowStockList", materialRepository.findLowStockMaterials().stream().limit(5).toList());

        return data;
    }

    private Map<String, Long> buildTasksByStatus() {
        Map<String, Long> map = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            map.put(status.name(), 0L);
        }
        for (TaskItem task : taskItemRepository.findAll()) {
            if (task.getStatus() != null) {
                map.merge(task.getStatus().name(), 1L, Long::sum);
            }
        }
        return map;
    }

    private Map<String, Long> buildProjectsByStatus() {
        Map<String, Long> map = new HashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            map.put(status.name(), projectRepository.countByStatusAndArchivedFalse(status));
        }
        return map;
    }

    private Map<String, Long> buildIssuesBySeverity() {
        Map<String, Long> map = new HashMap<>();
        for (IssueSeverity severity : IssueSeverity.values()) {
            map.put(severity.name(), 0L);
        }
        for (Issue issue : issueRepository.findAll()) {
            if (issue.getSeverity() != null) {
                map.merge(issue.getSeverity().name(), 1L, Long::sum);
            }
        }
        return map;
    }

    private List<Project> recentProjects(int limit) {
        return projectRepository.findByArchivedFalseOrderByNameAsc().stream()
                .sorted(Comparator.comparing(Project::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    private List<TaskItem> recentTasks(int limit) {
        return taskItemRepository.findAll().stream()
                .sorted(Comparator.comparing(TaskItem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    private List<Issue> recentIssues(int limit) {
        return issueRepository.findAll().stream()
                .sorted(Comparator.comparing(Issue::getReportedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    private List<MaterialRequest> recentRequests(int limit) {
        return materialRequestRepository.findAll().stream()
                .sorted(Comparator.comparing(MaterialRequest::getRequestDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }
}
