package com.buildflow.cms.service;

import com.buildflow.cms.dto.MaterialRequestForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Material;
import com.buildflow.cms.entity.MaterialRequest;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.MaterialRequestStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.MaterialRepository;
import com.buildflow.cms.repository.MaterialRequestRepository;
import com.buildflow.cms.repository.ProjectRepository;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MaterialRequestService {

    private final MaterialRequestRepository materialRequestRepository;
    private final ProjectRepository projectRepository;
    private final MaterialRepository materialRepository;
    private final MaterialService materialService;

    public MaterialRequestService(
            MaterialRequestRepository materialRequestRepository,
            ProjectRepository projectRepository,
            MaterialRepository materialRepository,
            MaterialService materialService) {
        this.materialRequestRepository = materialRequestRepository;
        this.projectRepository = projectRepository;
        this.materialRepository = materialRepository;
        this.materialService = materialService;
    }

    @Transactional
    public MaterialRequest create(MaterialRequestForm form) {
        MaterialRequest request = new MaterialRequest();
        applyFormFields(request, form);
        AppUser requester = SecurityUtils.requireCurrentUser();
        request.setRequester(requester);
        request.setStatus(MaterialRequestStatus.PENDING);
        if (request.getRequestDate() == null) {
            request.setRequestDate(LocalDate.now());
        }
        return materialRequestRepository.save(request);
    }

    @Transactional
    public MaterialRequest update(Long id, MaterialRequestForm form) {
        MaterialRequest request = findById(id);
        if (request.getStatus() != MaterialRequestStatus.PENDING) {
            throw new BusinessException("Only PENDING material requests can be updated");
        }
        applyFormFields(request, form);
        request.setStatus(MaterialRequestStatus.PENDING);
        return materialRequestRepository.save(request);
    }

    @Transactional
    public MaterialRequest approve(Long id) {
        MaterialRequest request = findById(id);
        if (request.getStatus() != MaterialRequestStatus.PENDING) {
            throw new BusinessException("Only PENDING requests can be approved");
        }
        request.setStatus(MaterialRequestStatus.APPROVED);
        markReviewed(request);
        return materialRequestRepository.save(request);
    }

    @Transactional
    public MaterialRequest reject(Long id, String rejectionReason) {
        MaterialRequest request = findById(id);
        if (request.getStatus() != MaterialRequestStatus.PENDING) {
            throw new BusinessException("Only PENDING requests can be rejected");
        }
        if (!StringUtils.hasText(rejectionReason)) {
            throw new BusinessException("Rejection reason is required");
        }
        request.setStatus(MaterialRequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason.trim());
        markReviewed(request);
        return materialRequestRepository.save(request);
    }

    @Transactional
    public MaterialRequest markOrdered(Long id) {
        MaterialRequest request = findById(id);
        if (request.getStatus() != MaterialRequestStatus.APPROVED) {
            throw new BusinessException("Only APPROVED requests can be marked as ordered");
        }
        request.setStatus(MaterialRequestStatus.ORDERED);
        return materialRequestRepository.save(request);
    }

    @Transactional
    public MaterialRequest markDelivered(Long id) {
        MaterialRequest request = findById(id);
        if (request.getStatus() != MaterialRequestStatus.ORDERED
                && request.getStatus() != MaterialRequestStatus.APPROVED) {
            throw new BusinessException("Only APPROVED or ORDERED requests can be marked as delivered");
        }
        Material material = request.getMaterial();
        BigDecimal qty = request.getQuantity() != null ? request.getQuantity() : BigDecimal.ZERO;
        BigDecimal current = material.getCurrentQuantity() != null ? material.getCurrentQuantity() : BigDecimal.ZERO;
        material.setCurrentQuantity(current.add(qty));
        materialService.refreshStockStatus(material);
        materialRepository.save(material);

        request.setStatus(MaterialRequestStatus.DELIVERED);
        return materialRequestRepository.save(request);
    }

    @Transactional
    public MaterialRequest cancel(Long id) {
        MaterialRequest request = findById(id);
        if (request.getStatus() == MaterialRequestStatus.DELIVERED
                || request.getStatus() == MaterialRequestStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel a " + request.getStatus() + " request");
        }
        request.setStatus(MaterialRequestStatus.CANCELLED);
        return materialRequestRepository.save(request);
    }

    public MaterialRequest findById(Long id) {
        return materialRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialRequest", id));
    }

    public List<MaterialRequest> findAll() {
        return materialRequestRepository.findAll();
    }

    public List<MaterialRequest> findByProject(Long projectId) {
        return materialRequestRepository.findByProjectIdOrderByRequestDateDesc(projectId);
    }

    public List<MaterialRequest> findByStatus(MaterialRequestStatus status) {
        return materialRequestRepository.findByStatusOrderByRequestDateDesc(status);
    }

    public long countPending() {
        return materialRequestRepository.countByStatus(MaterialRequestStatus.PENDING);
    }

    private void applyFormFields(MaterialRequest request, MaterialRequestForm form) {
        Project project = projectRepository.findById(form.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", form.getProjectId()));
        Material material = materialRepository.findById(form.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", form.getMaterialId()));
        if (form.getQuantity() == null || form.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        request.setProject(project);
        request.setMaterial(material);
        request.setQuantity(form.getQuantity());
        request.setRequestDate(form.getRequestDate() != null ? form.getRequestDate() : LocalDate.now());
        request.setRequiredDate(form.getRequiredDate());
        request.setNotes(form.getNotes());
    }

    private void markReviewed(MaterialRequest request) {
        SecurityUtils.getCurrentUser().ifPresent(request::setReviewedBy);
        request.setReviewedAt(LocalDateTime.now());
    }
}
