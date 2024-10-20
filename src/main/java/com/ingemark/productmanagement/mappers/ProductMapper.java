package com.ingemark.productmanagement.mappers;

import com.ingemark.productmanagement.cache.CurrencyCacheService;
import com.ingemark.productmanagement.controllers.requests.ProductRequest;
import com.ingemark.productmanagement.controllers.responses.ProductResponse;
import com.ingemark.productmanagement.controllers.responses.ProductsResponse;
import com.ingemark.productmanagement.enums.Action;
import com.ingemark.productmanagement.messaging.outbound.CreatedProductEvent;
import com.ingemark.productmanagement.messaging.outbound.DeletedProductEvent;
import com.ingemark.productmanagement.messaging.outbound.UpdatedProductEvent;
import com.ingemark.productmanagement.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CurrencyCacheService currencyCacheService;

    public Product toProduct(ProductRequest productRequest) {
        return Product.builder()
                .ofCode(productRequest.code())
                .ofName(productRequest.name())
                .ofPriceEur(productRequest.priceEur())
                .ofDescription(productRequest.description())
                .ofIsAvailable(productRequest.isAvailable())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .ofId(product.getId())
                .ofCode(product.getCode())
                .ofName(product.getName())
                .ofPriceEur(product.getPriceEur())
                .ofPriceUsd(convertToUsdPrice(product.getPriceEur()))
                .ofDescription(product.getDescription())
                .ofIsAvailable(product.getIsAvailable())
                .build();
    }

    public ProductsResponse toProductsResponse(List<Product> products) {
        var productResponseList = products
                .stream()
                .map(this::toProductResponse)
                .toList();
        return ProductsResponse.builder()
                .ofProducts(productResponseList)
                .build();
    }

    public BigDecimal convertToUsdPrice(BigDecimal priceEur) {
        return priceEur.multiply(currencyCacheService.getCurrencyUSD())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public CreatedProductEvent toCreatedProductEvent(Product product) {
        return CreatedProductEvent
                .builder()
                .ofEventId(UUID.randomUUID().toString())
                .ofTimestamp(getUTCZonedDateTimeNow())
                .ofAction(Action.CREATE)
                .ofProduct(CreatedProductEvent.Product.builder()
                        .ofId(product.getId())
                        .ofCode(product.getCode())
                        .ofName(product.getName())
                        .ofPriceEur(product.getPriceEur())
                        .ofDescription(product.getDescription())
                        .ofIsAvailable(product.getIsAvailable())
                        .build())
                .build();
    }

    public UpdatedProductEvent toUpdatedProductEvent(Product product) {
        return UpdatedProductEvent
                .builder()
                .ofEventId(UUID.randomUUID())
                .ofTimestamp(getUTCZonedDateTimeNow())
                .ofAction(Action.UPDATE)
                .ofProduct(UpdatedProductEvent.Product.builder()
                        .ofId(product.getId())
                        .ofCode(product.getCode())
                        .ofName(product.getName())
                        .ofPriceEur(product.getPriceEur())
                        .ofDescription(product.getDescription())
                        .ofIsAvailable(product.getIsAvailable())
                        .build())
                .build();
    }

    public DeletedProductEvent toDeletedProductEvent(Product product) {
        return DeletedProductEvent
                .builder()
                .ofEventId(UUID.randomUUID().toString())
                .ofAction(Action.DELETE)
                .ofTimestamp(getUTCZonedDateTimeNow())
                .ofProduct(DeletedProductEvent.Product.builder()
                        .ofId(product.getId())
                        .build())
                .build();
    }

    private ZonedDateTime getUTCZonedDateTimeNow() {
        return ZonedDateTime.parse(ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
    }
}
