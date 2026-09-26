package com.buildflow.cms.service;

import com.buildflow.cms.dto.ProgressUpdateForm;
import com.buildflow.cms.entity.ProgressUpdate;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.ProgressUpdateRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProgressUpdateService {

    private final ProgressUpdateRepository progressUpdateRepository;
    private final ProjectRepository projectRepository;

    public ProgressUpdateService(
            ProgressUpdateRepository progressUpdateRepository,
            ProjectRepository projectRepository) {
        this.progressUpdateRepository = progressUpdateRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProgressUpdate create(ProgressUpdateForm form) {
        return create(form, true);
    }

    @Transactional
    public ProgressUpdate create(ProgressUpdateForm form, boolean syncProjectProgress) {
        ProgressUpdate update = new ProgressUpdate();
        applyForm(update, form);
        SecurityUtils.getCurrentUser().ifPresent(update::setCreatedBy);
        ProgressUpdate saved = progressUpdateRepository.save(update);
        if (syncProjectProgress) {
            syncProgress(saved.getProject(), saved.getProgressPercentage());
        }
        return saved;
    }

    @Transactional
    public ProgressUpdate update(Long id, ProgressUpdateForm form) {
        return update(id, form, true);
    }

    @Transactional
    public ProgressUpdate update(Long id, ProgressUpdateForm form, boolean syncProjectProgress) {
        ProgressUpdate update = findById(id);
        applyForm(update, form);
        ProgressUpdate saved = progressUpdateRepository.save(update);
        if (syncProjectProgress) {
            syncProgress(saved.getProject(), saved.getProgressPercentage());
        }
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        ProgressUpdate update = findById(id);
        progressUpdateRepository.delete(update);
    }

    public ProgressUpdate findById(Long id) {
        return progressUpdateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProgressUpdate", id));
    }

    public List<ProgressUpdate> findByProject(Long projectId) {
        return progressUpdateRepository.findByProjectIdOrderByUpdateDateDesc(projectId);
    }

    public List<ProgressUpdate> findAll() {
        return progressUpdateRepository.findAll();
    }

    private void applyForm(ProgressUpdate update, ProgressUpdateForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", form.getProjectId()));
        Integer pct = form.getProgressPercentage();
        if (pct == null || pct < 0 || pct > 100) {
            throw new BusinessException("Progress percentage must be between 0 and 100");
        }
        update.setProject(project);
        update.setUpdateDate(form.getUpdateDate());
        update.setProgressPercentage(pct);
        update.setSummary(form.getSummary().trim());
        update.setWorkCompleted(form.getWorkCompleted());
        update.setNextPlannedWork(form.getNextPlannedWork());
        update.setImagePath(form.getImagePath());
    }

    private void syncProgress(Project project, Integer progressPercentage) {
        project.setProgressPercentage(progressPercentage);
        projectRepository.save(project);
    }
}
