package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.entity.Order;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingPhone;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;

    public static OrderDTO fromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingZip(order.getShippingZip());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(OrderItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}