package com.buildflow.cms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ProgressUpdateForm {

    private Long id;

    @NotNull(message = "Project is required")
    private Long projectId;

    @NotNull(message = "Update date is required")
    private LocalDate updateDate;

    @NotNull(message = "Progress percentage is required")
    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must be at most 100")
    private Integer progressPercentage;

    @NotBlank(message = "Summary is required")
    @Size(max = 5000, message = "Summary is too long")
    private String summary;

    @Size(max = 5000, message = "Work completed is too long")
    private String workCompleted;

    @Size(max = 5000, message = "Next planned work is too long")
    private String nextPlannedWork;

    @Size(max = 500, message = "Image path must be at most 500 characters")
    private String imagePath;

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

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getWorkCompleted() {
        return workCompleted;
    }

    public void setWorkCompleted(String workCompleted) {
        this.workCompleted = workCompleted;
    }

    public String getNextPlannedWork() {
        return nextPlannedWork;
    }

    public void setNextPlannedWork(String nextPlannedWork) {
        this.nextPlannedWork = nextPlannedWork;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
