package com.buildflow.cms.service;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Issue;
import com.buildflow.cms.entity.Material;
import com.buildflow.cms.entity.MaterialRequest;
import com.buildflow.cms.entity.Notification;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.IssueSeverity;
import com.buildflow.cms.enums.IssueStatus;
import com.buildflow.cms.enums.MaterialRequestStatus;
import com.buildflow.cms.enums.NotificationType;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.IssueRepository;
import com.buildflow.cms.repository.MaterialRepository;
import com.buildflow.cms.repository.MaterialRequestRepository;
import com.buildflow.cms.repository.NotificationRepository;
import com.buildflow.cms.repository.TaskItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;
    private final TaskItemRepository taskItemRepository;
    private final MaterialRepository materialRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final IssueRepository issueRepository;
    private final ExpenseService expenseService;

    public NotificationService(
            NotificationRepository notificationRepository,
            AppUserRepository appUserRepository,
            TaskItemRepository taskItemRepository,
            MaterialRepository materialRepository,
            MaterialRequestRepository materialRequestRepository,
            IssueRepository issueRepository,
            ExpenseService expenseService) {
        this.notificationRepository = notificationRepository;
        this.appUserRepository = appUserRepository;
        this.taskItemRepository = taskItemRepository;
        this.materialRepository = materialRepository;
        this.materialRequestRepository = materialRequestRepository;
        this.issueRepository = issueRepository;
        this.expenseService = expenseService;
    }

    @Transactional
    public Notification create(AppUser user, String title, String message, String link, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setType(type != null ? type : NotificationType.GENERAL);
        notification.setReadFlag(false);
        return notificationRepository.save(notification);
    }

    public List<Notification> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> listForUser(AppUser user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    @Transactional
    public Notification markRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        notification.setReadFlag(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllReadForUser(userId);
    }

    @Transactional
    public int generateAlerts() {
        int created = 0;
        List<AppUser> adminsAndManagers = appUserRepository.findAll().stream()
                .filter(u -> u.isEnabled()
                        && (u.getRole() == Role.ADMIN || u.getRole() == Role.PROJECT_MANAGER))
                .toList();

        for (TaskItem task : taskItemRepository.findOverdueTasks(LocalDate.now())) {
            String title = "Overdue task";
            String message = "Task \"" + task.getTitle() + "\" is overdue"
                    + (task.getDueDate() != null ? " (due " + task.getDueDate() + ")" : "");
            String link = "/tasks/" + task.getId();
            if (task.getAssignee() != null) {
                created += createIfAbsent(task.getAssignee(), title, message, link, NotificationType.OVERDUE_TASK);
            }
            for (AppUser admin : adminsAndManagers) {
                if (task.getAssignee() != null && admin.getId().equals(task.getAssignee().getId())) {
                    continue;
                }
                created += createIfAbsent(admin, title, message, link, NotificationType.OVERDUE_TASK);
            }
        }

        for (Material material : materialRepository.findLowStockMaterials()) {
            String title = "Low stock alert";
            String message = "Material \"" + material.getName() + "\" (" + material.getMaterialCode()
                    + ") is at or below reorder level";
            String link = "/materials/" + material.getId();
            for (AppUser admin : adminsAndManagers) {
                created += createIfAbsent(admin, title, message, link, NotificationType.LOW_STOCK);
            }
        }

        for (MaterialRequest request : materialRequestRepository.findByStatusOrderByRequestDateDesc(
                MaterialRequestStatus.PENDING)) {
            String title = "Pending material request";
            String message = "Material request #" + request.getId() + " for "
                    + (request.getMaterial() != null ? request.getMaterial().getName() : "material")
                    + " is awaiting approval";
            String link = "/material-requests/" + request.getId();
            for (AppUser admin : adminsAndManagers) {
                created += createIfAbsent(admin, title, message, link, NotificationType.PENDING_REQUEST);
            }
        }

        EnumSet<IssueStatus> closedStatuses = EnumSet.of(IssueStatus.RESOLVED, IssueStatus.CLOSED);
        for (Issue issue : issueRepository.findBySeverityOrderByReportedDateDesc(IssueSeverity.CRITICAL)) {
            if (closedStatuses.contains(issue.getStatus())) {
                continue;
            }
            String title = "Critical issue";
            String message = "Critical issue \"" + issue.getTitle() + "\" requires attention";
            String link = "/issues/" + issue.getId();
            for (AppUser admin : adminsAndManagers) {
                created += createIfAbsent(admin, title, message, link, NotificationType.CRITICAL_ISSUE);
            }
            if (issue.getAssignedTo() != null) {
                created += createIfAbsent(issue.getAssignedTo(), title, message, link, NotificationType.CRITICAL_ISSUE);
            }
        }

        for (Project project : expenseService.findHighBudgetWarningProjects()) {
            String title = "Budget warning";
            String message = "Project \"" + project.getName() + "\" has used "
                    + expenseService.budgetUsagePercentage(project.getId()) + "% of its budget";
            String link = "/projects/" + project.getId() + "/expenses";
            for (AppUser admin : adminsAndManagers) {
                created += createIfAbsent(admin, title, message, link, NotificationType.BUDGET_WARNING);
            }
            if (project.getManager() != null) {
                created += createIfAbsent(project.getManager(), title, message, link, NotificationType.BUDGET_WARNING);
            }
        }

        return created;
    }

    private int createIfAbsent(AppUser user, String title, String message, String link, NotificationType type) {
        boolean exists = notificationRepository.findByUserAndReadFlagFalseOrderByCreatedAtDesc(user).stream()
                .anyMatch(n -> n.getType() == type && link.equals(n.getLink()));
        if (exists) {
            return 0;
        }
        create(user, title, message, link, type);
        return 1;
    }
}
