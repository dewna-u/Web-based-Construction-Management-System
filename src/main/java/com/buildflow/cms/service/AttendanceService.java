package com.buildflow.cms.service;

import com.buildflow.cms.dto.AttendanceForm;
import com.buildflow.cms.entity.AttendanceRecord;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.AttendanceStatus;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AttendanceRecordRepository;
import com.buildflow.cms.repository.WorkerRepository;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final WorkerRepository workerRepository;

    public AttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            WorkerRepository workerRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.workerRepository = workerRepository;
    }

    @Transactional
    public AttendanceRecord create(AttendanceForm form) {
        Worker worker = workerRepository.findById(form.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", form.getWorkerId()));
        LocalDate date = form.getAttendanceDate();
        if (attendanceRecordRepository.existsByWorkerAndAttendanceDate(worker, date)) {
            throw new DuplicateResourceException(
                    "Attendance already exists for worker " + worker.getWorkerCode() + " on " + date);
        }
        AttendanceRecord record = new AttendanceRecord();
        record.setWorker(worker);
        record.setAttendanceDate(date);
        record.setStatus(form.getStatus() != null ? form.getStatus() : AttendanceStatus.PRESENT);
        record.setNotes(form.getNotes());
        SecurityUtils.getCurrentUser().ifPresent(record::setRecordedBy);
        return attendanceRecordRepository.save(record);
    }

    @Transactional
    public AttendanceRecord update(Long id, AttendanceForm form) {
        AttendanceRecord record = findById(id);
        Worker worker = workerRepository.findById(form.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", form.getWorkerId()));
        LocalDate date = form.getAttendanceDate();

        if ((!record.getWorker().getId().equals(worker.getId()) || !record.getAttendanceDate().equals(date))
                && attendanceRecordRepository.existsByWorkerAndAttendanceDate(worker, date)) {
            throw new DuplicateResourceException(
                    "Attendance already exists for worker " + worker.getWorkerCode() + " on " + date);
        }

        record.setWorker(worker);
        record.setAttendanceDate(date);
        record.setStatus(form.getStatus() != null ? form.getStatus() : AttendanceStatus.PRESENT);
        record.setNotes(form.getNotes());
        return attendanceRecordRepository.save(record);
    }

    @Transactional
    public void delete(Long id) {
        AttendanceRecord record = findById(id);
        attendanceRecordRepository.delete(record);
    }

    public AttendanceRecord findById(Long id) {
        return attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord", id));
    }

    public List<AttendanceRecord> findAll() {
        return attendanceRecordRepository.findAll();
    }

    public List<AttendanceRecord> filter(LocalDate date, Long projectId, Long workerId) {
        Stream<AttendanceRecord> stream = attendanceRecordRepository.findAll().stream();

        if (date != null) {
            stream = stream.filter(a -> date.equals(a.getAttendanceDate()));
        }
        if (workerId != null) {
            stream = stream.filter(a -> a.getWorker() != null && workerId.equals(a.getWorker().getId()));
        }
        if (projectId != null) {
            stream = stream.filter(a -> a.getWorker() != null
                    && a.getWorker().getAssignedProject() != null
                    && projectId.equals(a.getWorker().getAssignedProject().getId()));
        }

        return stream
                .sorted((a, b) -> {
                    int cmp = b.getAttendanceDate().compareTo(a.getAttendanceDate());
                    if (cmp != 0) {
                        return cmp;
                    }
                    String na = a.getWorker() != null ? a.getWorker().getFullName() : "";
                    String nb = b.getWorker() != null ? b.getWorker().getFullName() : "";
                    return String.CASE_INSENSITIVE_ORDER.compare(na, nb);
                })
                .toList();
    }

    public Map<String, Long> summaryForDate(LocalDate date) {
        Map<String, Long> summary = new HashMap<>();
        for (AttendanceStatus status : AttendanceStatus.values()) {
            summary.put(status.name(), attendanceRecordRepository.countByAttendanceDateAndStatus(date, status));
        }
        return summary;
    }

    public Map<String, Long> summaryForProjectAndDate(Long projectId, LocalDate date) {
        Map<String, Long> summary = new HashMap<>();
        for (AttendanceStatus status : AttendanceStatus.values()) {
            summary.put(status.name(),
                    attendanceRecordRepository.countByProjectAndDateAndStatus(projectId, date, status));
        }
        return summary;
    }
}
