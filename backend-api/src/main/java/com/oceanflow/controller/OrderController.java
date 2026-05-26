package com.oceanflow.controller;

import com.oceanflow.dto.ApiResponse;
import com.oceanflow.dto.OrderCreationRequest;
import com.oceanflow.dto.OrderResponseDTO;
import com.oceanflow.dto.WeightUpdateRequest;
import com.oceanflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @RequestBody OrderCreationRequest request) {

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Thông tin đơn hàng không được để trống.");
        }

        OrderResponseDTO createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdOrder, "Tạo đơn hàng thành công."));
    }

    @PutMapping("/{orderId}/actual-weights")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateActualWeights(
            @PathVariable Long orderId,
            @RequestBody WeightUpdateRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Dữ liệu cập nhật không được để trống.");
        }

        OrderResponseDTO updatedOrder = orderService.updateWeightAndProcessOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Cập nhật cân nặng và trừ kho thành công!"));
    }
}
