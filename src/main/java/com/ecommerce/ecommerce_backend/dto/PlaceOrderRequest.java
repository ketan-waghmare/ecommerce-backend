package com.ecommerce.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    private String shippingCity;

    @NotBlank(message = "State is required")
    private String shippingState;

    @NotBlank(message = "ZIP code is required")
    private String shippingZip;

    @NotBlank(message = "Phone number is required")
    private String shippingPhone;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}