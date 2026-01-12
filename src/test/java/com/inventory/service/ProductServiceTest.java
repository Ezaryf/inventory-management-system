package com.inventory.service;

import com.inventory.dto.ProductCreateDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.ProductUpdateDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic items")
                .build();

        testSupplier = Supplier.builder()
                .id(1L)
                .companyName("Tech Supplier")
                .email("tech@supplier.com")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .description("A test product")
                .category(testCategory)
                .supplier(testSupplier)
                .unitPrice(new BigDecimal("99.99"))
                .currentStock(100)
                .reorderLevel(10)
                .build();
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            ProductDTO result = productService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getSku()).isEqualTo("TEST-001");
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("create tests")
    class CreateTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            ProductCreateDTO createDTO = ProductCreateDTO.builder()
                    .name("New Product")
                    .sku("NEW-001")
                    .description("A new product")
                    .categoryId(1L)
                    .supplierId(1L)
                    .unitPrice(new BigDecimal("49.99"))
                    .initialStock(50)
                    .reorderLevel(5)
                    .build();

            when(productRepository.existsBySku("NEW-001")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(2L);
                return p;
            });

            ProductDTO result = productService.create(createDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Product");
            assertThat(result.getSku()).isEqualTo("NEW-001");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate SKU")
        void shouldThrowExceptionForDuplicateSku() {
            ProductCreateDTO createDTO = ProductCreateDTO.builder()
                    .name("Duplicate Product")
                    .sku("TEST-001")
                    .unitPrice(new BigDecimal("29.99"))
                    .build();

            when(productRepository.existsBySku("TEST-001")).thenReturn(true);

            assertThatThrownBy(() -> productService.create(createDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Product")
                    .hasMessageContaining("sku");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            ProductCreateDTO createDTO = ProductCreateDTO.builder()
                    .name("New Product")
                    .sku("NEW-002")
                    .categoryId(999L)
                    .unitPrice(new BigDecimal("29.99"))
                    .build();

            when(productRepository.existsBySku("NEW-002")).thenReturn(false);
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(createDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category");
        }
    }

    @Nested
    @DisplayName("update tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            ProductUpdateDTO updateDTO = ProductUpdateDTO.builder()
                    .name("Updated Product")
                    .description("Updated description")
                    .unitPrice(new BigDecimal("149.99"))
                    .reorderLevel(20)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDTO result = productService.update(1L, updateDTO);

            assertThat(result).isNotNull();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
            ProductUpdateDTO updateDTO = ProductUpdateDTO.builder()
                    .name("Updated Product")
                    .unitPrice(new BigDecimal("149.99"))
                    .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(999L, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            when(productRepository.existsById(1L)).thenReturn(true);
            doNothing().when(productRepository).deleteById(1L);

            productService.delete(1L);

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            when(productRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> productService.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Low stock tests")
    class LowStockTests {

        @Test
        @DisplayName("Should identify low stock product")
        void shouldIdentifyLowStockProduct() {
            testProduct.setCurrentStock(5);
            testProduct.setReorderLevel(10);

            assertThat(testProduct.isLowStock()).isTrue();
        }

        @Test
        @DisplayName("Should identify product with sufficient stock")
        void shouldIdentifyProductWithSufficientStock() {
            testProduct.setCurrentStock(100);
            testProduct.setReorderLevel(10);

            assertThat(testProduct.isLowStock()).isFalse();
        }
    }
}
