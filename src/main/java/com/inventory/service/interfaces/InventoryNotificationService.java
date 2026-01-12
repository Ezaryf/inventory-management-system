package com.inventory.service.interfaces;

/**
 * Interface for low stock notifications (DIP - Dependency Inversion Principle).
 * Allows different notification strategies to be plugged in.
 */
public interface InventoryNotificationService {

    /**
     * Send notification for low stock.
     * 
     * @param productId    Product ID
     * @param productName  Product name
     * @param currentStock Current stock level
     * @param reorderLevel Reorder threshold
     */
    void notifyLowStock(Long productId, String productName, Integer currentStock, Integer reorderLevel);

    /**
     * Send notification for stock update.
     * 
     * @param productId     Product ID
     * @param productName   Product name
     * @param previousStock Previous stock level
     * @param newStock      New stock level
     * @param operationType Type of operation (STOCK_IN, STOCK_OUT)
     */
    void notifyStockUpdate(Long productId, String productName, Integer previousStock, Integer newStock,
            String operationType);
}
