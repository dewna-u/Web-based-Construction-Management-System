package com.buildflow.cms.service;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.AttendanceRecord;
import com.buildflow.cms.entity.Expense;
import com.buildflow.cms.entity.Issue;
import com.buildflow.cms.entity.Material;
import com.buildflow.cms.entity.MaterialRequest;
import com.buildflow.cms.entity.Milestone;
import com.buildflow.cms.entity.ProgressUpdate;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.Supplier;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.AttendanceStatus;
import com.buildflow.cms.enums.EmploymentType;
import com.buildflow.cms.enums.ExpenseCategory;
import com.buildflow.cms.enums.IssueSeverity;
import com.buildflow.cms.enums.IssueStatus;
import com.buildflow.cms.enums.MaterialRequestStatus;
import com.buildflow.cms.enums.MilestoneStatus;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.enums.TaskPriority;
import com.buildflow.cms.enums.TaskStatus;
import com.buildflow.cms.enums.WorkerStatus;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.AttendanceRecordRepository;
import com.buildflow.cms.repository.ExpenseRepository;
import com.buildflow.cms.repository.IssueRepository;
import com.buildflow.cms.repository.MaterialRepository;
import com.buildflow.cms.repository.MaterialRequestRepository;
import com.buildflow.cms.repository.MilestoneRepository;
import com.buildflow.cms.repository.ProgressUpdateRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.repository.SupplierRepository;
import com.buildflow.cms.repository.TaskItemRepository;
import com.buildflow.cms.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = false)
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);
    private static final String DEMO_PASSWORD = "Demo@123";
    private static final String DEMO_PROJECT_CODE = "DEMO-001";

    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final TaskItemRepository taskItemRepository;
    private final WorkerRepository workerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final ExpenseRepository expenseRepository;
    private final ProgressUpdateRepository progressUpdateRepository;
    private final IssueRepository issueRepository;
    private final PasswordEncoder passwordEncoder;
    private final MaterialService materialService;

    @Value("${app.demo.enabled:false}")
    private boolean demoEnabled;

    public DemoDataService(
            AppUserRepository appUserRepository,
            ProjectRepository projectRepository,
            MilestoneRepository milestoneRepository,
            TaskItemRepository taskItemRepository,
            WorkerRepository workerRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            SupplierRepository supplierRepository,
            MaterialRepository materialRepository,
            MaterialRequestRepository materialRequestRepository,
            ExpenseRepository expenseRepository,
            ProgressUpdateRepository progressUpdateRepository,
            IssueRepository issueRepository,
            PasswordEncoder passwordEncoder,
            MaterialService materialService) {
        this.appUserRepository = appUserRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.taskItemRepository = taskItemRepository;
        this.workerRepository = workerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.supplierRepository = supplierRepository;
        this.materialRepository = materialRepository;
        this.materialRequestRepository = materialRequestRepository;
        this.expenseRepository = expenseRepository;
        this.progressUpdateRepository = progressUpdateRepository;
        this.issueRepository = issueRepository;
        this.passwordEncoder = passwordEncoder;
        this.materialService = materialService;
    }

    public boolean isDemoDataPresent() {
        return projectRepository.existsByProjectCodeIgnoreCase(DEMO_PROJECT_CODE);
    }

    public boolean isDemoEnabled() {
        return demoEnabled;
    }

    @Transactional
    public boolean generateDemoData(AppUser adminUser) {
        if (!demoEnabled) {
            log.info("Demo data generation skipped: app.demo.enabled is false");
            return false;
        }
        if (isDemoDataPresent()) {
            log.info("Demo data already present (project {} exists); skipping", DEMO_PROJECT_CODE);
            return false;
        }

        String passwordHash = passwordEncoder.encode(DEMO_PASSWORD);

        AppUser pm = findOrCreateUser("Priya Manager", "pm@demo.buildflow.local", Role.PROJECT_MANAGER, passwordHash);
        AppUser engineer = findOrCreateUser("Sahan Engineer", "engineer@demo.buildflow.local", Role.SITE_ENGINEER, passwordHash);
        AppUser contractor = findOrCreateUser("Kasun Contractor", "contractor@demo.buildflow.local", Role.CONTRACTOR, passwordHash);
        AppUser supplierUser = findOrCreateUser("Nimal Supplier", "supplier@demo.buildflow.local", Role.SUPPLIER, passwordHash);
        AppUser client = findOrCreateUser("Anoma Client", "client@demo.buildflow.local", Role.CLIENT, passwordHash);
        AppUser client2 = findOrCreateUser("Rohan Client", "client2@demo.buildflow.local", Role.CLIENT, passwordHash);

        Project project1 = createProject(
                "Harbour View Residences",
                DEMO_PROJECT_CODE,
                "Multi-storey residential development with podium parking",
                "Colombo 03",
                client,
                pm,
                ProjectStatus.ACTIVE,
                new BigDecimal("85000000"),
                35,
                LocalDate.now().minusMonths(4),
                LocalDate.now().plusMonths(8));

        Project project2 = createProject(
                "Green Park Office Complex",
                "DEMO-002",
                "Commercial office tower with LEED targets",
                "Rajagiriya",
                client2,
                pm,
                ProjectStatus.PLANNING,
                new BigDecimal("120000000"),
                10,
                LocalDate.now().plusWeeks(2),
                LocalDate.now().plusMonths(18));

        Project project3 = createProject(
                "City Mall Renovation",
                "DEMO-003",
                "Interior fit-out and MEP upgrades for retail mall",
                "Kandy",
                client,
                engineer,
                ProjectStatus.ON_HOLD,
                new BigDecimal("25000000"),
                55,
                LocalDate.now().minusMonths(6),
                LocalDate.now().plusMonths(2));

        createMilestone(project1, "Foundation complete", "All pile caps and ground beams",
                LocalDate.now().minusMonths(1), LocalDate.now().minusWeeks(3), MilestoneStatus.COMPLETED);
        createMilestone(project1, "Structural frame level 5", "Columns and slabs up to level 5",
                LocalDate.now().plusWeeks(3), null, MilestoneStatus.IN_PROGRESS);
        createMilestone(project1, "Roof waterproofing", "Complete roof membrane",
                LocalDate.now().plusMonths(3), null, MilestoneStatus.PENDING);
        createMilestone(project2, "Design freeze", "Issued-for-construction drawings",
                LocalDate.now().plusWeeks(6), null, MilestoneStatus.PENDING);

        createTask(project1, "Pour ground floor slab", "Ready-mix pour and curing",
                engineer, TaskPriority.HIGH, TaskStatus.IN_PROGRESS, 60,
                LocalDate.now().minusWeeks(1), LocalDate.now().plusDays(3));
        createTask(project1, "Install temporary site fencing", "Perimeter security fencing",
                contractor, TaskPriority.MEDIUM, TaskStatus.COMPLETED, 100,
                LocalDate.now().minusMonths(3), LocalDate.now().minusMonths(2));
        createTask(project1, "Submit MEP shop drawings", "HVAC and electrical packages",
                engineer, TaskPriority.URGENT, TaskStatus.TODO, 0,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
        createTask(project2, "Soil investigation review", "Review bore log report",
                pm, TaskPriority.HIGH, TaskStatus.IN_PROGRESS, 40,
                LocalDate.now(), LocalDate.now().plusWeeks(1));
        createTask(project3, "Resolve scaffolding delay", "Coordinate with subcontractor",
                contractor, TaskPriority.HIGH, TaskStatus.BLOCKED, 20,
                LocalDate.now().minusWeeks(2), LocalDate.now().minusDays(1));

        Worker worker1 = createWorker("WRK-DEMO-01", "Sunil Mason", "Mason", EmploymentType.FULL_TIME,
                new BigDecimal("4500"), project1, null);
        Worker worker2 = createWorker("WRK-DEMO-02", "Ruwan Carpenter", "Carpenter", EmploymentType.CONTRACT,
                new BigDecimal("5200"), project1, contractor);
        Worker worker3 = createWorker("WRK-DEMO-03", "Chamara Labour", "General Labour", EmploymentType.DAILY,
                new BigDecimal("2800"), project3, null);
        createWorker("WRK-DEMO-04", "Dilani Electrician", "Electrician", EmploymentType.FULL_TIME,
                new BigDecimal("6000"), null, null);

        createAttendance(worker1, LocalDate.now().minusDays(1), AttendanceStatus.PRESENT, adminUser);
        createAttendance(worker1, LocalDate.now(), AttendanceStatus.PRESENT, adminUser);
        createAttendance(worker2, LocalDate.now(), AttendanceStatus.HALF_DAY, pm);
        createAttendance(worker3, LocalDate.now(), AttendanceStatus.ABSENT, pm);

        Supplier supplier1 = createSupplier("Lanka Cement Distributors", "Kamal Perera",
                "0112345678", "orders@lankacement.demo", "Kelaniya Industrial Zone", supplierUser);
        Supplier supplier2 = createSupplier("Island Steel Traders", "Fathima Noor",
                "0771234567", "sales@islandsteel.demo", "Wattala", null);

        Material cement = createMaterial("MAT-DEMO-CEM", "Ordinary Portland Cement", "Cement", "bags",
                new BigDecimal("120"), new BigDecimal("200"), new BigDecimal("1850"), supplier1, "Warehouse A");
        Material steel = createMaterial("MAT-DEMO-STL", "TMT Bar 16mm", "Steel", "tons",
                new BigDecimal("8"), new BigDecimal("10"), new BigDecimal("285000"), supplier2, "Yard B");
        Material sand = createMaterial("MAT-DEMO-SND", "River Sand", "Aggregates", "cubes",
                new BigDecimal("2"), new BigDecimal("15"), new BigDecimal("12000"), supplier1, "Yard A");
        createMaterial("MAT-DEMO-BRK", "Clay Bricks", "Masonry", "pcs",
                new BigDecimal("5000"), new BigDecimal("2000"), new BigDecimal("28"), supplier2, "Warehouse C");

        createMaterialRequest(project1, cement, engineer, new BigDecimal("50"),
                MaterialRequestStatus.PENDING, "Need for slab pour");
        createMaterialRequest(project1, steel, contractor, new BigDecimal("2"),
                MaterialRequestStatus.APPROVED, "Column reinforcement");
        createMaterialRequest(project1, sand, engineer, new BigDecimal("5"),
                MaterialRequestStatus.DELIVERED, "Backfill and plaster");

        createExpense(project1, ExpenseCategory.MATERIAL, "Cement batch delivery",
                new BigDecimal("92500"), LocalDate.now().minusWeeks(2), "INV-1001", adminUser);
        createExpense(project1, ExpenseCategory.LABOUR, "Weekly labour wages",
                new BigDecimal("340000"), LocalDate.now().minusWeeks(1), "PAY-220", pm);
        createExpense(project1, ExpenseCategory.EQUIPMENT, "Crane hire",
                new BigDecimal("180000"), LocalDate.now().minusDays(5), "EQ-88", pm);
        createExpense(project3, ExpenseCategory.SUBCONTRACT, "Scaffolding subcontract",
                new BigDecimal("21000000"), LocalDate.now().minusMonths(1), "SC-12", adminUser);

        createProgress(project1, LocalDate.now().minusWeeks(2), 25,
                "Foundations handed over; slab prep underway", "Pile caps complete", "Ground floor pour", engineer);
        createProgress(project1, LocalDate.now().minusDays(3), 35,
                "Ground floor formwork in progress", "Formwork 70%", "Concrete pour", engineer);
        createProgress(project3, LocalDate.now().minusMonths(1), 55,
                "Fit-out paused pending scaffolding", "Mall atrium framing", "Resume after scaffold", pm);

        createIssue(project1, "Water seepage at basement wall", "Observed damp patches after rain",
                engineer, pm, IssueSeverity.HIGH, IssueStatus.IN_PROGRESS);
        createIssue(project1, "Missing rebar certificates", "Supplier docs incomplete for 16mm bars",
                contractor, engineer, IssueSeverity.MEDIUM, IssueStatus.OPEN);
        createIssue(project3, "Scaffold collapse near atrium", "Near miss; work stopped",
                contractor, pm, IssueSeverity.CRITICAL, IssueStatus.OPEN);

        log.info("Demo data generated successfully for admin={} (password for demo accounts: {})",
                adminUser != null ? adminUser.getEmail() : "n/a", DEMO_PASSWORD);
        return true;
    }

    private AppUser findOrCreateUser(String fullName, String email, Role role, String passwordHash) {
        return appUserRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setFullName(fullName);
            user.setEmail(email.toLowerCase());
            user.setPasswordHash(passwordHash);
            user.setRole(role);
            user.setEnabled(true);
            return appUserRepository.save(user);
        });
    }

    private Project createProject(
            String name,
            String code,
            String description,
            String location,
            AppUser client,
            AppUser manager,
            ProjectStatus status,
            BigDecimal budget,
            int progress,
            LocalDate start,
            LocalDate end) {
        Project project = new Project();
        project.setName(name);
        project.setProjectCode(code);
        project.setDescription(description);
        project.setLocation(location);
        project.setClient(client);
        project.setManager(manager);
        project.setStatus(status);
        project.setBudget(budget);
        project.setProgressPercentage(progress);
        project.setStartDate(start);
        project.setExpectedEndDate(end);
        project.setArchived(false);
        return projectRepository.save(project);
    }

    private void createMilestone(
            Project project,
            String title,
            String description,
            LocalDate dueDate,
            LocalDate completionDate,
            MilestoneStatus status) {
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(title);
        milestone.setDescription(description);
        milestone.setDueDate(dueDate);
        milestone.setCompletionDate(completionDate);
        milestone.setStatus(status);
        milestoneRepository.save(milestone);
    }

    private TaskItem createTask(
            Project project,
            String title,
            String description,
            AppUser assignee,
            TaskPriority priority,
            TaskStatus status,
            int completion,
            LocalDate start,
            LocalDate due) {
        TaskItem task = new TaskItem();
        task.setProject(project);
        task.setTitle(title);
        task.setDescription(description);
        task.setAssignee(assignee);
        task.setPriority(priority);
        task.setStatus(status);
        task.setCompletionPercentage(completion);
        task.setStartDate(start);
        task.setDueDate(due);
        return taskItemRepository.save(task);
    }

    private Worker createWorker(
            String code,
            String fullName,
            String trade,
            EmploymentType type,
            BigDecimal dailyRate,
            Project project,
            AppUser linkedUser) {
        Worker worker = new Worker();
        worker.setWorkerCode(code);
        worker.setFullName(fullName);
        worker.setTradeSkill(trade);
        worker.setEmploymentType(type);
        worker.setDailyRate(dailyRate);
        worker.setStatus(WorkerStatus.ACTIVE);
        worker.setAssignedProject(project);
        worker.setLinkedUser(linkedUser);
        worker.setPhone("07" + Math.abs(code.hashCode() % 100000000));
        return workerRepository.save(worker);
    }

    private void createAttendance(Worker worker, LocalDate date, AttendanceStatus status, AppUser recordedBy) {
        if (attendanceRecordRepository.existsByWorkerAndAttendanceDate(worker, date)) {
            return;
        }
        AttendanceRecord record = new AttendanceRecord();
        record.setWorker(worker);
        record.setAttendanceDate(date);
        record.setStatus(status);
        record.setRecordedBy(recordedBy);
        attendanceRecordRepository.save(record);
    }

    private Supplier createSupplier(
            String name,
            String contact,
            String phone,
            String email,
            String address,
            AppUser linkedUser) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactPerson(contact);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        supplier.setActive(true);
        supplier.setLinkedUser(linkedUser);
        return supplierRepository.save(supplier);
    }

    private Material createMaterial(
            String code,
            String name,
            String category,
            String unit,
            BigDecimal qty,
            BigDecimal reorder,
            BigDecimal unitCost,
            Supplier supplier,
            String location) {
        Material material = new Material();
        material.setMaterialCode(code);
        material.setName(name);
        material.setCategory(category);
        material.setUnit(unit);
        material.setCurrentQuantity(qty);
        material.setReorderLevel(reorder);
        material.setUnitCost(unitCost);
        material.setSupplier(supplier);
        material.setStorageLocation(location);
        materialService.refreshStockStatus(material);
        return materialRepository.save(material);
    }

    private MaterialRequest createMaterialRequest(
            Project project,
            Material material,
            AppUser requester,
            BigDecimal quantity,
            MaterialRequestStatus status,
            String notes) {
        MaterialRequest request = new MaterialRequest();
        request.setProject(project);
        request.setMaterial(material);
        request.setRequester(requester);
        request.setQuantity(quantity);
        request.setRequestDate(LocalDate.now().minusDays(3));
        request.setRequiredDate(LocalDate.now().plusDays(7));
        request.setStatus(status);
        request.setNotes(notes);
        return materialRequestRepository.save(request);
    }

    private void createExpense(
            Project project,
            ExpenseCategory category,
            String description,
            BigDecimal amount,
            LocalDate date,
            String reference,
            AppUser recordedBy) {
        Expense expense = new Expense();
        expense.setProject(project);
        expense.setCategory(category);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setReference(reference);
        expense.setRecordedBy(recordedBy);
        expenseRepository.save(expense);
    }

    private void createProgress(
            Project project,
            LocalDate date,
            int percentage,
            String summary,
            String workCompleted,
            String nextWork,
            AppUser createdBy) {
        ProgressUpdate update = new ProgressUpdate();
        update.setProject(project);
        update.setUpdateDate(date);
        update.setProgressPercentage(percentage);
        update.setSummary(summary);
        update.setWorkCompleted(workCompleted);
        update.setNextPlannedWork(nextWork);
        update.setCreatedBy(createdBy);
        progressUpdateRepository.save(update);
    }

    private void createIssue(
            Project project,
            String title,
            String description,
            AppUser reportedBy,
            AppUser assignedTo,
            IssueSeverity severity,
            IssueStatus status) {
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setReportedBy(reportedBy);
        issue.setAssignedTo(assignedTo);
        issue.setSeverity(severity);
        issue.setStatus(status);
        issue.setReportedDate(LocalDate.now().minusDays(5));
        issueRepository.save(issue);
    }
}
