package com.inventory.repository;

import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InventoryTransaction entity.
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Find transactions by product ID.
     */
    Page<InventoryTransaction> findByProductId(Long productId, Pageable pageable);

    /**
     * Find transactions by type.
     */
    Page<InventoryTransaction> findByTransactionType(TransactionType type, Pageable pageable);

    /**
     * Find transactions by product ID and type.
     */
    List<InventoryTransaction> findByProductIdAndTransactionType(Long productId, TransactionType type);

    /**
     * Find transactions within a date range.
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<InventoryTransaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find recent transactions for a product.
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.product.id = :productId ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findRecentByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * Sum quantity by product and transaction type.
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM InventoryTransaction t WHERE t.product.id = :productId AND t.transactionType = :type")
    Integer sumQuantityByProductAndType(@Param("productId") Long productId, @Param("type") TransactionType type);
}
