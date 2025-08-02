package com.example.ecommerce.service;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.Order_Repository;
import com.example.ecommerce.repository.Product_Repository;
import com.example.ecommerce.repository.User_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private Order_Repository orderRepository;

    @Autowired
    private User_Repository userRepository;

    @Autowired
    private Product_Repository productRepository;

    @Transactional
    public Order createOrder(Long userId, List<OrderItem> orderItems) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validate order items
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();

        // Process order items
        for (OrderItem item : orderItems) {
            // Validate product
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getProductId()));

            // Validate quantity
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for product: " + product.getName());
            }
            if (item.getQuantity() > product.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getQuantity());
            }

            // Set product details
            item.setProduct(product);
            item.setUser(user);
            item.setUnitPrice(product.getPrice());
            item.setProductNameSnapshot(product.getName());
            item.calculateSubtotal();

            // Update product quantity
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);

            // Add to order
            order.addOrderItem(item);
        }

        // Save order (cascades to order items)
        return orderRepository.save(order);
    }

    public OrdersResponse getOrdersByUserId(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        List<Order> orders = orderRepository.findByUserUserId(userId);

        // Calculate total of all orders
        BigDecimal totalOfAllOrders = orders.stream()
                .map(Order::getTotalAmount)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrdersResponse.builder()
                .orders(orders)
                .totalOfAllOrders(totalOfAllOrders)
                .build();
    }
}