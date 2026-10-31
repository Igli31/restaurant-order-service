package com.restaurant.order.service;

import com.restaurant.order.dto.*;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getAll();
    OrderResponse getById(Long id);
    OrderResponse create(OrderRequest request);
    OrderResponse updateStatus(Long id,OrderStatusRequest request);
}
