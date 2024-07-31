package com.webflux.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webflux.util.ValidCustomersCount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("orders")
public class Order {
    @Id
    private Long orderNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Telephone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    private String telephoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @ValidCustomersCount
    private int numberOfCustomers;

    private BigDecimal orderTotal;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;
}
