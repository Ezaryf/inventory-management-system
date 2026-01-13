package com.inventory.service.impl;

import com.inventory.dto.*;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.interfaces.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SupplierService.
 * Follows SRP - only handles supplier-related operations.
 */
@Service
@Transactional
@SuppressWarnings("null")
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    // Constructor injection (DIP)
    public SupplierServiceImpl(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return mapToDTO(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SupplierDTO> findAll(Pageable pageable) {
        Page<Supplier> page = supplierRepository.findAll(pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public SupplierDTO create(SupplierCreateDTO dto) {
        // Check for duplicate email
        if (supplierRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new DuplicateResourceException("Supplier", "email", dto.getEmail());
        }

        Supplier supplier = Supplier.builder()
                .companyName(dto.getCompanyName())
                .contactPerson(dto.getContactPerson())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return mapToDTO(saved);
    }

    @Override
    public SupplierDTO update(Long id, SupplierUpdateDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        // Check for duplicate email (excluding current supplier)
        supplierRepository.findByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)
                .ifPresent(s -> {
                    throw new DuplicateResourceException("Supplier", "email", dto.getEmail());
                });

        supplier.setCompanyName(dto.getCompanyName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());

        Supplier saved = supplierRepository.save(supplier);
        return mapToDTO(saved);
    }

    @Override
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier", "id", id);
        }
        supplierRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return supplierRepository.existsByEmailIgnoreCase(email);
    }

    // Helper methods for mapping
    private SupplierDTO mapToDTO(Supplier supplier) {
        int productCount = (int) productRepository.countBySupplierId(supplier.getId());

        return SupplierDTO.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .productCount(productCount)
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    private PagedResponse<SupplierDTO> mapToPagedResponse(Page<Supplier> page) {
        return PagedResponse.<SupplierDTO>builder()
                .content(page.getContent().stream().map(this::mapToDTO).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
