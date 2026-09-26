package com.buildflow.cms.dto;

import com.buildflow.cms.enums.ProjectStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectForm {

    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @NotBlank(message = "Project code is required")
    @Size(max = 50, message = "Project code must be at most 50 characters")
    private String projectCode;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @NotNull(message = "Client is required")
    private Long clientId;

    private Long managerId;

    private LocalDate startDate;

    private LocalDate expectedEndDate;

    private LocalDate actualEndDate;

    @NotNull(message = "Status is required")
    private ProjectStatus status = ProjectStatus.PLANNING;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget cannot be negative")
    private BigDecimal budget;

    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must be at most 100")
    private Integer progressPercentage = 0;

    private boolean archived;

    @AssertTrue(message = "Expected end date must be on or after the start date")
    public boolean isEndDateValid() {
        if (startDate == null || expectedEndDate == null) {
            return true;
        }
        return !expectedEndDate.isBefore(startDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(LocalDate expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
