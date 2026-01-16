package com.inventory.service.impl;
import com.inventory.dto.*;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.entity.TransactionType;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.service.interfaces.InventoryNotificationService;
import com.inventory.service.interfaces.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
@Transactional
@SuppressWarnings("null")
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryNotificationService notificationService;
    public InventoryServiceImpl(ProductRepository productRepository,
            InventoryTransactionRepository transactionRepository,
            InventoryNotificationService notificationService) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }
    @Override
    public ProductDTO addStock(StockAdjustmentDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));
        int previousStock = product.getCurrentStock();
        product.setCurrentStock(previousStock + dto.getQuantity());
        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .transactionType(TransactionType.STOCK_IN)
                .quantity(dto.getQuantity())
                .referenceNumber(dto.getReferenceNumber())
                .notes(dto.getNotes())
                .createdBy(getCurrentUsername())
                .build();
        transactionRepository.save(transaction);
        Product saved = productRepository.save(product);
        notificationService.notifyStockUpdate(
                product.getId(),
                product.getName(),
                previousStock,
                saved.getCurrentStock(),
                "STOCK_IN");
        return mapToProductDTO(saved);
    }
    @Override
    public ProductDTO removeStock(StockAdjustmentDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));
        int previousStock = product.getCurrentStock();
        if (previousStock < dto.getQuantity()) {
            throw new InsufficientStockException(dto.getProductId(), dto.getQuantity(), previousStock);
        }
        product.setCurrentStock(previousStock - dto.getQuantity());
        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .transactionType(TransactionType.STOCK_OUT)
                .quantity(dto.getQuantity())
                .referenceNumber(dto.getReferenceNumber())
                .notes(dto.getNotes())
                .createdBy(getCurrentUsername())
                .build();
        transactionRepository.save(transaction);
        Product saved = productRepository.save(product);
        notificationService.notifyStockUpdate(
                product.getId(),
                product.getName(),
                previousStock,
                saved.getCurrentStock(),
                "STOCK_OUT");
        if (saved.isLowStock()) {
            notificationService.notifyLowStock(
                    saved.getId(),
                    saved.getName(),
                    saved.getCurrentStock(),
                    saved.getReorderLevel());
        }
        return mapToProductDTO(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToProductDTO)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getProductTransactions(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        return transactionRepository.findRecentByProductId(productId, PageRequest.of(0, 100))
                .stream()
                .map(this::mapToTransactionDTO)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public boolean isLowStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return product.isLowStock();
    }
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionDTO> findAllTransactions(Pageable pageable) {
        Page<InventoryTransaction> page = transactionRepository.findAll(pageable);
        return mapToPagedResponse(page);
    }
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionDTO> findTransactionsByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        Page<InventoryTransaction> page = transactionRepository.findByProductId(productId, pageable);
        return mapToPagedResponse(page);
    }
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionDTO> findTransactionsByType(TransactionType type, Pageable pageable) {
        Page<InventoryTransaction> page = transactionRepository.findByTransactionType(type, pageable);
        return mapToPagedResponse(page);
    }
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionDTO> findTransactionsByDateRange(
            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        Page<InventoryTransaction> page = transactionRepository.findByDateRange(start, end, pageable);
        return mapToPagedResponse(page);
    }
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
    private ProductDTO mapToProductDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getCompanyName() : null)
                .unitPrice(product.getUnitPrice())
                .currentStock(product.getCurrentStock())
                .reorderLevel(product.getReorderLevel())
                .lowStock(product.isLowStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    private InventoryTransactionDTO mapToTransactionDTO(InventoryTransaction transaction) {
        return InventoryTransactionDTO.builder()
                .id(transaction.getId())
                .productId(transaction.getProduct().getId())
                .productName(transaction.getProduct().getName())
                .productSku(transaction.getProduct().getSku())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .referenceNumber(transaction.getReferenceNumber())
                .notes(transaction.getNotes())
                .transactionDate(transaction.getTransactionDate())
                .createdBy(transaction.getCreatedBy())
                .build();
    }
    private PagedResponse<InventoryTransactionDTO> mapToPagedResponse(Page<InventoryTransaction> page) {
        return PagedResponse.<InventoryTransactionDTO>builder()
                .content(page.getContent().stream().map(this::mapToTransactionDTO).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
