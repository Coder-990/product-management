package com.ingemark.productmanagement.repositories;

import com.ingemark.productmanagement.models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Transactional
    void deleteProductById(Long id);

    List<Product> findAllByNameIsContainingIgnoreCase(String name, Pageable pageable);
}
