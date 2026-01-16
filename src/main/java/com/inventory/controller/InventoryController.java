package com.inventory.controller;
import com.inventory.dto.*;
import com.inventory.entity.TransactionType;
import com.inventory.service.interfaces.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @PostMapping("/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Add stock", description = "Add stock to a product")
    @ApiResponse(responseCode = "200", description = "Stock added successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDTO> stockIn(@Valid @RequestBody StockAdjustmentDTO dto) {
        return ResponseEntity.ok(inventoryService.addStock(dto));
    }
    @PostMapping("/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Remove stock", description = "Remove stock from a product")
    @ApiResponse(responseCode = "200", description = "Stock removed successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient stock")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDTO> stockOut(@Valid @RequestBody StockAdjustmentDTO dto) {
        return ResponseEntity.ok(inventoryService.removeStock(dto));
    }
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Returns products below reorder level")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }
    @GetMapping("/check-low-stock/{productId}")
    @Operation(summary = "Check if product has low stock", description = "Returns true if stock is below reorder level")
    public ResponseEntity<Boolean> checkLowStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.isLowStock(productId));
    }
    @GetMapping("/transactions")
    @Operation(summary = "Get all transactions", description = "Returns paginated list of all transactions")
    public ResponseEntity<PagedResponse<InventoryTransactionDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(inventoryService.findAllTransactions(pageable));
    }
    @GetMapping("/transactions/product/{productId}")
    @Operation(summary = "Get transactions by product", description = "Returns transactions for a specific product")
    public ResponseEntity<PagedResponse<InventoryTransactionDTO>> getTransactionsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return ResponseEntity.ok(inventoryService.findTransactionsByProduct(productId, pageable));
    }
    @GetMapping("/transactions/type/{type}")
    @Operation(summary = "Get transactions by type", description = "Returns transactions of a specific type")
    public ResponseEntity<PagedResponse<InventoryTransactionDTO>> getTransactionsByType(
            @PathVariable TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return ResponseEntity.ok(inventoryService.findTransactionsByType(type, pageable));
    }
    @GetMapping("/transactions/date-range")
    @Operation(summary = "Get transactions by date range", description = "Returns transactions within a date range")
    public ResponseEntity<PagedResponse<InventoryTransactionDTO>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return ResponseEntity.ok(inventoryService.findTransactionsByDateRange(start, end, pageable));
    }
    @GetMapping("/product/{productId}/history")
    @Operation(summary = "Get product transaction history", description = "Returns transaction history for a product")
    public ResponseEntity<List<InventoryTransactionDTO>> getProductTransactionHistory(
            @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductTransactions(productId));
    }
}
