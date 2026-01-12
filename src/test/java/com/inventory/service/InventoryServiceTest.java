package com.inventory.service;

import com.inventory.dto.InventoryTransactionDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.StockAdjustmentDTO;
import com.inventory.entity.Category;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.entity.TransactionType;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.service.impl.InventoryServiceImpl;
import com.inventory.service.interfaces.InventoryNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private InventoryNotificationService notificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .unitPrice(new BigDecimal("99.99"))
                .currentStock(100)
                .reorderLevel(10)
                .build();

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
    }

    @Nested
    @DisplayName("Stock In tests")
    class StockInTests {

        @Test
        @DisplayName("Should add stock successfully")
        void shouldAddStockSuccessfully() {
            StockAdjustmentDTO dto = StockAdjustmentDTO.builder()
                    .productId(1L)
                    .quantity(50)
                    .referenceNumber("PO-001")
                    .notes("Test stock in")
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(new InventoryTransaction());

            ProductDTO result = inventoryService.addStock(dto);

            assertThat(result).isNotNull();
            assertThat(testProduct.getCurrentStock()).isEqualTo(150);
            verify(transactionRepository).save(any(InventoryTransaction.class));
            verify(notificationService).notifyStockUpdate(
                    eq(1L), eq("Test Product"), eq(100), eq(150), eq("STOCK_IN"));
        }

        @Test
        @DisplayName("Should throw exception when product not found for stock in")
        void shouldThrowExceptionWhenProductNotFound() {
            StockAdjustmentDTO dto = StockAdjustmentDTO.builder()
                    .productId(999L)
                    .quantity(50)
                    .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.addStock(dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Stock Out tests")
    class StockOutTests {

        @Test
        @DisplayName("Should remove stock successfully")
        void shouldRemoveStockSuccessfully() {
            StockAdjustmentDTO dto = StockAdjustmentDTO.builder()
                    .productId(1L)
                    .quantity(30)
                    .referenceNumber("SO-001")
                    .notes("Test stock out")
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(new InventoryTransaction());

            ProductDTO result = inventoryService.removeStock(dto);

            assertThat(result).isNotNull();
            assertThat(testProduct.getCurrentStock()).isEqualTo(70);
            verify(transactionRepository).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should throw exception for insufficient stock")
        void shouldThrowExceptionForInsufficientStock() {
            testProduct.setCurrentStock(20);

            StockAdjustmentDTO dto = StockAdjustmentDTO.builder()
                    .productId(1L)
                    .quantity(50)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> inventoryService.removeStock(dto))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should notify for low stock after stock out")
        void shouldNotifyForLowStockAfterStockOut() {
            testProduct.setCurrentStock(15);
            testProduct.setReorderLevel(10);

            StockAdjustmentDTO dto = StockAdjustmentDTO.builder()
                    .productId(1L)
                    .quantity(10)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                // Simulate the updated product
                return p;
            });
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(new InventoryTransaction());

            inventoryService.removeStock(dto);

            verify(notificationService).notifyLowStock(
                    eq(1L), eq("Test Product"), eq(5), eq(10));
        }
    }

    @Nested
    @DisplayName("Low Stock tests")
    class LowStockTests {

        @Test
        @DisplayName("Should return low stock products")
        void shouldReturnLowStockProducts() {
            Product lowStockProduct = Product.builder()
                    .id(2L)
                    .name("Low Stock Product")
                    .sku("LOW-001")
                    .currentStock(5)
                    .reorderLevel(10)
                    .unitPrice(new BigDecimal("19.99"))
                    .build();

            when(productRepository.findLowStockProducts())
                    .thenReturn(Arrays.asList(lowStockProduct));

            List<ProductDTO> result = inventoryService.getLowStockProducts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Low Stock Product");
            assertThat(result.get(0).getLowStock()).isTrue();
        }

        @Test
        @DisplayName("Should check if product has low stock")
        void shouldCheckIfProductHasLowStock() {
            testProduct.setCurrentStock(5);
            testProduct.setReorderLevel(10);

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            boolean result = inventoryService.isLowStock(1L);

            assertThat(result).isTrue();
        }
    }
}
