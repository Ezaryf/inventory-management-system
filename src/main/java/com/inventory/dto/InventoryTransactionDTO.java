package com.inventory.dto;
import com.inventory.entity.TransactionType;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private TransactionType transactionType;
    private Integer quantity;
    private String referenceNumber;
    private String notes;
    private LocalDateTime transactionDate;
    private String createdBy;
}
