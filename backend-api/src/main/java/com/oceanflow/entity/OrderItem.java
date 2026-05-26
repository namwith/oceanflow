package com.oceanflow.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ordered_weight", nullable = false)
    private Double orderedWeight; // Khách đặt

    @Column(name = "actual_weight")
    private Double actualWeight; // Kho cân thực tế nhập vào

    @Column(name = "price_per_unit", nullable = false)
    private Double pricePerUnit;

    @Column(name = "subtotal")
    private Double subtotal;
}
