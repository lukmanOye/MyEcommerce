package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.service.OrderService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/myEcommerce/payments")
@Validated
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/process/{orderId}")
    public ResponseEntity<?> processPayment(@PathVariable Long orderId,
                                            @RequestParam Long userId,
                                            @Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            Order order = orderService.processPayment(orderId, paymentRequest.getPaymentMethodId(), userId);
            String deliverLink = "/myEcommerce/payments/mark-delivered/" + order.getOrderId() + "?userId=" + userId;
            PaymentSuccessResponse response = new PaymentSuccessResponse(order, deliverLink);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Payment processing failed: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/process-all")
    public ResponseEntity<?> processAllPayments(@RequestParam Long userId,
                                                @Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            List<Order> orders = orderService.processAllPayments(userId, paymentRequest.getPaymentMethodId());
            return ResponseEntity.ok(orders);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Payment processing failed: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/mark-delivered/{orderId}")
    public ResponseEntity<?> markAsDelivered(@PathVariable Long orderId,
                                             @RequestParam Long userId,
                                             @RequestParam(required = false) Long addressId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (!order.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Order does not belong to user: " + userId);
            }

            if (order.getStatus() != OrderStatus.PAID) {
                throw new IllegalStateException("Order must be in PAID status to initiate shipping");
            }

            if (addressId != null) {
                orderService.initiateShippingWithAddress(order, userId, addressId);
            } else {
                orderService.initiateShipping(order, userId);
            }

            order = orderService.markAsDelivered(orderId, userId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            orderService.cancelOrder(orderId, userId);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Delivery failed, order cancelled: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(new PaymentStatusResponse(order.getOrderId(), order.getStatus().name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    static class PaymentRequest {
        @NotBlank(message = "Payment method ID is required")
        private String paymentMethodId;

        public String getPaymentMethodId() {
            return paymentMethodId;
        }

        public void setPaymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }
    }

    static class PaymentSuccessResponse {
        private Order order;
        private String deliverLink;

        public PaymentSuccessResponse(Order order, String deliverLink) {
            this.order = order;
            this.deliverLink = deliverLink;
        }

        public Order getOrder() {
            return order;
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public String getDeliverLink() {
            return deliverLink;
        }

        public void setDeliverLink(String deliverLink) {
            this.deliverLink = deliverLink;
        }
    }

    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    static class PaymentStatusResponse {
        private Long orderId;
        private String status;

        public PaymentStatusResponse(Long orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}