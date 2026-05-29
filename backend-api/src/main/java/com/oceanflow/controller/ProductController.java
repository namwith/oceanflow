package com.oceanflow.controller;

import com.oceanflow.dto.ApiResponse;
import com.oceanflow.entity.Product;
import com.oceanflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ApiResponse<List<Product>> getProducts() {
        return ApiResponse.success(productRepository.findAll(), "Lay danh sach san pham thanh cong.");
    }
}
