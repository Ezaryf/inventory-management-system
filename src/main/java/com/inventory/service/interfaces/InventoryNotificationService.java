package com.inventory.service.interfaces;
public interface InventoryNotificationService {
    void notifyLowStock(Long productId, String productName, Integer currentStock, Integer reorderLevel);
    void notifyStockUpdate(Long productId, String productName, Integer previousStock, Integer newStock,
            String operationType);
}
