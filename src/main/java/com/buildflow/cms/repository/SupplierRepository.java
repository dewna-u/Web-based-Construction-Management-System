package com.buildflow.cms.repository;

import com.buildflow.cms.entity.Supplier;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Override
    @EntityGraph(attributePaths = {"linkedUser"})
    Optional<Supplier> findById(Long id);

    List<Supplier> findByActiveTrueOrderByNameAsc();

    List<Supplier> findByActiveFalseOrderByNameAsc();

    List<Supplier> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Supplier> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByActiveTrue();
}
