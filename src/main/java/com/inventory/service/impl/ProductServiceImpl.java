package com.inventory.service.impl;

import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.interfaces.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ProductService.
 * Follows SRP - only handles product-related operations.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    // Constructor injection (DIP)
    public ProductServiceImpl(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> findAll(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> searchByName(String name, Pageable pageable) {
        Page<Product> page = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> findByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> findBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier", "id", supplierId);
        }
        Page<Product> page = productRepository.findBySupplierId(supplierId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findLowStock() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public ProductDTO create(ProductCreateDTO dto) {
        // Check for duplicate SKU
        if (productRepository.existsBySku(dto.getSku())) {
            throw new DuplicateResourceException("Product", "sku", dto.getSku());
        }

        Product product = Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .description(dto.getDescription())
                .unitPrice(dto.getUnitPrice())
                .currentStock(dto.getInitialStock() != null ? dto.getInitialStock() : 0)
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 10)
                .build();

        // Set category if provided
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        // Set supplier if provided
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));
            product.setSupplier(supplier);
        }

        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Override
    public ProductDTO update(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setUnitPrice(dto.getUnitPrice());

        if (dto.getReorderLevel() != null) {
            product.setReorderLevel(dto.getReorderLevel());
        }

        // Update category if provided
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        // Update supplier if provided
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));
            product.setSupplier(supplier);
        } else {
            product.setSupplier(null);
        }

        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    // Helper methods for mapping
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getCompanyName() : null)
                .unitPrice(product.getUnitPrice())
                .currentStock(product.getCurrentStock())
                .reorderLevel(product.getReorderLevel())
                .lowStock(product.isLowStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private PagedResponse<ProductDTO> mapToPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductDTO>builder()
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
