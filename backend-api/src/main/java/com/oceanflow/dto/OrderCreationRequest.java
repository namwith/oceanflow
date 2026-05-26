package com.oceanflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreationRequest {
    private List<OrderItemRequest> items;
    private String paymentMethod;
}
