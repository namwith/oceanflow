package com.oceanflow.service;

import com.oceanflow.dto.OrderCreationRequest;
import com.oceanflow.dto.OrderItemRequest;
import com.oceanflow.dto.OrderResponseDTO;
import com.oceanflow.dto.WeightUpdateRequest;
import com.oceanflow.entity.Order;
import com.oceanflow.entity.OrderItem;
import com.oceanflow.entity.Product;
import com.oceanflow.entity.ProductBatch;
import com.oceanflow.mapper.OrderMapper;
import com.oceanflow.repository.OrderItemRepository;
import com.oceanflow.repository.OrderRepository;
import com.oceanflow.repository.ProductBatchRepository;
import com.oceanflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrders(String status) {
        List<Order> orders = (status == null || status.isBlank())
                ? orderRepository.findAll()
                : orderRepository.findByStatusOrderByIdAsc(status);

        return orders.stream()
                .map(OrderMapper::toOrderResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Logic nghiệp vụ: Nhân viên kho cập nhật cân nặng thực tế và hệ thống tiến
     * hành trừ kho FEFO
     */
    @Transactional
    public OrderResponseDTO updateWeightAndProcessOrder(Long orderId, WeightUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được xử lý hoặc đã hủy, không thể thay đổi cân nặng.");
        }

        double newTotalAmount = 0.0;

        for (WeightUpdateRequest.ActualWeightItem updateItem : request.getItems()) {
            OrderItem item = orderItemRepository.findById(updateItem.getOrderItemId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy OrderItem."));

            item.setActualWeight(updateItem.getActualWeight());
            double subtotal = updateItem.getActualWeight() * item.getPricePerUnit();
            item.setSubtotal(subtotal);
            orderItemRepository.save(item);

            newTotalAmount += subtotal;
            deductInventoryFEFO(item.getProduct().getId(), updateItem.getActualWeight());
        }

        order.setTotalAmount(newTotalAmount);
        order.setStatus("PROCESSING");
        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toOrderResponseDTO(savedOrder);
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderCreationRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Đơn hàng phải có ít nhất một mục.");
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setStatus("PENDING");
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD");

        double totalAmount = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(
                            () -> new RuntimeException("Sản phẩm không tồn tại, ID: " + itemRequest.getProductId()));

            double orderedWeight = itemRequest.getOrderedWeight() != null ? itemRequest.getOrderedWeight() : 0.0;
            double pricePerUnit = itemRequest.getPricePerUnit() != null ? itemRequest.getPricePerUnit()
                    : product.getBasePrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setOrderedWeight(orderedWeight);
            orderItem.setActualWeight(null);
            orderItem.setPricePerUnit(pricePerUnit);
            orderItem.setSubtotal(orderedWeight * pricePerUnit);
            order.getItems().add(orderItem);

            totalAmount += orderItem.getSubtotal();
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toOrderResponseDTO(savedOrder);
    }

    private String generateOrderCode() {
        return "OD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void deductInventoryFEFO(Long productId, double requiredWeight) {
        List<ProductBatch> availableBatches = productBatchRepository.findAvailableBatchesByProductIdFEFO(productId);
        double remainingWeightToDeduct = requiredWeight;

        for (ProductBatch batch : availableBatches) {
            if (remainingWeightToDeduct <= 0)
                break;

            double availableInBatch = batch.getQuantityStored();

            if (availableInBatch >= remainingWeightToDeduct) {
                batch.setQuantityStored(availableInBatch - remainingWeightToDeduct);
                productBatchRepository.save(batch);
                remainingWeightToDeduct = 0;
            } else {
                remainingWeightToDeduct -= availableInBatch;
                batch.setQuantityStored(0.0);
                productBatchRepository.save(batch);
            }
        }

        if (remainingWeightToDeduct > 0) {
            throw new RuntimeException("Cảnh báo: Kho không đủ hàng cho sản phẩm ID " + productId +
                    ". Thiếu " + remainingWeightToDeduct + " (Kg/Đơn vị).");
        }
    }
}
