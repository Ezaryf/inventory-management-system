package com.inventory.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.*;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.service.interfaces.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ProductController.class)
@Import(ProductControllerTest.TestSecurityConfig.class)
@SuppressWarnings("null")
class ProductControllerTest {
        @EnableMethodSecurity
        static class TestSecurityConfig {
        }
        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @MockBean
        private ProductService productService;
        private ProductDTO createTestProductDTO() {
                return ProductDTO.builder()
                                .id(1L)
                                .name("Test Product")
                                .sku("TEST-001")
                                .description("A test product")
                                .categoryId(1L)
                                .categoryName("Electronics")
                                .supplierId(1L)
                                .supplierName("Tech Supplier")
                                .unitPrice(new BigDecimal("99.99"))
                                .currentStock(100)
                                .reorderLevel(10)
                                .lowStock(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }
        @Nested
        @DisplayName("GET /api/products tests")
        class GetProductsTests {
                @Test
                @WithMockUser
                @DisplayName("Should return paginated products")
                void shouldReturnPaginatedProducts() throws Exception {
                        ProductDTO product = createTestProductDTO();
                        PagedResponse<ProductDTO> response = PagedResponse.<ProductDTO>builder()
                                        .content(List.of(product))
                                        .page(0)
                                        .size(10)
                                        .totalElements(1)
                                        .totalPages(1)
                                        .first(true)
                                        .last(true)
                                        .build();
                        when(productService.findAll(any(Pageable.class))).thenReturn(response);
                        mockMvc.perform(get("/api/products")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content[0].id").value(1))
                                        .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                                        .andExpect(jsonPath("$.content[0].sku").value("TEST-001"))
                                        .andExpect(jsonPath("$.totalElements").value(1));
                }
                @Test
                @DisplayName("Should require authentication")
                void shouldRequireAuthentication() throws Exception {
                        mockMvc.perform(get("/api/products"))
                                        .andExpect(status().isUnauthorized());
                }
        }
        @Nested
        @DisplayName("GET /api/products/{id} tests")
        class GetProductByIdTests {
                @Test
                @WithMockUser
                @DisplayName("Should return product when found")
                void shouldReturnProductWhenFound() throws Exception {
                        ProductDTO product = createTestProductDTO();
                        when(productService.findById(1L)).thenReturn(product);
                        mockMvc.perform(get("/api/products/1")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1))
                                        .andExpect(jsonPath("$.name").value("Test Product"));
                }
                @Test
                @WithMockUser
                @DisplayName("Should return 404 when product not found")
                void shouldReturn404WhenNotFound() throws Exception {
                        when(productService.findById(999L))
                                        .thenThrow(new ResourceNotFoundException("Product", "id", 999L));
                        mockMvc.perform(get("/api/products/999")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound());
                }
        }
        @Nested
        @DisplayName("POST /api/products tests")
        class CreateProductTests {
                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should create product successfully")
                void shouldCreateProductSuccessfully() throws Exception {
                        ProductCreateDTO createDTO = ProductCreateDTO.builder()
                                        .name("New Product")
                                        .sku("NEW-001")
                                        .unitPrice(new BigDecimal("49.99"))
                                        .initialStock(50)
                                        .build();
                        ProductDTO responseDTO = ProductDTO.builder()
                                        .id(2L)
                                        .name("New Product")
                                        .sku("NEW-001")
                                        .unitPrice(new BigDecimal("49.99"))
                                        .currentStock(50)
                                        .build();
                        when(productService.create(any(ProductCreateDTO.class))).thenReturn(responseDTO);
                        mockMvc.perform(post("/api/products")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createDTO)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(2))
                                        .andExpect(jsonPath("$.name").value("New Product"));
                }
                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return 400 for invalid input")
                void shouldReturn400ForInvalidInput() throws Exception {
                        ProductCreateDTO invalidDTO = ProductCreateDTO.builder()
                                        .name("") 
                                        .sku("") 
                                        .build();
                        mockMvc.perform(post("/api/products")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalidDTO)))
                                        .andExpect(status().isBadRequest());
                }
                @Test
                @WithMockUser(roles = "VIEWER")
                @DisplayName("Should deny access for VIEWER role")
                void shouldDenyAccessForViewerRole() throws Exception {
                        ProductCreateDTO createDTO = ProductCreateDTO.builder()
                                        .name("New Product")
                                        .sku("NEW-001")
                                        .unitPrice(new BigDecimal("49.99"))
                                        .build();
                        mockMvc.perform(post("/api/products")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createDTO)))
                                        .andExpect(status().isForbidden());
                }
        }
        @Nested
        @DisplayName("DELETE /api/products/{id} tests")
        class DeleteProductTests {
                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("Should delete product with ADMIN role")
                void shouldDeleteProductWithAdminRole() throws Exception {
                        doNothing().when(productService).delete(1L);
                        mockMvc.perform(delete("/api/products/1")
                                        .with(csrf()))
                                        .andExpect(status().isNoContent());
                        verify(productService).delete(1L);
                }
                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should deny delete for USER role")
                void shouldDenyDeleteForUserRole() throws Exception {
                        mockMvc.perform(delete("/api/products/1")
                                        .with(csrf()))
                                        .andExpect(status().isForbidden());
                }
        }
        @Nested
        @DisplayName("GET /api/products/search tests")
        class SearchProductsTests {
                @Test
                @WithMockUser
                @DisplayName("Should search products by name")
                void shouldSearchProductsByName() throws Exception {
                        ProductDTO product = createTestProductDTO();
                        PagedResponse<ProductDTO> response = PagedResponse.<ProductDTO>builder()
                                        .content(List.of(product))
                                        .page(0)
                                        .size(10)
                                        .totalElements(1)
                                        .totalPages(1)
                                        .first(true)
                                        .last(true)
                                        .build();
                        when(productService.searchByName(eq("Test"), any(Pageable.class))).thenReturn(response);
                        mockMvc.perform(get("/api/products/search")
                                        .param("name", "Test")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content[0].name").value("Test Product"));
                }
        }
        @Nested
        @DisplayName("GET /api/products/low-stock tests")
        class LowStockProductsTests {
                @Test
                @WithMockUser
                @DisplayName("Should return low stock products")
                void shouldReturnLowStockProducts() throws Exception {
                        ProductDTO lowStockProduct = ProductDTO.builder()
                                        .id(1L)
                                        .name("Low Stock Product")
                                        .currentStock(5)
                                        .reorderLevel(10)
                                        .lowStock(true)
                                        .build();
                        when(productService.findLowStock()).thenReturn(List.of(lowStockProduct));
                        mockMvc.perform(get("/api/products/low-stock")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[0].lowStock").value(true));
                }
        }
}
