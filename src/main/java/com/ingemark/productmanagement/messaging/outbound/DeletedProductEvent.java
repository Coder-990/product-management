package com.ingemark.productmanagement.messaging.outbound;

import com.ingemark.productmanagement.enums.Action;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder(setterPrefix = "of")
public record DeletedProductEvent(
        String eventId,
        ZonedDateTime timestamp,
        Action action,
        Product product) {
    @Builder(setterPrefix = "of")
    public record Product(
            Long id) {
    }
}
