package com.ecommerce.ecommerce_backend.services;

import com.ecommerce.ecommerce_backend.dto.PlaceOrderRequest;
import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    /**
     * Place order with shipping information
     * Gets cart automatically using userId (no cartId needed!)
     */
    @Transactional
    public Order placeOrder(PlaceOrderRequest request, Long userId) {
        log.info("📦 Placing order for user: {}", userId);

        // Validate userId
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        // Get user's cart using userId (NOT cartId!)
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // Validate cart has items
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Please add items before placing order.");
        }

        log.info("Found cart with {} items for user {}", cart.getItems().size(), userId);

        // Create new order
        Order order = new Order();

        // Set user ID
        order.setUserId(userId);

        // Generate unique order number
        order.setOrderNumber(generateOrderNumber());

        // Set order date
        order.setOrderDate(LocalDateTime.now());

        // Set status
        order.setStatus("PENDING");

        // Set shipping information
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingPhone(request.getShippingPhone());

        // Set payment information
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("UNPAID");

        // Calculate total and create order items
        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            log.info("Processing cart item: {} x{}", product.getName(), cartItem.getQuantity());

            // Validate stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                                product.getName(), product.getStock(), cartItem.getQuantity())
                );
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            // CRITICAL: Save product snapshot (preserves data even if product changes/deletes)
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getImageUrl());

            // Calculate subtotal
            double subtotal = cartItem.getPrice() * cartItem.getQuantity();
            total += subtotal;

            log.info("  - {} x{} @ ${} = ${}",
                    product.getName(), cartItem.getQuantity(), cartItem.getPrice(), subtotal);

            orderItems.add(orderItem);

            // Reduce product stock
            int newStock = product.getStock() - cartItem.getQuantity();
            product.setStock(newStock);
            productRepository.save(product);

            log.info("  - Stock updated: {} -> {}", product.getStock() + cartItem.getQuantity(), newStock);
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        // Save order
        Order savedOrder = orderRepository.save(order);

        log.info("✅ Order created successfully:");
        log.info("   Order Number: {}", savedOrder.getOrderNumber());
        log.info("   Order ID: {}", savedOrder.getId());
        log.info("   User ID: {}", savedOrder.getUserId());
        log.info("   Total: ${}", savedOrder.getTotalAmount());
        log.info("   Status: {}", savedOrder.getStatus());
        log.info("   Items: {}", savedOrder.getItems().size());
        log.info("   Shipping: {}, {}, {}",
                savedOrder.getShippingAddress(),
                savedOrder.getShippingCity(),
                savedOrder.getShippingState());

        // Clear cart after successful order
        clearCart(cart);

        return savedOrder;
    }

    /**
     * Generate unique order number in format: ORD-2026-00001
     */
    private String generateOrderNumber() {
        String year = String.valueOf(Year.now().getValue());
        long count = orderRepository.count() + 1;
        String orderNumber = String.format("ORD-%s-%05d", year, count);

        // Ensure uniqueness (in case of concurrent orders)
        while (orderRepository.findByOrderNumber(orderNumber).isPresent()) {
            count++;
            orderNumber = String.format("ORD-%s-%05d", year, count);
        }

        log.info("Generated order number: {}", orderNumber);
        return orderNumber;
    }

    /**
     * Clear cart after order is placed
     */
    private void clearCart(Cart cart) {
        if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {

            int itemCount = cart.getItems().size();

            // Delete all cart items
            cartItemRepository.deleteAll(cart.getItems());

            // Clear items list and reset total
            cart.getItems().clear();
            cart.setTotalAmount(0.0);

            // Save cart
            cartRepository.save(cart);

            log.info("🗑️  Cart cleared: {} items removed for user {}", itemCount, cart.getUser().getId());
        }
    }

    /**
     * Get user's order history (newest first)
     */
    public List<Order> getUserOrders(Long userId) {
        log.info("📋 Fetching order history for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Found {} orders for user {}", orders.size(), userId);
        return orders;
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    /**
     * Get order by order number
     */
    public Order getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    /**
     * Update order status (for admin)
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = getOrderById(orderId);
        String oldStatus = order.getStatus();
        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

        log.info("✅ Order {} status updated: {} -> {}", orderId, oldStatus, newStatus);

        return updatedOrder;
    }

    /**
     * Update payment status (after payment gateway confirmation)
     */
    @Transactional
    public Order updatePaymentStatus(Long orderId, String paymentStatus) {
        log.info("Updating order {} payment status to {}", orderId, paymentStatus);

        Order order = getOrderById(orderId);
        order.setPaymentStatus(paymentStatus);

        // If payment is successful, confirm the order
        if ("PAID".equals(paymentStatus) && "PENDING".equals(order.getStatus())) {
            order.setStatus("CONFIRMED");
            log.info("Order {} auto-confirmed after successful payment", orderId);
        }

        return orderRepository.save(order);
    }

    /**
     * Get all orders (for admin)
     */
    public List<Order> getAllOrders() {
        log.info("📋 Fetching all orders (admin)");
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        log.info("Found {} total orders", orders.size());
        return orders;
    }

    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(String status) {
        log.info("📋 Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        log.info("Found {} orders with status {}", orders.size(), status);
        return orders;
    }

    /**
     * Cancel order (if not yet shipped)
     */
    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order {} by user {}", orderId, userId);

        Order order = getOrderById(orderId);

        // Verify user owns this order
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        // Check if order can be cancelled
        if ("SHIPPED".equals(order.getStatus()) || "DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel order that has been shipped or delivered");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Order is already cancelled");
        }

        // Restore product stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
                log.info("Restored {} units of {} to stock", item.getQuantity(), product.getName());
            }
        }

        // Update order status
        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);

        log.info("✅ Order {} cancelled successfully", orderId);

        return cancelledOrder;
    }
}