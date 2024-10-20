package com.ingemark.productmanagementdomagojborovcak.messaging.outbound;

import com.ingemark.productmanagementdomagojborovcak.enums.Action;
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
