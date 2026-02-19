package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.entity.OrderItem;
import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private Double price;
    private Double subtotal;

    public static OrderItemDTO fromEntity(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProductName());
        dto.setProductImageUrl(item.getProductImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getPrice() * item.getQuantity());
        return dto;
    }
}