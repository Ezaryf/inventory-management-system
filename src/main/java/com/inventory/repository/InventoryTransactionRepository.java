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
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByProductId(Long productId, Pageable pageable);
    Page<InventoryTransaction> findByTransactionType(TransactionType type, Pageable pageable);
    List<InventoryTransaction> findByProductIdAndTransactionType(Long productId, TransactionType type);
    @Query("SELECT t FROM InventoryTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<InventoryTransaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    @Query("SELECT t FROM InventoryTransaction t WHERE t.product.id = :productId ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findRecentByProductId(@Param("productId") Long productId, Pageable pageable);
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM InventoryTransaction t WHERE t.product.id = :productId AND t.transactionType = :type")
    Integer sumQuantityByProductAndType(@Param("productId") Long productId, @Param("type") TransactionType type);
}
