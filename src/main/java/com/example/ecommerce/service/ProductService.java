package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.Product_Repository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class ProductService {
    @Autowired
    private Product_Repository productRepository;

    @Transactional
    public Product createProduct(@Valid Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional
    public Product updateProduct(Long id, @Valid Product product) {
        Product existingProduct = getProductById(id);

        // Update fields
        existingProduct.setName(product.getName());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDescription(product.getDescription());

        return productRepository.save(existingProduct);
    }
}