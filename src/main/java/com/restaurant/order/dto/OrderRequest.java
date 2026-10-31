package com.restaurant.order.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.restaurant.order.entity.OrderStatus;

@Data
public class OrderRequest {

    @NotBlank
    @Size(max = 100)
    private String customerName;

    @NotNull
    @Min(1)
    @Max(1000)
    private Integer tableNumber;

    @NotEmpty
    @Size(max = 50)
    @Valid
    private List<@NotNull OrderItemRequest> items;
}
