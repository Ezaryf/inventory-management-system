package com.inventory.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCreateDTO {
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must be less than 200 characters")
    private String companyName;
    @Size(max = 100, message = "Contact person name must be less than 100 characters")
    private String contactPerson;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;
    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;
}
