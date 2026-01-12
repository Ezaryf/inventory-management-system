package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.service.interfaces.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Supplier operations.
 */
@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier management APIs")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Returns paginated list of suppliers")
    public ResponseEntity<PagedResponse<SupplierDTO>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(supplierService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Returns a single supplier")
    @ApiResponse(responseCode = "200", description = "Supplier found")
    @ApiResponse(responseCode = "404", description = "Supplier not found")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new supplier", description = "Creates a new supplier")
    @ApiResponse(responseCode = "201", description = "Supplier created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Supplier with this email already exists")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierCreateDTO dto) {
        SupplierDTO created = supplierService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update a supplier", description = "Updates an existing supplier")
    @ApiResponse(responseCode = "200", description = "Supplier updated successfully")
    @ApiResponse(responseCode = "404", description = "Supplier not found")
    @ApiResponse(responseCode = "409", description = "Supplier with this email already exists")
    public ResponseEntity<SupplierDTO> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateDTO dto) {
        return ResponseEntity.ok(supplierService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a supplier", description = "Deletes a supplier")
    @ApiResponse(responseCode = "204", description = "Supplier deleted successfully")
    @ApiResponse(responseCode = "404", description = "Supplier not found")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
