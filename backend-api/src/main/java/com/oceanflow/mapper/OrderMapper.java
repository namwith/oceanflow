package com.oceanflow.mapper;

import com.oceanflow.dto.OrderItemResponseDTO;
import com.oceanflow.dto.OrderResponseDTO;
import com.oceanflow.entity.Order;
import com.oceanflow.entity.OrderItem;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponseDTO toOrderResponseDTO(Order order) {
        if (order == null)
            return null;

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCancelReason(order.getCancelReason());

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(OrderMapper::toOrderItemResponseDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item) {
        if (item == null)
            return null;

        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
        }

        dto.setOrderedWeight(item.getOrderedWeight());
        dto.setActualWeight(item.getActualWeight());
        dto.setPricePerUnit(item.getPricePerUnit());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}
