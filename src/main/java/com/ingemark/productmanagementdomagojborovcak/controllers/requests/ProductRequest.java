package com.ingemark.productmanagementdomagojborovcak.controllers.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder(setterPrefix = "of")
public record ProductRequest(@Size(min = 10, max = 10, message = "Property must be exactly 10 characters") String code,
                             @NotBlank() String name,
                             @Min(0) BigDecimal priceEur,
                             String description,
                             @NotNull() boolean isAvailable) {
}
