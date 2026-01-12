package com.inventory.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for category response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Integer productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
