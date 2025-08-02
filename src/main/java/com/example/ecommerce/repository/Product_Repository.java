package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface Product_Repository extends JpaRepository<Product,Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findByName(String productNameSnapshot);
}
