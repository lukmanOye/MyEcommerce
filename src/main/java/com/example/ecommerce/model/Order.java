package com.example.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-order")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference("order-item")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private OrderStatus status;

    @Column(name = "total_amount", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal totalAmount;

    public void addOrderItem(OrderItem item) {
        if (item != null) {
            item.setOrder(this);
            item.setUser(this.user);
            item.calculateSubtotal();
            orderItems.add(item);
            calculateTotal();
        }
    }

    public void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                if (item.getSubtotal() != null) {
                    total = total.add(item.getSubtotal());
                }
            }
        }
        this.totalAmount = total;
    }
}