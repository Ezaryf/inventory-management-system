package com.inventory.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for supplier response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Long id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private Integer productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
