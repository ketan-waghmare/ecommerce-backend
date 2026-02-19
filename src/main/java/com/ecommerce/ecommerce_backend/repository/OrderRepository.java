package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order>  findByUserId(Long userId);

    // Order history for a user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);


    // Find by order number (for order tracking)
    Optional<Order> findByOrderNumber(String orderNumber);

    // Get all orders newest first (for admin)
    List<Order> findAllByOrderByCreatedAtDesc();

    // Get orders by status (for admin filtering)
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    // Get orders by user and status
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
