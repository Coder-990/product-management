package com.ingemark.productmanagementdomagojborovcak.messaging.outbound;

import com.ingemark.productmanagementdomagojborovcak.enums.Action;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;


@Builder(setterPrefix = "of")
public record UpdatedProductEvent(
        UUID eventId,
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
