package com.inventory.service.impl;
import com.inventory.service.interfaces.InventoryNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service
public class LoggingNotificationService implements InventoryNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingNotificationService.class);
    @Override
    public void notifyLowStock(Long productId, String productName, Integer currentStock, Integer reorderLevel) {
        logger.warn("LOW STOCK ALERT - Product: {} (ID: {}), Current Stock: {}, Reorder Level: {}",
                productName, productId, currentStock, reorderLevel);
    }
    @Override
    public void notifyStockUpdate(Long productId, String productName, Integer previousStock,
            Integer newStock, String operationType) {
        logger.info("STOCK UPDATE - Product: {} (ID: {}), Operation: {}, Previous: {}, New: {}",
                productName, productId, operationType, previousStock, newStock);
    }
}
