package com.buildflow.cms.dto;

import com.buildflow.cms.enums.MaterialRequestStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MaterialRequestForm {

    private Long id;

    @NotNull(message = "Project is required")
    private Long projectId;

    @NotNull(message = "Material is required")
    private Long materialId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", inclusive = true, message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private LocalDate requestDate;

    private LocalDate requiredDate;

    @NotNull(message = "Status is required")
    private MaterialRequestStatus status = MaterialRequestStatus.PENDING;

    @Size(max = 5000, message = "Notes are too long")
    private String notes;

    @Size(max = 5000, message = "Rejection reason is too long")
    private String rejectionReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public MaterialRequestStatus getStatus() {
        return status;
    }

    public void setStatus(MaterialRequestStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
