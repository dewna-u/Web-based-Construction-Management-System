package com.buildflow.cms.repository;

import com.buildflow.cms.entity.AttendanceRecord;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @Override
    @EntityGraph(attributePaths = {"worker", "worker.assignedProject", "recordedBy"})
    Optional<AttendanceRecord> findById(Long id);

    Optional<AttendanceRecord> findByWorkerAndAttendanceDate(Worker worker, LocalDate attendanceDate);

    boolean existsByWorkerAndAttendanceDate(Worker worker, LocalDate attendanceDate);

    boolean existsByWorkerId(Long workerId);

    @EntityGraph(attributePaths = {"worker", "worker.assignedProject"})
    List<AttendanceRecord> findByAttendanceDateOrderByWorkerFullNameAsc(LocalDate attendanceDate);

    List<AttendanceRecord> findByWorkerOrderByAttendanceDateDesc(Worker worker);

    List<AttendanceRecord> findByWorkerAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Worker worker, LocalDate start, LocalDate end);

    List<AttendanceRecord> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate start, LocalDate end);

    long countByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.worker.assignedProject.id = :projectId
              AND a.attendanceDate = :date
              AND a.status = :status
            """)
    long countByProjectAndDateAndStatus(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date,
            @Param("status") AttendanceStatus status);
}
