package com.inventory.service.interfaces;

import com.inventory.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Product operations.
 */
public interface ProductService {

    ProductDTO findById(Long id);

    ProductDTO findBySku(String sku);

    PagedResponse<ProductDTO> findAll(Pageable pageable);

    PagedResponse<ProductDTO> searchByName(String name, Pageable pageable);

    PagedResponse<ProductDTO> findByCategory(Long categoryId, Pageable pageable);

    PagedResponse<ProductDTO> findBySupplier(Long supplierId, Pageable pageable);

    List<ProductDTO> findLowStock();

    ProductDTO create(ProductCreateDTO dto);

    ProductDTO update(Long id, ProductUpdateDTO dto);

    void delete(Long id);

    boolean existsBySku(String sku);
}
