package com.buildflow.cms.repository;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.MaterialRequest;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.MaterialRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {

    @Override
    @EntityGraph(attributePaths = {"project", "material", "requester", "reviewedBy"})
    Optional<MaterialRequest> findById(Long id);

    @EntityGraph(attributePaths = {"project", "material", "requester"})
    List<MaterialRequest> findByProjectOrderByRequestDateDesc(Project project);

    @EntityGraph(attributePaths = {"project", "material", "requester"})
    List<MaterialRequest> findByProjectIdOrderByRequestDateDesc(Long projectId);

    List<MaterialRequest> findByRequesterOrderByRequestDateDesc(AppUser requester);

    @EntityGraph(attributePaths = {"project", "material", "requester"})
    List<MaterialRequest> findByStatusOrderByRequestDateDesc(MaterialRequestStatus status);

    List<MaterialRequest> findByProjectAndStatusOrderByRequestDateDesc(Project project, MaterialRequestStatus status);

    long countByStatus(MaterialRequestStatus status);

    long countByProjectAndStatus(Project project, MaterialRequestStatus status);

    boolean existsByMaterialId(Long materialId);

    boolean existsByProjectId(Long projectId);
}
