package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order>  findByUserId(Long userId);

    // Order history for a user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
