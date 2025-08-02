
package com.example.ecommerce.controller;

import com.example.ecommerce.model.User;
import com.example.ecommerce.model.UserAddress;
import com.example.ecommerce.model.UserProfile;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/myEcommerce")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.debug("Fetching user with id: {}", id);
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/createUser")
    public ResponseEntity<User> createUser(@RequestBody User user,
                                           @RequestParam(required = false) Set<String> roles) {
        logger.debug("Creating user: {}", user.getEmail());
        try {
            User createdUser = userService.createUser(user, roles);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestParam Long id, @RequestBody User userDetails) {
        logger.debug("Updating user with id: {}", id);
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Void> deleteUser(@RequestParam Long id) {
        logger.debug("Deleting user with id: {}", id);
        try {
            userService.deleteUserById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/address")
    public ResponseEntity<User> addAddress(@PathVariable Long userId, @RequestBody UserAddress address) {
        logger.debug("Adding address for userId: {}", userId);
        try {
            User updatedUser = userService.addAddress(userId, address);
            return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding address: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{userId}/address/{addressId}")
    public ResponseEntity<User> removeAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        logger.debug("Removing addressId: {} for userId: {}", addressId, userId);
        try {
            User updatedUser = userService.removeAddress(userId, addressId);
            return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error removing address: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{userId}/profile")
    public ResponseEntity<User> addProfile(@PathVariable Long userId, @RequestBody UserProfile profile) {
        logger.debug("Adding profile for userId: {}", userId);
        try {
            User updatedUser = userService.addProfile(userId, profile);
            return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{userId}/profile/{profileId}")
    public ResponseEntity<User> removeProfile(@PathVariable Long userId, @PathVariable Long profileId) {
        logger.debug("Removing profileId: {} for userId: {}", profileId, userId);
        try {
            User updatedUser = userService.removeProfile(userId, profileId);
            return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error removing profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<User> addOrder(@PathVariable Long userId, @RequestBody Order order) {
        logger.debug("Adding order for userId: {}", userId);
        try {
            User updatedUser = userService.addOrder(userId, order);
            return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{userId}/order/{orderId}")
    public ResponseEntity<User> removeOrder(@PathVariable Long userId, @PathVariable Long orderId) {
        logger.debug("Removing orderId: {} for userId: {}", orderId, userId);
        try {
            User updatedUser = userService.removeOrder(userId, orderId);
            return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error removing order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
