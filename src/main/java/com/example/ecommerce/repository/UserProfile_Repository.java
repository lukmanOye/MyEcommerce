package com.example.ecommerce.repository;

import com.example.ecommerce.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfile_Repository extends
        JpaRepository<UserProfile,Long> {
}
