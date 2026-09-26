package com.buildflow.cms.repository;

import com.buildflow.cms.entity.ProgressUpdate;
import com.buildflow.cms.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressUpdateRepository extends JpaRepository<ProgressUpdate, Long> {

    @Override
    @EntityGraph(attributePaths = {"project", "createdBy"})
    Optional<ProgressUpdate> findById(Long id);

    @EntityGraph(attributePaths = {"project", "createdBy"})
    List<ProgressUpdate> findByProjectOrderByUpdateDateDesc(Project project);

    @EntityGraph(attributePaths = {"project", "createdBy"})
    List<ProgressUpdate> findByProjectIdOrderByUpdateDateDesc(Long projectId);

    Optional<ProgressUpdate> findFirstByProjectOrderByUpdateDateDesc(Project project);

    long countByProject(Project project);
}
