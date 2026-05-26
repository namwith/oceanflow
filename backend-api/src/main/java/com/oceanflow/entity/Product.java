package com.oceanflow.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "selling_unit", nullable = false)
    private String sellingUnit; // KG, CON, KHAY, COMBO

    @Column(name = "is_weight_var", nullable = false)
    private Boolean isWeightVar; // true nếu có dung sai trọng lượng khi cân

    private Double basePrice; // Giá niêm yết trên 1 đơn vị (VD: 1kg)
}
