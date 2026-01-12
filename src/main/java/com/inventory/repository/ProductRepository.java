package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check if a product exists by SKU.
     */
    boolean existsBySku(String sku);

    /**
     * Search products by name (case-insensitive, contains).
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Search products by name without pagination.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find products by category ID.
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find products by supplier ID.
     */
    Page<Product> findBySupplierId(Long supplierId, Pageable pageable);

    /**
     * Find products with low stock (current stock <= reorder level).
     */
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.reorderLevel")
    List<Product> findLowStockProducts();

    /**
     * Find products with low stock (with pagination).
     */
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.reorderLevel")
    Page<Product> findLowStockProducts(Pageable pageable);

    /**
     * Count products by category ID.
     */
    long countByCategoryId(Long categoryId);

    /**
     * Count products by supplier ID.
     */
    long countBySupplierId(Long supplierId);
}
