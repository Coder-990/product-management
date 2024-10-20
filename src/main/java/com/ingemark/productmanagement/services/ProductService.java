package com.ingemark.productmanagement.services;


import com.ingemark.productmanagement.exceptions.NotFoundException;
import com.ingemark.productmanagement.mappers.ProductMapper;
import com.ingemark.productmanagement.messaging.KafkaSender;
import com.ingemark.productmanagement.models.Product;
import com.ingemark.productmanagement.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaSender kafkaSender;
    private final ProductMapper productMapper;

    public List<Product> getAll(int page, int size, String name) {
        var sort = Sort.by("id");
        var pageRequest = PageRequest.of(page, size, sort);
        return (Objects.isNull(name)) ?
                productRepository.findAll(pageRequest).stream().toList() :
                productRepository.findAllByNameIsContainingIgnoreCase(name, pageRequest);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Could not find product by this id %d".formatted(id)));
    }

    public Product create(Product product) {
        var createdProduct = productRepository.save(product);
        var createdProductEvent = productMapper.toCreatedProductEvent(createdProduct);
        kafkaSender.sendProductCreatedEvent(createdProductEvent);
        return createdProduct;
    }

    public Product update(Product product, Long id) {
        var existingProduct = getById(id);
        existingProduct.setCode(product.getCode());
        existingProduct.setName(product.getName());
        existingProduct.setPriceEur(product.getPriceEur());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setIsAvailable(product.getIsAvailable());
        var updatedProduct = productRepository.saveAndFlush(existingProduct);
        var updatedProductEvent = productMapper.toUpdatedProductEvent(updatedProduct);
        kafkaSender.sendProductUpdatedEvent(updatedProductEvent);
        return updatedProduct;
    }

    public void removeProduct(Long id) {
        var product = getById(id);
        productRepository.deleteProductById(product.getId());
        var deletedProductEvent = productMapper.toDeletedProductEvent(product);
        kafkaSender.sendProductDeletedEvent(deletedProductEvent);
    }
}
