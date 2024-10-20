package com.ingemark.productmanagementdomagojborovcak.fixtures;

import com.ingemark.productmanagementdomagojborovcak.controllers.requests.ProductRequest;
import com.ingemark.productmanagementdomagojborovcak.models.Product;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder(setterPrefix = "of")
public class ProductFixtures {

    private ProductFixtures() {
    }
    public static List<Product> getProductList() {
        return List.of(
                productOne(),
                productTwo(),
                productThree(),
                productFour(),
                productFive());
    }
    public static Product productOne() {
        return Product.builder()
                .ofCode("125g4fHZH8")
                .ofName("Game")
                .ofPriceEur(BigDecimal.valueOf(25.99))
                .ofDescription("This is Game product")
                .ofIsAvailable(true)
                .build();
    }
    public static Product productTwo() {
        return Product.builder()
                .ofCode("7gfgrRHZH8")
                .ofName("Game Boy")
                .ofPriceEur(BigDecimal.valueOf(159.99))
                .ofDescription("This is Game Boy product")
                .ofIsAvailable(true)
                .build();
    }
    public static Product productThree() {
        return Product.builder()
                .ofCode("854TGFoi98")
                .ofName("Desktop PC")
                .ofPriceEur(BigDecimal.valueOf(2049.54))
                .ofDescription("This is Desktop PC product")
                .ofIsAvailable(true)
                .build();
    }

    public static Product productFour() {
        return Product.builder()
                .ofCode("4g1b5s2cRR")
                .ofName("PS5")
                .ofPriceEur(BigDecimal.valueOf(399.54))
                .ofDescription("This is Playstation 5 product")
                .ofIsAvailable(false)
                .build();
    }
    public static Product productFive() {
        return Product.builder()
                .ofCode("1234567abc")
                .ofName("Keyboard")
                .ofPriceEur(BigDecimal.valueOf(99.42))
                .ofDescription("This is keyboard product")
                .ofIsAvailable(false)
                .build();
    }
    public static Product newProduct() {
        return Product.builder()
                .ofCode("tkgf14fdFv")
                .ofName("God of war Game")
                .ofPriceEur(BigDecimal.valueOf(75.99))
                .ofDescription("Great action game")
                .ofIsAvailable(true)
                .build();
    }
    public static ProductRequest newProductRequest() {
        return ProductRequest.builder()
                .ofCode("tkEf14dDFv")
                .ofName("God of war Ragnarok Game")
                .ofPriceEur(BigDecimal.valueOf(95.99))
                .ofDescription("Great action game")
                .ofIsAvailable(true)
                .build();
    }
    public static ProductRequest productRequestOne() {
        return ProductRequest.builder()
                .ofCode("7gfgrRHZH8")
                .ofName("Keyboard")
                .ofPriceEur(BigDecimal.valueOf(124.42))
                .ofDescription("Logitech k860 wireless keyboard")
                .ofIsAvailable(true)
                .build();
    }
    public static ProductRequest productRequestTwo() {
        return ProductRequest.builder()
                .ofCode("1234567abc")
                .ofName("Keyboard")
                .ofPriceEur(BigDecimal.valueOf(124.42))
                .ofDescription("Logitech k860 wireless keyboard")
                .ofIsAvailable(true)
                .build();
    }
}
