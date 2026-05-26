package com.oceanflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private String orderCode;
    private String status;
    private Double totalAmount;
    private String paymentMethod;
    private String cancelReason;
    private List<OrderItemResponseDTO> items;
}
