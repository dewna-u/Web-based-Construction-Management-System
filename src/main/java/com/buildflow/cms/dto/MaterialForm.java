package com.buildflow.cms.dto;

import com.buildflow.cms.enums.MaterialStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class MaterialForm {

    private Long id;

    @NotBlank(message = "Material code is required")
    @Size(max = 50, message = "Material code must be at most 50 characters")
    private String materialCode;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @NotBlank(message = "Unit is required")
    @Size(max = 50, message = "Unit must be at most 50 characters")
    private String unit;

    @NotNull(message = "Current quantity is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Quantity cannot be negative")
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Reorder level cannot be negative")
    private BigDecimal reorderLevel;

    @DecimalMin(value = "0.0", inclusive = true, message = "Unit cost cannot be negative")
    private BigDecimal unitCost;

    private Long supplierId;

    @Size(max = 150, message = "Storage location must be at most 150 characters")
    private String storageLocation;

    @NotNull(message = "Status is required")
    private MaterialStatus status = MaterialStatus.AVAILABLE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(BigDecimal currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public MaterialStatus getStatus() {
        return status;
    }

    public void setStatus(MaterialStatus status) {
        this.status = status;
    }
}
