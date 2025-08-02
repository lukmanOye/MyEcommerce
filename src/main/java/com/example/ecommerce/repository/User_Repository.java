package com.example.ecommerce.repository;

import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;



public interface User_Repository extends JpaRepository <User ,Long> {


    User findByEmail(String email);
}
