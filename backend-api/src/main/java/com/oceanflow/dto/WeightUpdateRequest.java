package com.oceanflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class WeightUpdateRequest {
    private List<ActualWeightItem> items;

    @Data
    public static class ActualWeightItem {
        private Long orderItemId;
        private Long productId;
        private Double actualWeight;
    }
}
