package com.buildflow.cms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class StockAdjustmentForm {

    @NotNull(message = "Material is required")
    private Long materialId;

    @NotNull(message = "Adjustment quantity is required")
    private BigDecimal adjustmentQuantity;

    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public BigDecimal getAdjustmentQuantity() {
        return adjustmentQuantity;
    }

    public void setAdjustmentQuantity(BigDecimal adjustmentQuantity) {
        this.adjustmentQuantity = adjustmentQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
