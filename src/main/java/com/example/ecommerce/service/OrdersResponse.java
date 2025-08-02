package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersResponse {
    private List<Order> orders;
    private BigDecimal totalOfAllOrders;
}