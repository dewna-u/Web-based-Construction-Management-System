package com.buildflow.cms.service;

import com.buildflow.cms.dto.WorkerForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.EmploymentType;
import com.buildflow.cms.enums.WorkerStatus;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.AttendanceRecordRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public WorkerService(
            WorkerRepository workerRepository,
            ProjectRepository projectRepository,
            AppUserRepository appUserRepository,
            AttendanceRecordRepository attendanceRecordRepository) {
        this.workerRepository = workerRepository;
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Transactional
    public Worker create(WorkerForm form) {
        String code = form.getWorkerCode().trim();
        if (workerRepository.existsByWorkerCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Worker", "workerCode", code);
        }
        Worker worker = new Worker();
        applyForm(worker, form);
        return workerRepository.save(worker);
    }

    @Transactional
    public Worker update(Long id, WorkerForm form) {
        Worker worker = findById(id);
        String code = form.getWorkerCode().trim();
        if (!worker.getWorkerCode().equalsIgnoreCase(code)
                && workerRepository.existsByWorkerCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Worker", "workerCode", code);
        }
        applyForm(worker, form);
        return workerRepository.save(worker);
    }

    @Transactional
    public Worker deactivate(Long id) {
        Worker worker = findById(id);
        worker.setStatus(WorkerStatus.INACTIVE);
        return workerRepository.save(worker);
    }

    /**
     * @return user-facing result message
     */
    @Transactional
    public String delete(Long id) {
        Worker worker = findById(id);
        if (attendanceRecordRepository.existsByWorkerId(id)) {
            worker.setStatus(WorkerStatus.INACTIVE);
            workerRepository.save(worker);
            return "Worker has attendance records, so they were deactivated instead of deleted.";
        }
        workerRepository.delete(worker);
        return "Worker deleted.";
    }

    public Worker findById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", id));
    }

    public List<Worker> findAll() {
        return workerRepository.findAll();
    }

    public List<Worker> findActive() {
        return workerRepository.findByStatusOrderByFullNameAsc(WorkerStatus.ACTIVE);
    }

    public List<Worker> search(String keyword, WorkerStatus status, Long projectId) {
        Stream<Worker> stream = workerRepository.findAll().stream();

        if (status != null) {
            stream = stream.filter(w -> w.getStatus() == status);
        }
        if (projectId != null) {
            stream = stream.filter(w -> w.getAssignedProject() != null
                    && projectId.equals(w.getAssignedProject().getId()));
        }
        if (StringUtils.hasText(keyword)) {
            String q = keyword.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(w -> (w.getFullName() != null && w.getFullName().toLowerCase(Locale.ROOT).contains(q))
                    || (w.getWorkerCode() != null && w.getWorkerCode().toLowerCase(Locale.ROOT).contains(q))
                    || (w.getTradeSkill() != null && w.getTradeSkill().toLowerCase(Locale.ROOT).contains(q))
                    || (w.getNic() != null && w.getNic().toLowerCase(Locale.ROOT).contains(q)));
        }

        return stream
                .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                        a.getFullName() != null ? a.getFullName() : "",
                        b.getFullName() != null ? b.getFullName() : ""))
                .toList();
    }

    public long countActive() {
        return workerRepository.countByStatus(WorkerStatus.ACTIVE);
    }

    private void applyForm(Worker worker, WorkerForm form) {
        Project project = null;
        if (form.getAssignedProjectId() != null) {
            project = projectRepository.findById(form.getAssignedProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", form.getAssignedProjectId()));
        }
        AppUser linkedUser = null;
        if (form.getLinkedUserId() != null) {
            linkedUser = appUserRepository.findById(form.getLinkedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", form.getLinkedUserId()));
        }

        worker.setWorkerCode(form.getWorkerCode().trim());
        worker.setFullName(form.getFullName().trim());
        worker.setNic(form.getNic());
        worker.setPhone(form.getPhone());
        worker.setEmail(form.getEmail());
        worker.setTradeSkill(form.getTradeSkill());
        worker.setEmploymentType(form.getEmploymentType() != null ? form.getEmploymentType() : EmploymentType.FULL_TIME);
        worker.setDailyRate(form.getDailyRate());
        worker.setStatus(form.getStatus() != null ? form.getStatus() : WorkerStatus.ACTIVE);
        worker.setAssignedProject(project);
        worker.setLinkedUser(linkedUser);
    }
}
