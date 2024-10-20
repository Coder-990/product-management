package com.ingemark.productmanagementdomagojborovcak.messaging.outbound;

import com.ingemark.productmanagementdomagojborovcak.enums.Action;
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
