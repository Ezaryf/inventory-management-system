package com.inventory.service.interfaces;

import com.inventory.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Supplier operations.
 */
public interface SupplierService {

    SupplierDTO findById(Long id);

    PagedResponse<SupplierDTO> findAll(Pageable pageable);

    SupplierDTO create(SupplierCreateDTO dto);

    SupplierDTO update(Long id, SupplierUpdateDTO dto);

    void delete(Long id);

    boolean existsByEmail(String email);
}
