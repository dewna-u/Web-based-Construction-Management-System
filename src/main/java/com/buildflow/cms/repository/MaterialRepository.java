package com.buildflow.cms.repository;

import com.buildflow.cms.entity.Material;
import com.buildflow.cms.entity.Supplier;
import com.buildflow.cms.enums.MaterialStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Override
    @EntityGraph(attributePaths = {"supplier"})
    Optional<Material> findById(Long id);

    Optional<Material> findByMaterialCodeIgnoreCase(String materialCode);

    boolean existsByMaterialCodeIgnoreCase(String materialCode);

    List<Material> findByStatusOrderByNameAsc(MaterialStatus status);

    List<Material> findBySupplierOrderByNameAsc(Supplier supplier);

    List<Material> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    List<Material> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    @EntityGraph(attributePaths = {"supplier"})
    @Query("""
            SELECT m FROM Material m
            WHERE m.reorderLevel IS NOT NULL
              AND m.currentQuantity <= m.reorderLevel
              AND m.status <> com.buildflow.cms.enums.MaterialStatus.DISCONTINUED
            ORDER BY m.currentQuantity ASC
            """)
    List<Material> findLowStockMaterials();

    @Query("""
            SELECT COUNT(m) FROM Material m
            WHERE m.reorderLevel IS NOT NULL
              AND m.currentQuantity <= m.reorderLevel
              AND m.status <> com.buildflow.cms.enums.MaterialStatus.DISCONTINUED
            """)
    long countLowStockMaterials();

    long countByStatus(MaterialStatus status);

    boolean existsBySupplierId(Long supplierId);
}
