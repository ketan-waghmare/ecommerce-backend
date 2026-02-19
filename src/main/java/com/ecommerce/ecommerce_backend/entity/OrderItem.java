package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    private Product product;

    private Integer quantity;

    private Double price;

    // NEW SNAPSHOT FIELDS (CRITICAL!)
    @Column(name = "product_name", nullable = false)
    private String productName;  // Product name at time of purchase

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;  // Product image at time of purchase
}
