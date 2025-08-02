package com.example.ecommerce.controller;

import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.OrdersResponse;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/myEcommerce/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestParam Long userId,
            @RequestBody List<OrderItem> orderItems) {
        Order order = orderService.createOrder(userId, orderItems);
        return ResponseEntity.status(201).body(order);
    }

    @GetMapping
    public ResponseEntity<OrdersResponse> getOrders(@RequestParam Long userId) {
        OrdersResponse response = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(response);
    }
}