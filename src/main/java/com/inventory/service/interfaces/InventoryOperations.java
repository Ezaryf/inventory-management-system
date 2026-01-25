package com.inventory.service.interfaces;
import com.inventory.dto.InventoryTransactionDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.StockAdjustmentDTO;
import java.util.List;
public interface InventoryOperations {
    ProductDTO addStock(StockAdjustmentDTO dto);
    ProductDTO removeStock(StockAdjustmentDTO dto);
    List<ProductDTO> getLowStockProducts();
    List<InventoryTransactionDTO> getProductTransactions(Long productId);
    boolean isLowStock(Long productId);
}
