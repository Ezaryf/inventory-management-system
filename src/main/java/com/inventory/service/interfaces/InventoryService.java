package com.inventory.service.interfaces;

import com.inventory.dto.*;
import com.inventory.entity.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Service interface for Inventory operations.
 */
public interface InventoryService extends InventoryOperations {

    PagedResponse<InventoryTransactionDTO> findAllTransactions(Pageable pageable);

    PagedResponse<InventoryTransactionDTO> findTransactionsByProduct(Long productId, Pageable pageable);

    PagedResponse<InventoryTransactionDTO> findTransactionsByType(TransactionType type, Pageable pageable);

    PagedResponse<InventoryTransactionDTO> findTransactionsByDateRange(LocalDateTime start, LocalDateTime end,
            Pageable pageable);
}
