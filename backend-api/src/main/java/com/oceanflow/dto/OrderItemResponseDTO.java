package com.oceanflow.dto;

import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Double orderedWeight;
    private Double actualWeight;
    private Double pricePerUnit;
    private Double subtotal;
}
