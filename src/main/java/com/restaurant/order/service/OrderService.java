package com.restaurant.order.service;

import com.restaurant.order.dto.*;
import com.restaurant.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    List<OrderResponse> getAll(OrderStatus status);

    OrderResponse getById(Long id);

    OrderResponse create(OrderRequest request);

    OrderResponse updateStatus(Long id, OrderStatusRequest request);
}
