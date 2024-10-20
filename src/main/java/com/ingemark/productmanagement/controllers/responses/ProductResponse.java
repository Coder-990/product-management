package com.ingemark.productmanagement.controllers.responses;

import lombok.Builder;

import java.math.BigDecimal;

@Builder(setterPrefix = "of")
public record ProductResponse(Long id,
                              String code,
                              String name,
                              BigDecimal priceEur,
                              BigDecimal priceUsd,
                              String description,
                              boolean isAvailable) {
}
