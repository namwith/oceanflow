package com.oceanflow.config;

import com.oceanflow.entity.Order;
import com.oceanflow.entity.OrderItem;
import com.oceanflow.entity.Product;
import com.oceanflow.entity.ProductBatch;
import com.oceanflow.repository.OrderItemRepository;
import com.oceanflow.repository.OrderRepository;
import com.oceanflow.repository.ProductBatchRepository;
import com.oceanflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() == 0) {
            System.out.println("🌱 Đang khởi tạo dữ liệu mẫu cho dự án OceanFlow...");

            Product product = new Product();
            product.setName("Cá Tầm Sapa nguyên con");
            product.setSellingUnit("KG");
            product.setIsWeightVar(true);
            product.setBasePrice(300000.0);
            product = productRepository.save(product);

            ProductBatch batch1 = new ProductBatch();
            batch1.setProduct(product);
            batch1.setBatchCode("LOT-CATAM-001");
            batch1.setQuantityStored(1.5);
            batch1.setExpiryDate(LocalDate.now().plusDays(2));
            batch1.setOriginLocation("Trại cá Tầm Sapa");
            productBatchRepository.save(batch1);

            ProductBatch batch2 = new ProductBatch();
            batch2.setProduct(product);
            batch2.setBatchCode("LOT-CATAM-002");
            batch2.setQuantityStored(5.0);
            batch2.setExpiryDate(LocalDate.now().plusDays(10));
            batch2.setOriginLocation("Trại cá Tầm Đà Lạt");
            productBatchRepository.save(batch2);

            Order order = new Order();
            order.setOrderCode("ORD-2026-0001");
            order.setStatus("PENDING");
            order.setPaymentMethod("COD");
            order.setTotalAmount(600000.0);
            order = orderRepository.save(order);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setOrderedWeight(2.0);
            item.setPricePerUnit(300000.0);
            item.setSubtotal(600000.0);
            orderItemRepository.save(item);

            System.out.println("✅ Khởi tạo dữ liệu mẫu thành công!");
        }
    }
}
