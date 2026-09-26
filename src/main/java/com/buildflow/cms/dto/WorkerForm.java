package com.buildflow.cms.dto;

import com.buildflow.cms.enums.EmploymentType;
import com.buildflow.cms.enums.WorkerStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class WorkerForm {

    private Long id;

    @NotBlank(message = "Worker code is required")
    @Size(max = 50, message = "Worker code must be at most 50 characters")
    private String workerCode;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Size(max = 50, message = "NIC must be at most 50 characters")
    private String nic;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @Size(max = 100, message = "Trade skill must be at most 100 characters")
    private String tradeSkill;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @DecimalMin(value = "0.0", inclusive = true, message = "Daily rate cannot be negative")
    private BigDecimal dailyRate;

    @NotNull(message = "Status is required")
    private WorkerStatus status = WorkerStatus.ACTIVE;

    private Long assignedProjectId;

    private Long linkedUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkerCode() {
        return workerCode;
    }

    public void setWorkerCode(String workerCode) {
        this.workerCode = workerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTradeSkill() {
        return tradeSkill;
    }

    public void setTradeSkill(String tradeSkill) {
        this.tradeSkill = tradeSkill;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void setStatus(WorkerStatus status) {
        this.status = status;
    }

    public Long getAssignedProjectId() {
        return assignedProjectId;
    }

    public void setAssignedProjectId(Long assignedProjectId) {
        this.assignedProjectId = assignedProjectId;
    }

    public Long getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(Long linkedUserId) {
        this.linkedUserId = linkedUserId;
    }
}
