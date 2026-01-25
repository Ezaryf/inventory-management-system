package com.inventory.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDTO {
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must be less than 200 characters")
    private String name;
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    private Long categoryId;
    private Long supplierId;
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;
    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;
}
