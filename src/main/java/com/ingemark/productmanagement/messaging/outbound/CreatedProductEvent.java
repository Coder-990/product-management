package com.ingemark.productmanagement.messaging.outbound;

import com.ingemark.productmanagement.enums.Action;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder(setterPrefix = "of")
public record CreatedProductEvent(
        String eventId,
        ZonedDateTime timestamp,
        Action action,
        Product product) {
    @Builder(setterPrefix = "of")
    public record Product(
            Long id,
            String code,
            String name,
            BigDecimal priceEur,
            String description,
            Boolean isAvailable) {
    }
}
