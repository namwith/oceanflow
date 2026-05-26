package com.oceanflow.service;

import com.oceanflow.dto.OrderResponseDTO;
import com.oceanflow.dto.WeightUpdateRequest;
import com.oceanflow.entity.Order;
import com.oceanflow.entity.OrderItem;
import com.oceanflow.entity.Product;
import com.oceanflow.entity.ProductBatch;
import com.oceanflow.repository.OrderItemRepository;
import com.oceanflow.repository.OrderRepository;
import com.oceanflow.repository.ProductBatchRepository;
import com.oceanflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void updateWeightAndProcessOrder_usesOldestBatchFirstAndUpdatesTotals() {
        Product product = buildProduct(1L, "Cá Tầm Sapa", 300000.0);
        Order order = buildOrder(100L, "PENDING", "COD");
        OrderItem item = buildOrderItem(10L, order, product, 2.0, 300000.0);
        order.getItems().add(item);

        ProductBatch olderBatch = buildBatch(1L, product, "LOT-OLD", 3.0, LocalDate.now().plusDays(1));
        ProductBatch newerBatch = buildBatch(2L, product, "LOT-NEW", 5.0, LocalDate.now().plusDays(10));

        WeightUpdateRequest request = buildWeightRequest(10L, 4.5);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(productBatchRepository.findAvailableBatchesByProductIdFEFO(1L)).thenReturn(List.of(olderBatch, newerBatch));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productBatchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.updateWeightAndProcessOrder(100L, request);

        assertEquals("PROCESSING", response.getStatus());
        assertEquals(1_350_000.0, response.getTotalAmount(), 0.0001);
        assertEquals(4.5, item.getActualWeight(), 0.0001);
        assertEquals(1_350_000.0, item.getSubtotal(), 0.0001);
        assertEquals(0.0, olderBatch.getQuantityStored(), 0.0001);
        assertEquals(3.5, newerBatch.getQuantityStored(), 0.0001);

        InOrder inOrder = inOrder(productBatchRepository);
        inOrder.verify(productBatchRepository).save(olderBatch);
        inOrder.verify(productBatchRepository).save(newerBatch);
        verify(orderItemRepository).save(item);
        verify(orderRepository).save(order);
    }

    @Test
    void updateWeightAndProcessOrder_throwsExceptionAndSkipsOrderSaveWhenInventoryIsInsufficient() {
        Product product = buildProduct(1L, "Cá Tầm Sapa", 300000.0);
        Order order = buildOrder(101L, "PENDING", "COD");
        OrderItem item = buildOrderItem(11L, order, product, 2.0, 300000.0);
        order.getItems().add(item);

        ProductBatch batch1 = buildBatch(1L, product, "LOT-LOW", 2.0, LocalDate.now().plusDays(1));
        ProductBatch batch2 = buildBatch(2L, product, "LOT-MID", 3.0, LocalDate.now().plusDays(5));

        WeightUpdateRequest request = buildWeightRequest(11L, 7.0);

        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(11L)).thenReturn(Optional.of(item));
        when(productBatchRepository.findAvailableBatchesByProductIdFEFO(1L)).thenReturn(List.of(batch1, batch2));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productBatchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.updateWeightAndProcessOrder(101L, request));

        assertTrue(exception.getMessage().contains("Kho không đủ hàng"));
        assertEquals(0.0, batch1.getQuantityStored(), 0.0001);
        assertEquals(0.0, batch2.getQuantityStored(), 0.0001);
        verify(orderItemRepository).save(item);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateWeightAndProcessOrder_recalculatesSubtotalWithDecimalPrecision() {
        Product product = buildProduct(2L, "Tôm Sú", 99.99);
        Order order = buildOrder(102L, "PENDING", "VIETQR");
        OrderItem item = buildOrderItem(12L, order, product, 1.0, 99.99);
        order.getItems().add(item);

        ProductBatch batch = buildBatch(3L, product, "LOT-DECIMAL", 2.0, LocalDate.now().plusDays(2));

        WeightUpdateRequest request = buildWeightRequest(12L, 1.25);

        when(orderRepository.findById(102L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(12L)).thenReturn(Optional.of(item));
        when(productBatchRepository.findAvailableBatchesByProductIdFEFO(2L)).thenReturn(List.of(batch));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productBatchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.updateWeightAndProcessOrder(102L, request);

        assertEquals(124.9875, response.getTotalAmount(), 0.0001);
        assertEquals(124.9875, item.getSubtotal(), 0.0001);
        assertEquals(0.75, batch.getQuantityStored(), 0.0001);
        verify(productBatchRepository).save(batch);
        verify(orderRepository).save(order);
    }

    private Product buildProduct(Long id, String name, Double basePrice) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSellingUnit("KG");
        product.setIsWeightVar(true);
        product.setBasePrice(basePrice);
        return product;
    }

    private Order buildOrder(Long id, String status, String paymentMethod) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setPaymentMethod(paymentMethod);
        order.setItems(new ArrayList<>());
        return order;
    }

    private OrderItem buildOrderItem(Long id, Order order, Product product, Double orderedWeight, Double pricePerUnit) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrder(order);
        item.setProduct(product);
        item.setOrderedWeight(orderedWeight);
        item.setActualWeight(null);
        item.setPricePerUnit(pricePerUnit);
        item.setSubtotal(orderedWeight * pricePerUnit);
        return item;
    }

    private ProductBatch buildBatch(Long id, Product product, String batchCode, Double quantityStored, LocalDate expiryDate) {
        ProductBatch batch = new ProductBatch();
        batch.setId(id);
        batch.setProduct(product);
        batch.setBatchCode(batchCode);
        batch.setQuantityStored(quantityStored);
        batch.setExpiryDate(expiryDate);
        batch.setOriginLocation("Kho chính");
        return batch;
    }

    private WeightUpdateRequest buildWeightRequest(Long orderItemId, Double actualWeight) {
        WeightUpdateRequest request = new WeightUpdateRequest();
        WeightUpdateRequest.ActualWeightItem item = new WeightUpdateRequest.ActualWeightItem();
        item.setOrderItemId(orderItemId);
        item.setActualWeight(actualWeight);
        request.setItems(List.of(item));
        return request;
    }
}
