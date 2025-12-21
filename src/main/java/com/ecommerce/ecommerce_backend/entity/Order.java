package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Long userId;   // later from JWT

        private Double totalAmount;

        private String status; // CREATED, PAID, SHIPPED

        private LocalDateTime createdAt;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
        private List<OrderItem> items;


        @PrePersist
        public void prePersist() {
                this.createdAt = LocalDateTime.now();
        }

}

