package com.restaurant.order.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.restaurant.order.entity.OrderStatus;

@Data public class OrderResponse {
    private Long id;
    private String customerName;
    private Integer tableNumber;
    private OrderStatus status;
    private Instant createdAt;
    private BigDecimal total;
    private List<OrderItemResponse> items;
}
