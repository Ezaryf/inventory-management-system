package com.inventory.repository;

import com.inventory.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Supplier entity.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Find supplier by email.
     */
    Optional<Supplier> findByEmailIgnoreCase(String email);

    /**
     * Check if a supplier exists by email.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find supplier by email, excluding a specific ID (for update validation).
     */
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.email) = LOWER(:email) AND s.id != :id")
    Optional<Supplier> findByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Find suppliers by company name containing (search).
     */
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.companyName) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<Supplier> findByCompanyNameContainingIgnoreCase(String name);
}
