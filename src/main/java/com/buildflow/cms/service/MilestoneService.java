package com.buildflow.cms.service;

import com.buildflow.cms.dto.MilestoneForm;
import com.buildflow.cms.entity.Milestone;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.MilestoneStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.MilestoneRepository;
import com.buildflow.cms.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Milestone create(MilestoneForm form) {
        Milestone milestone = new Milestone();
        applyForm(milestone, form);
        return milestoneRepository.save(milestone);
    }

    @Transactional
    public Milestone update(Long id, MilestoneForm form) {
        Milestone milestone = findById(id);
        applyForm(milestone, form);
        return milestoneRepository.save(milestone);
    }

    @Transactional
    public Milestone markComplete(Long id) {
        Milestone milestone = findById(id);
        if (milestone.getStatus() == MilestoneStatus.COMPLETED) {
            throw new BusinessException("Milestone is already completed");
        }
        milestone.setStatus(MilestoneStatus.COMPLETED);
        milestone.setCompletionDate(LocalDate.now());
        return milestoneRepository.save(milestone);
    }

    @Transactional
    public void delete(Long id) {
        Milestone milestone = findById(id);
        milestoneRepository.delete(milestone);
    }

    public Milestone findById(Long id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", id));
    }

    public List<Milestone> findByProject(Long projectId) {
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId);
    }

    public List<Milestone> findUpcoming(int daysAhead) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(Math.max(daysAhead, 0));
        return milestoneRepository.findUpcomingBetween(from, to);
    }

    public List<Milestone> findOverdue() {
        return milestoneRepository.findOverdueMilestones(LocalDate.now());
    }

    public List<Milestone> findAll() {
        return milestoneRepository.findAll();
    }

    private void applyForm(Milestone milestone, MilestoneForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", form.getProjectId()));
        milestone.setTitle(form.getTitle().trim());
        milestone.setDescription(form.getDescription());
        milestone.setProject(project);
        milestone.setDueDate(form.getDueDate());
        milestone.setCompletionDate(form.getCompletionDate());
        milestone.setStatus(form.getStatus() != null ? form.getStatus() : MilestoneStatus.PENDING);
        if (milestone.getStatus() == MilestoneStatus.COMPLETED && milestone.getCompletionDate() == null) {
            milestone.setCompletionDate(LocalDate.now());
        }
    }
}
