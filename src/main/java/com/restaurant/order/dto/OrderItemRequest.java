package com.restaurant.order.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.restaurant.order.entity.OrderStatus;

@Data public class OrderItemRequest {
    @NotNull @Positive private Long dishId;
    @NotNull @Min(1) @Max(100) private Integer quantity;
}
