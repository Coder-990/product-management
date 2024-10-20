package com.ingemark.productmanagement.controllers;

import com.ingemark.productmanagement.controllers.requests.ProductRequest;
import com.ingemark.productmanagement.controllers.responses.ProductResponse;
import com.ingemark.productmanagement.controllers.responses.ProductsResponse;
import com.ingemark.productmanagement.mappers.ProductMapper;
import com.ingemark.productmanagement.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ProductMapper productMapper;

    @GetMapping("/products")
    public ProductsResponse getAll(
            @RequestParam(required = false, defaultValue = "0")
            @Min(message = "attribute page must be positive number", value = 0)
            @Max(message = "attribute page must be below than 100", value = 100) int page,
            @RequestParam(required = false, defaultValue = "10")
            @Min(message = "attribute size must be greater than 1", value = 1)
            @Max(message = "attribute size must be below than 100", value = 100) int size,
            @RequestParam(required = false) String name) {
        log.info("Fetching all products for page {}, size {}, name {}...", page, size, name);
        var products = productService.getAll(page, size, name);
        var productsResponse = productMapper.toProductsResponse(products);
        log.info("Fetched {} products", productsResponse.products().size());
        return productsResponse;
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        log.info("Fetching product with id {}... ", id);
        var product = productService.getById(id);
        var productResponse = productMapper.toProductResponse(product);
        log.info("Fetched product with id {}", id);
        return productResponse;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody @Valid ProductRequest productRequest) {
        log.info("Creating product with code {}...", productRequest.code());
        var product = productMapper.toProduct(productRequest);
        var productResponse = productMapper.toProductResponse(productService.create(product));
        log.info("Created product for code {} with id {}", productRequest.code(), productResponse.id());
        return productResponse;
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@RequestBody @Valid ProductRequest productRequest, @PathVariable Long id) {
        log.info("Updating product with id {}...", id);
        var product = productMapper.toProduct(productRequest);
        var productResponse = productMapper.toProductResponse(productService.update(product, id));
        log.info("Updated product with id {}", id);
        return productResponse;
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductById(@PathVariable Long id) {
        log.info("Removing product with id {}...", id);
        productService.removeProduct(id);
        log.info("Removed product with id {}", id);
    }
}
