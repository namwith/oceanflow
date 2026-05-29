package com.oceanflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "product_batches")
@Data
public class ProductBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "batch_code", unique = true, nullable = false)
    private String batchCode;

    @Column(name = "quantity_stored", nullable = false)
    private Double quantityStored; // Số lượng tồn kho

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate; // Quan trọng nhất để xuất kho theo FEFO

    @Column(name = "origin_location")
    private String originLocation;

    @Column(name = "certificates", columnDefinition = "VARCHAR(4000)")
    private String certificates; // Lưu chứng nhận JSON (VARCHAR compatible với H2 + PostgreSQL)
}
