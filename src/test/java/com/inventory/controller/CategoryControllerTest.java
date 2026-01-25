package com.inventory.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.*;
import com.inventory.service.interfaces.CategoryService;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(CategoryController.class)
@Import(CategoryControllerTest.TestSecurityConfig.class)
@SuppressWarnings("null")
class CategoryControllerTest {
        @EnableMethodSecurity
        static class TestSecurityConfig {
        }
        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @MockBean
        private CategoryService categoryService;
        private CategoryDTO createTestCategoryDTO() {
                return CategoryDTO.builder()
                                .id(1L)
                                .name("Electronics")
                                .description("Electronic devices")
                                .productCount(10)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }
        @Test
        @WithMockUser
        @DisplayName("Should return paginated categories")
        void shouldReturnPaginatedCategories() throws Exception {
                CategoryDTO category = createTestCategoryDTO();
                PagedResponse<CategoryDTO> response = PagedResponse.<CategoryDTO>builder()
                                .content(List.of(category))
                                .page(0)
                                .size(10)
                                .totalElements(1)
                                .totalPages(1)
                                .first(true)
                                .last(true)
                                .build();
                when(categoryService.findAll(any(Pageable.class))).thenReturn(response);
                mockMvc.perform(get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(1))
                                .andExpect(jsonPath("$.content[0].name").value("Electronics"));
        }
        @Test
        @WithMockUser
        @DisplayName("Should return category by ID")
        void shouldReturnCategoryById() throws Exception {
                CategoryDTO category = createTestCategoryDTO();
                when(categoryService.findById(1L)).thenReturn(category);
                mockMvc.perform(get("/api/categories/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Electronics"));
        }
        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() throws Exception {
                CategoryCreateDTO createDTO = CategoryCreateDTO.builder()
                                .name("New Category")
                                .description("A new category")
                                .build();
                CategoryDTO responseDTO = CategoryDTO.builder()
                                .id(2L)
                                .name("New Category")
                                .description("A new category")
                                .productCount(0)
                                .build();
                when(categoryService.create(any(CategoryCreateDTO.class))).thenReturn(responseDTO);
                mockMvc.perform(post("/api/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(2))
                                .andExpect(jsonPath("$.name").value("New Category"));
        }
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete category with ADMIN role")
        void shouldDeleteCategoryWithAdminRole() throws Exception {
                doNothing().when(categoryService).delete(1L);
                mockMvc.perform(delete("/api/categories/1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());
                verify(categoryService).delete(1L);
        }
        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should deny delete for USER role")
        void shouldDenyDeleteForUserRole() throws Exception {
                mockMvc.perform(delete("/api/categories/1")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }
        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
                mockMvc.perform(get("/api/categories"))
                                .andExpect(status().isUnauthorized());
        }
}
