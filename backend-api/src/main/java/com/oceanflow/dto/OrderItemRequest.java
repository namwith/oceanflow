package com.oceanflow.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productId;
    private Double orderedWeight;
    private Double pricePerUnit;
}
