package com.example.ecommerce.service;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.Order_Repository;
import com.example.ecommerce.repository.Product_Repository;
import com.example.ecommerce.repository.Role_Repository;
import com.example.ecommerce.repository.User_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final User_Repository userRepository;
    private final Role_Repository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;
    private final Product_Repository productRepository;
    private final Order_Repository order_repository;

    @Autowired
    public UserService(User_Repository userRepository, Role_Repository roleRepository, PasswordEncoder passwordEncoder, OrderService orderService, Product_Repository productRepository, Order_Repository order_repository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.order_repository = order_repository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user, Set<String> roleNames) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalArgumentException("USER role not found"));
            roles.add(userRole);
        } else {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
                roles.add(role);
            }
        }
        user.setRoles(roles);

        if (user.getUserAddresses() != null) {
            user.getUserAddresses().forEach(address -> address.setUser(user));
        }
        if (user.getUserProfile() != null) {
            user.getUserProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User createOrUpdateOAuthUser(String email, String name) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user;
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found"));

        user = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Secure random password
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        try {
            return userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                return existingUser;
            }
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }

    @Transactional
    public User addProfile(Long userId, UserProfile profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }
        UserProfile existingProfile = user.getUserProfile();
        if (existingProfile != null) {
            existingProfile.setBio(profile.getBio());
            existingProfile.setDateOfBirth(profile.getDateOfBirth());
            existingProfile.setPhoneNumber(profile.getPhoneNumber());
        } else {
            profile.setUser(user);
            user.setUserProfile(profile);
        }
        return userRepository.save(user);
    }

    public User createOAuthUser(String email, String name) {
        if (userRepository.findByEmail(email) != null) {
            return userRepository.findByEmail(email);
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalArgumentException("USER role not found"));
        user.setRoles(Set.of(userRole));
        return userRepository.save(user);
    }

    public void deleteUserById(Long Id) {
        var user = userRepository.findById(Id).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        userRepository.delete(user);
    }

    @Transactional
    public User addAddress(Long userId, UserAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        address.setUser(user);
        user.getUserAddresses().add(address);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public User removeAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        UserAddress addressToRemove = user.getUserAddresses().stream()
                .filter(address -> address.getAddressId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found: " + addressId));
        user.getUserAddresses().remove(addressToRemove);
        return userRepository.save(user);
    }

    public String welcome(User user) {
        return "Welcome to My Ecommerce " + user.getName();
    }

    @Transactional
    public User removeProfile(Long userId, Long profileId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        UserProfile profile = user.getUserProfile();
        if (profile == null || !profile.getProfileId().equals(profileId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found: " + profileId);
        }
        user.setUserProfile(null);
        return userRepository.save(user);
    }

    @Transactional
    public User addOrder(Long userId, Order order) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        if (order == null || order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        Order createdOrder = orderService.createOrder(userId, order.getOrderItems());
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    @Transactional
    public User removeOrder(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        Order order = order_repository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        if (!order.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order does not belong to user: " + userId);
        }
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getProductId()));
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        user.getOrders().remove(order);
        order_repository.delete(order);
        return userRepository.save(user);
    }
}