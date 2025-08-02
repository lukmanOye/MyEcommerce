package com.example.ecommerce.repository;

import com.example.ecommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItem_Repository extends JpaRepository<OrderItem,Long> {
}
