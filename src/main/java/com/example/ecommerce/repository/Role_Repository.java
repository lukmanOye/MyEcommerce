package com.example.ecommerce.repository;

import com.example.ecommerce.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Role_Repository extends JpaRepository<Role, Long>{
    Optional<Role>findByName(String name);
}
