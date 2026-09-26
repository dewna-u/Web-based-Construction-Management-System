package com.buildflow.cms.service;

import com.buildflow.cms.dto.MaterialForm;
import com.buildflow.cms.dto.StockAdjustmentForm;
import com.buildflow.cms.entity.Material;
import com.buildflow.cms.entity.Supplier;
import com.buildflow.cms.enums.MaterialStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.MaterialRepository;
import com.buildflow.cms.repository.MaterialRequestRepository;
import com.buildflow.cms.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final SupplierRepository supplierRepository;

    public MaterialService(
            MaterialRepository materialRepository,
            MaterialRequestRepository materialRequestRepository,
            SupplierRepository supplierRepository) {
        this.materialRepository = materialRepository;
        this.materialRequestRepository = materialRequestRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public Material create(MaterialForm form) {
        String code = form.getMaterialCode().trim();
        if (materialRepository.existsByMaterialCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Material", "materialCode", code);
        }
        Material material = new Material();
        applyForm(material, form);
        refreshStockStatus(material);
        return materialRepository.save(material);
    }

    @Transactional
    public Material update(Long id, MaterialForm form) {
        Material material = findById(id);
        String code = form.getMaterialCode().trim();
        if (!material.getMaterialCode().equalsIgnoreCase(code)
                && materialRepository.existsByMaterialCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Material", "materialCode", code);
        }
        applyForm(material, form);
        refreshStockStatus(material);
        return materialRepository.save(material);
    }

    @Transactional
    public Material stockAdjustment(StockAdjustmentForm form) {
        Material material = findById(form.getMaterialId());
        if (form.getAdjustmentQuantity() == null) {
            throw new BusinessException("Adjustment quantity is required");
        }
        BigDecimal current = material.getCurrentQuantity() != null ? material.getCurrentQuantity() : BigDecimal.ZERO;
        BigDecimal updated = current.add(form.getAdjustmentQuantity());
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Stock quantity cannot go below zero");
        }
        material.setCurrentQuantity(updated);
        refreshStockStatus(material);
        return materialRepository.save(material);
    }

    /**
     * Deletes the material when unused; otherwise marks it DISCONTINUED to preserve history.
     *
     * @return user-facing result message
     */
    @Transactional
    public String delete(Long id) {
        Material material = findById(id);
        if (materialRequestRepository.existsByMaterialId(id)) {
            material.setStatus(MaterialStatus.DISCONTINUED);
            materialRepository.save(material);
            return "Material is linked to requests, so it was marked as discontinued instead of deleted.";
        }
        materialRepository.delete(material);
        return "Material deleted.";
    }

    public Material findById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));
    }

    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    public List<Material> findLowStock() {
        return materialRepository.findLowStockMaterials();
    }

    public List<Material> search(String keyword, String category, MaterialStatus status) {
        Stream<Material> stream = materialRepository.findAll().stream();

        if (status != null) {
            stream = stream.filter(m -> m.getStatus() == status);
        }
        if (StringUtils.hasText(category)) {
            String cat = category.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(m -> m.getCategory() != null
                    && m.getCategory().toLowerCase(Locale.ROOT).equals(cat));
        }
        if (StringUtils.hasText(keyword)) {
            String q = keyword.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(m -> (m.getName() != null && m.getName().toLowerCase(Locale.ROOT).contains(q))
                    || (m.getMaterialCode() != null && m.getMaterialCode().toLowerCase(Locale.ROOT).contains(q)));
        }

        return stream
                .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                        a.getName() != null ? a.getName() : "",
                        b.getName() != null ? b.getName() : ""))
                .toList();
    }

    public long countLowStock() {
        return materialRepository.countLowStockMaterials();
    }

    public void refreshStockStatus(Material material) {
        if (material.getStatus() == MaterialStatus.DISCONTINUED) {
            return;
        }
        BigDecimal qty = material.getCurrentQuantity() != null ? material.getCurrentQuantity() : BigDecimal.ZERO;
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            material.setStatus(MaterialStatus.OUT_OF_STOCK);
            return;
        }
        BigDecimal reorder = material.getReorderLevel();
        if (reorder != null && qty.compareTo(reorder) <= 0) {
            material.setStatus(MaterialStatus.LOW_STOCK);
        } else {
            material.setStatus(MaterialStatus.AVAILABLE);
        }
    }

    private void applyForm(Material material, MaterialForm form) {
        Supplier supplier = null;
        if (form.getSupplierId() != null) {
            supplier = supplierRepository.findById(form.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", form.getSupplierId()));
        }
        material.setMaterialCode(form.getMaterialCode().trim());
        material.setName(form.getName().trim());
        material.setCategory(form.getCategory());
        material.setUnit(form.getUnit().trim());
        material.setCurrentQuantity(form.getCurrentQuantity() != null ? form.getCurrentQuantity() : BigDecimal.ZERO);
        material.setReorderLevel(form.getReorderLevel());
        material.setUnitCost(form.getUnitCost());
        material.setSupplier(supplier);
        material.setStorageLocation(form.getStorageLocation());
        if (form.getStatus() == MaterialStatus.DISCONTINUED) {
            material.setStatus(MaterialStatus.DISCONTINUED);
        } else if (form.getStatus() != null) {
            material.setStatus(form.getStatus());
        }
    }
}
