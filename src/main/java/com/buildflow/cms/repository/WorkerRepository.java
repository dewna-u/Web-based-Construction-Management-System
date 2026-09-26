package com.buildflow.cms.repository;

import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.WorkerStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    @Override
    @EntityGraph(attributePaths = {"assignedProject", "linkedUser"})
    Optional<Worker> findById(Long id);

    Optional<Worker> findByWorkerCodeIgnoreCase(String workerCode);

    boolean existsByWorkerCodeIgnoreCase(String workerCode);

    @EntityGraph(attributePaths = {"assignedProject"})
    List<Worker> findByStatusOrderByFullNameAsc(WorkerStatus status);

    List<Worker> findByAssignedProjectOrderByFullNameAsc(Project project);

    @EntityGraph(attributePaths = {"assignedProject", "linkedUser"})
    List<Worker> findByAssignedProjectIdOrderByFullNameAsc(Long projectId);

    List<Worker> findByStatusAndAssignedProjectIsNullOrderByFullNameAsc(WorkerStatus status);

    List<Worker> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(String name);

    long countByStatus(WorkerStatus status);

    long countByAssignedProject(Project project);
}
