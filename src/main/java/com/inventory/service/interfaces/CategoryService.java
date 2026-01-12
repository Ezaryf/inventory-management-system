package com.inventory.service.interfaces;

import com.inventory.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    CategoryDTO findById(Long id);

    PagedResponse<CategoryDTO> findAll(Pageable pageable);

    CategoryDTO create(CategoryCreateDTO dto);

    CategoryDTO update(Long id, CategoryUpdateDTO dto);

    void delete(Long id);

    boolean existsByName(String name);
}
