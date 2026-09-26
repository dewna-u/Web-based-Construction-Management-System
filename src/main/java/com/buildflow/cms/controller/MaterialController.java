package com.buildflow.cms.controller;

import com.buildflow.cms.dto.MaterialForm;
import com.buildflow.cms.dto.StockAdjustmentForm;
import com.buildflow.cms.entity.Material;
import com.buildflow.cms.enums.MaterialStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.MaterialService;
import com.buildflow.cms.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialService materialService;
    private final SupplierService supplierService;

    public MaterialController(MaterialService materialService, SupplierService supplierService) {
        this.materialService = materialService;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) MaterialStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<Material> materials = materialService.search(search, category, status);
        model.addAttribute("page", toPage(materials, pageable));
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        model.addAttribute("statuses", MaterialStatus.values());
        model.addAttribute("lowStockCount", materialService.countLowStock());
        return "materials/list";
    }

    @GetMapping("/low-stock")
    public String lowStock(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("page", toPage(materialService.findLowStock(), pageable));
        model.addAttribute("lowStockCount", materialService.countLowStock());
        return "materials/low-stock";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String createForm(Model model) {
        if (!model.containsAttribute("materialForm")) {
            MaterialForm form = new MaterialForm();
            form.setStatus(MaterialStatus.AVAILABLE);
            model.addAttribute("materialForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Add Material");
        return "materials/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String create(
            @Valid @ModelAttribute("materialForm") MaterialForm materialForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Material");
            return "materials/form";
        }
        try {
            Material material = materialService.create(materialForm);
            redirectAttributes.addFlashAttribute("successMessage", "Material created successfully.");
            return "redirect:/materials/" + material.getId();
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("materialError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Add Material");
            return "materials/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("material", materialService.findById(id));
        if (!model.containsAttribute("stockAdjustmentForm")) {
            StockAdjustmentForm adjustmentForm = new StockAdjustmentForm();
            adjustmentForm.setMaterialId(id);
            model.addAttribute("stockAdjustmentForm", adjustmentForm);
        }
        return "materials/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("materialForm")) {
            model.addAttribute("materialForm", toForm(materialService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Material");
        return "materials/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("materialForm") MaterialForm materialForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        materialForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Material");
            return "materials/form";
        }
        try {
            materialService.update(id, materialForm);
            redirectAttributes.addFlashAttribute("successMessage", "Material updated successfully.");
            return "redirect:/materials/" + id;
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("materialError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Material");
            return "materials/form";
        }
    }

    @PostMapping("/{id}/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String adjustStock(
            @PathVariable Long id,
            @Valid @ModelAttribute("stockAdjustmentForm") StockAdjustmentForm stockAdjustmentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        stockAdjustmentForm.setMaterialId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("material", materialService.findById(id));
            return "materials/detail";
        }
        try {
            materialService.stockAdjustment(stockAdjustmentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/materials/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("successMessage", materialService.delete(id));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/materials";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("suppliers", supplierService.findActive());
        model.addAttribute("statuses", MaterialStatus.values());
    }

    private MaterialForm toForm(Material material) {
        MaterialForm form = new MaterialForm();
        form.setId(material.getId());
        form.setMaterialCode(material.getMaterialCode());
        form.setName(material.getName());
        form.setCategory(material.getCategory());
        form.setUnit(material.getUnit());
        form.setCurrentQuantity(material.getCurrentQuantity());
        form.setReorderLevel(material.getReorderLevel());
        form.setUnitCost(material.getUnitCost());
        form.setSupplierId(material.getSupplier() != null ? material.getSupplier().getId() : null);
        form.setStorageLocation(material.getStorageLocation());
        form.setStatus(material.getStatus());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
