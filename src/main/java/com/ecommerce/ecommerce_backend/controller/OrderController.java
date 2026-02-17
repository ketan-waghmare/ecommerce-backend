package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.OrderDTO;
import com.ecommerce.ecommerce_backend.dto.PlaceOrderRequest;
import com.ecommerce.ecommerce_backend.entity.Order;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.services.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper method to get userId from authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUserId();
        }

        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        return null;
    }

    /**
     * Place new order - NO cartId needed!
     * Gets cart automatically using userId from JWT token
     */
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            Authentication authentication) {

        // Extract userId from JWT token
        Long userId = getUserIdFromAuthentication(authentication);

        if (userId == null) {
            log.error("❌ User not authenticated");
            return ResponseEntity.status(401).body("User not authenticated");
        }

        log.info("=== PLACE ORDER REQUEST ===");
        log.info("User ID: {}", userId);
        log.info("Shipping Address: {}", request.getShippingAddress());
        log.info("City: {}", request.getShippingCity());
        log.info("State: {}", request.getShippingState());
        log.info("Payment Method: {}", request.getPaymentMethod());
        log.info("========================");

        try {
            // Place order - service will find cart using userId
            Order order = orderService.placeOrder(request, userId);

            log.info("=== ORDER CREATED SUCCESSFULLY ===");
            log.info("Order Number: {}", order.getOrderNumber());
            log.info("Order ID: {}", order.getId());
            log.info("User ID: {}", order.getUserId());
            log.info("Total: ${}", order.getTotalAmount());
            log.info("Status: {}", order.getStatus());
            log.info("Items: {}", order.getItems().size());
            log.info("================================");

            OrderDTO orderDTO = OrderDTO.fromEntity(order);
            return ResponseEntity.ok(orderDTO);

        } catch (Exception e) {
            log.error("❌ Failed to place order: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Failed to place order: " + e.getMessage());
        }
    }

    /**
     * Get user's order history
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        log.info("📋 Fetching orders for user: {}", userId);

        try {
            List<Order> orders = orderService.getUserOrders(userId);

            List<OrderDTO> orderDTOs = orders.stream()
                    .map(OrderDTO::fromEntity)
                    .collect(Collectors.toList());

            log.info("✅ Found {} orders for user {}", orders.size(), userId);
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            log.error("❌ Failed to fetch orders: {}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to fetch orders: " + e.getMessage());
        }
    }

    /**
     * Get specific order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            Order order = orderService.getOrderById(orderId);

            // Security check - user can only view their own orders
            if (!order.getUserId().equals(userId)) {
                log.warn("⚠️ User {} tried to access order {} belonging to user {}",
                        userId, orderId, order.getUserId());
                return ResponseEntity.status(403).body("Access denied");
            }

            OrderDTO orderDTO = OrderDTO.fromEntity(order);
            return ResponseEntity.ok(orderDTO);

        } catch (Exception e) {
            log.error("❌ Failed to fetch order: {}", e.getMessage());
            return ResponseEntity.status(404).body("Order not found");
        }
    }

    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Check if user is admin
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            log.warn("⚠️ Non-admin user {} tried to update order status", userId);
            return ResponseEntity.status(403).body("Admin access required");
        }

        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            OrderDTO orderDTO = OrderDTO.fromEntity(order);

            log.info("✅ Order {} status updated to {} by admin {}", orderId, status, userId);
            return ResponseEntity.ok(orderDTO);

        } catch (Exception e) {
            log.error("❌ Failed to update order status: {}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to update status: " + e.getMessage());
        }
    }

    /**
     * Get all orders (Admin only)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Check if user is admin
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            log.warn("⚠️ Non-admin user {} tried to access all orders", userId);
            return ResponseEntity.status(403).body("Admin access required");
        }

        try {
            List<Order> orders = orderService.getAllOrders();

            List<OrderDTO> orderDTOs = orders.stream()
                    .map(OrderDTO::fromEntity)
                    .collect(Collectors.toList());

            log.info("✅ Admin {} fetched {} orders", userId, orders.size());
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            log.error("❌ Failed to fetch all orders: {}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to fetch orders: " + e.getMessage());
        }
    }
}