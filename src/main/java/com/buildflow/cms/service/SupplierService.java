package com.buildflow.cms.service;

import com.buildflow.cms.dto.SupplierForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Supplier;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import com.buildflow.cms.repository.MaterialRepository;
import com.buildflow.cms.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final AppUserRepository appUserRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            MaterialRepository materialRepository,
            AppUserRepository appUserRepository) {
        this.supplierRepository = supplierRepository;
        this.materialRepository = materialRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Supplier create(SupplierForm form) {
        validateUniqueEmail(form.getEmail(), null);
        Supplier supplier = new Supplier();
        applyForm(supplier, form);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, SupplierForm form) {
        Supplier supplier = findById(id);
        validateUniqueEmail(form.getEmail(), supplier.getId());
        applyForm(supplier, form);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier activate(Long id) {
        Supplier supplier = findById(id);
        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier deactivate(Long id) {
        Supplier supplier = findById(id);
        supplier.setActive(false);
        return supplierRepository.save(supplier);
    }

    /**
     * @return user-facing result message
     */
    @Transactional
    public String delete(Long id) {
        Supplier supplier = findById(id);
        if (materialRepository.existsBySupplierId(id)) {
            supplier.setActive(false);
            supplierRepository.save(supplier);
            return "Supplier is linked to materials, so it was deactivated instead of deleted.";
        }
        supplierRepository.delete(supplier);
        return "Supplier deleted.";
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public List<Supplier> findActive() {
        return supplierRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<Supplier> search(String name) {
        if (!StringUtils.hasText(name)) {
            return supplierRepository.findByActiveTrueOrderByNameAsc();
        }
        return supplierRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name.trim());
    }

    public long countActive() {
        return supplierRepository.countByActiveTrue();
    }

    private void applyForm(Supplier supplier, SupplierForm form) {
        AppUser linkedUser = null;
        if (form.getLinkedUserId() != null) {
            linkedUser = appUserRepository.findById(form.getLinkedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", form.getLinkedUserId()));
        }
        supplier.setName(form.getName().trim());
        supplier.setContactPerson(form.getContactPerson());
        supplier.setPhone(form.getPhone());
        supplier.setEmail(StringUtils.hasText(form.getEmail()) ? form.getEmail().trim().toLowerCase(Locale.ROOT) : null);
        supplier.setAddress(form.getAddress());
        supplier.setActive(form.isActive());
        supplier.setLinkedUser(linkedUser);
    }

    private void validateUniqueEmail(String email, Long excludeId) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        supplierRepository.findByEmailIgnoreCase(email.trim()).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new DuplicateResourceException("Supplier", "email", email);
            }
        });
    }
}
