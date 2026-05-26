package com.oceanflow.repository;

import com.oceanflow.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    // Câu query lõi của FEFO: Lấy các lô hàng của 1 sản phẩm, còn tồn kho (>0) và
    // xếp theo hạn sử dụng tăng dần
    @Query("SELECT pb FROM ProductBatch pb WHERE pb.product.id = :productId AND pb.quantityStored > 0 ORDER BY pb.expiryDate ASC")
    List<ProductBatch> findAvailableBatchesByProductIdFEFO(@Param("productId") Long productId);
}
