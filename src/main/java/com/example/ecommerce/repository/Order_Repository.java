package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Order_Repository extends JpaRepository<Order,Long> {

    List<Order> findByUserUserId(Long userId);
}
