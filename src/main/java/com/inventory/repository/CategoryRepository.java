package com.inventory.repository;

import com.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Check if a category exists by name.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find category by name, excluding a specific ID (for update validation).
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :id")
    Optional<Category> findByNameIgnoreCaseAndIdNot(String name, Long id);
}
