package com.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for creating a new product.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must be less than 200 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be less than 50 characters")
    private String sku;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private Long categoryId;

    private Long supplierId;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "Initial stock must be non-negative")
    @Builder.Default
    private Integer initialStock = 0;

    @Min(value = 0, message = "Reorder level must be non-negative")
    @Builder.Default
    private Integer reorderLevel = 10;
}
