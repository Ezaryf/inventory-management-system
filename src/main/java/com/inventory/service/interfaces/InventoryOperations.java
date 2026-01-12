package com.inventory.service.interfaces;

import com.inventory.dto.InventoryTransactionDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.StockAdjustmentDTO;
import com.inventory.entity.Product;

import java.util.List;

/**
 * Interface for inventory operations (ISP - Interface Segregation Principle).
 */
public interface InventoryOperations {

    /**
     * Add stock to a product (stock in).
     * 
     * @param dto Stock adjustment details
     * @return Updated product
     */
    ProductDTO addStock(StockAdjustmentDTO dto);

    /**
     * Remove stock from a product (stock out).
     * 
     * @param dto Stock adjustment details
     * @return Updated product
     */
    ProductDTO removeStock(StockAdjustmentDTO dto);

    /**
     * Get all products with low stock.
     * 
     * @return List of products below reorder level
     */
    List<ProductDTO> getLowStockProducts();

    /**
     * Get transaction history for a product.
     * 
     * @param productId Product ID
     * @return List of transactions
     */
    List<InventoryTransactionDTO> getProductTransactions(Long productId);

    /**
     * Check if a product has low stock.
     * 
     * @param productId Product ID
     * @return true if stock is at or below reorder level
     */
    boolean isLowStock(Long productId);
}
