package com.buildflow.cms.service;

import com.buildflow.cms.dto.MaterialForm;
import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.dto.StockAdjustmentForm;
import com.buildflow.cms.entity.Material;
import com.buildflow.cms.enums.MaterialStatus;
import com.buildflow.cms.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private UserService userService;

    private Material material;

    @BeforeEach
    void setUp() {
        SetupForm setup = new SetupForm();
        setup.setFullName("Admin");
        setup.setEmail("admin@buildflow.test");
        setup.setPassword("Password1!");
        setup.setConfirmPassword("Password1!");
        userService.createFirstAdmin(setup);

        MaterialForm form = new MaterialForm();
        form.setMaterialCode("CEM-001");
        form.setName("Portland Cement");
        form.setUnit("bags");
        form.setCurrentQuantity(new BigDecimal("100"));
        form.setReorderLevel(new BigDecimal("40"));
        form.setUnitCost(new BigDecimal("1250.00"));
        form.setStatus(MaterialStatus.AVAILABLE);
        material = materialService.create(form);
    }

    @Test
    void stockAdjustment_SetsLowStockStatus() {
        StockAdjustmentForm adjustment = new StockAdjustmentForm();
        adjustment.setMaterialId(material.getId());
        adjustment.setAdjustmentQuantity(new BigDecimal("-70"));
        adjustment.setReason("Site issue");

        Material updated = materialService.stockAdjustment(adjustment);

        assertThat(updated.getCurrentQuantity()).isEqualByComparingTo("30");
        assertThat(updated.getStatus()).isEqualTo(MaterialStatus.LOW_STOCK);
        assertThat(materialService.findLowStock()).extracting(Material::getId).contains(updated.getId());
    }

    @Test
    void stockAdjustment_SetsOutOfStock() {
        StockAdjustmentForm adjustment = new StockAdjustmentForm();
        adjustment.setMaterialId(material.getId());
        adjustment.setAdjustmentQuantity(new BigDecimal("-100"));

        Material updated = materialService.stockAdjustment(adjustment);

        assertThat(updated.getCurrentQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updated.getStatus()).isEqualTo(MaterialStatus.OUT_OF_STOCK);
    }

    @Test
    void stockAdjustment_RejectsNegativeStock() {
        StockAdjustmentForm adjustment = new StockAdjustmentForm();
        adjustment.setMaterialId(material.getId());
        adjustment.setAdjustmentQuantity(new BigDecimal("-101"));

        assertThatThrownBy(() -> materialService.stockAdjustment(adjustment))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("below zero");
    }
}
