package com.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Inventory Management System.
 * Tests end-to-end flows with in-memory H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    private Category testCategory;
    private Supplier testSupplier;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        supplierRepository.deleteAll();

        testCategory = categoryRepository.save(Category.builder()
                .name("Test Category")
                .description("Test category for integration tests")
                .build());

        testSupplier = supplierRepository.save(Supplier.builder()
                .companyName("Test Supplier")
                .email("test@supplier.com")
                .contactPerson("John Doe")
                .phone("+1-555-0100")
                .build());

        testProduct = productRepository.save(Product.builder()
                .name("Integration Test Product")
                .sku("INT-TEST-001")
                .description("A product for integration testing")
                .category(testCategory)
                .supplier(testSupplier)
                .unitPrice(new BigDecimal("49.99"))
                .currentStock(100)
                .reorderLevel(10)
                .build());
    }

    @Nested
    @DisplayName("Product CRUD Integration Tests")
    class ProductCrudTests {

        @Test
        @WithMockUser
        @DisplayName("Should get all products")
        void shouldGetAllProducts() throws Exception {
            mockMvc.perform(get("/api/products")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("Integration Test Product"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should create and retrieve product")
        void shouldCreateAndRetrieveProduct() throws Exception {
            ProductCreateDTO createDTO = ProductCreateDTO.builder()
                    .name("New Integration Product")
                    .sku("INT-NEW-001")
                    .description("Created via integration test")
                    .categoryId(testCategory.getId())
                    .supplierId(testSupplier.getId())
                    .unitPrice(new BigDecimal("29.99"))
                    .initialStock(50)
                    .reorderLevel(5)
                    .build();

            String response = mockMvc.perform(post("/api/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Integration Product"))
                    .andExpect(jsonPath("$.categoryName").value("Test Category"))
                    .andReturn().getResponse().getContentAsString();

            ProductDTO created = objectMapper.readValue(response, ProductDTO.class);

            // Verify it can be retrieved
            mockMvc.perform(get("/api/products/" + created.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("INT-NEW-001"));
        }
    }

    @Nested
    @DisplayName("Inventory Operations Integration Tests")
    class InventoryOperationsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should perform stock in operation")
        void shouldPerformStockIn() throws Exception {
            int initialStock = testProduct.getCurrentStock();

            StockAdjustmentDTO stockIn = StockAdjustmentDTO.builder()
                    .productId(testProduct.getId())
                    .quantity(50)
                    .referenceNumber("PO-INT-001")
                    .notes("Integration test stock in")
                    .build();

            mockMvc.perform(post("/api/inventory/stock-in")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(stockIn)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentStock").value(initialStock + 50));

            // Verify transaction was recorded
            assertThat(transactionRepository.findByProductId(testProduct.getId(),
                    org.springframework.data.domain.PageRequest.of(0, 10))
                    .getContent()).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should perform stock out operation")
        void shouldPerformStockOut() throws Exception {
            int initialStock = testProduct.getCurrentStock();

            StockAdjustmentDTO stockOut = StockAdjustmentDTO.builder()
                    .productId(testProduct.getId())
                    .quantity(30)
                    .referenceNumber("SO-INT-001")
                    .notes("Integration test stock out")
                    .build();

            mockMvc.perform(post("/api/inventory/stock-out")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(stockOut)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentStock").value(initialStock - 30));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject insufficient stock")
        void shouldRejectInsufficientStock() throws Exception {
            StockAdjustmentDTO stockOut = StockAdjustmentDTO.builder()
                    .productId(testProduct.getId())
                    .quantity(500) // More than available
                    .build();

            mockMvc.perform(post("/api/inventory/stock-out")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(stockOut)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Insufficient stock")));
        }
    }

    @Nested
    @DisplayName("Low Stock Integration Tests")
    class LowStockTests {

        @Test
        @WithMockUser
        @DisplayName("Should detect low stock products")
        void shouldDetectLowStockProducts() throws Exception {
            // Create a low stock product
            Product lowStockProduct = productRepository.save(Product.builder()
                    .name("Low Stock Product")
                    .sku("LOW-INT-001")
                    .unitPrice(new BigDecimal("9.99"))
                    .currentStock(5)
                    .reorderLevel(10)
                    .build());

            mockMvc.perform(get("/api/products/low-stock")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sku").value("LOW-INT-001"))
                    .andExpect(jsonPath("$[0].lowStock").value(true));
        }
    }

    @Nested
    @DisplayName("Search Integration Tests")
    class SearchTests {

        @Test
        @WithMockUser
        @DisplayName("Should search products by name")
        void shouldSearchProductsByName() throws Exception {
            mockMvc.perform(get("/api/products/search")
                    .param("name", "Integration")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value(
                            org.hamcrest.Matchers.containsString("Integration")));
        }
    }
}
