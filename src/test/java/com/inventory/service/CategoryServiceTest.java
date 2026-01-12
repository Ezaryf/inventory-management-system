package com.inventory.service;

import com.inventory.dto.CategoryCreateDTO;
import com.inventory.dto.CategoryDTO;
import com.inventory.dto.CategoryUpdateDTO;
import com.inventory.entity.Category;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .build();
    }

    @Test
    @DisplayName("Should return category when found by ID")
    void shouldReturnCategoryWhenFoundById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.countByCategoryId(1L)).thenReturn(5L);

        CategoryDTO result = categoryService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getProductCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
        CategoryCreateDTO createDTO = CategoryCreateDTO.builder()
                .name("Clothing")
                .description("Apparel and accessories")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Clothing")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(productRepository.countByCategoryId(any())).thenReturn(0L);

        CategoryDTO result = categoryService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Clothing");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate category name")
    void shouldThrowExceptionForDuplicateName() {
        CategoryCreateDTO createDTO = CategoryCreateDTO.builder()
                .name("Electronics")
                .description("Another electronics category")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category")
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategorySuccessfully() {
        CategoryUpdateDTO updateDTO = CategoryUpdateDTO.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameIgnoreCaseAndIdNot("Updated Electronics", 1L))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(productRepository.countByCategoryId(1L)).thenReturn(5L);

        CategoryDTO result = categoryService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete category successfully")
    void shouldDeleteCategorySuccessfully() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void shouldThrowExceptionWhenDeletingNonExistent() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
