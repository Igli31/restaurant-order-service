package com.restaurant.order.controller;

import com.restaurant.order.dto.*;
import com.restaurant.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/api/orders") public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) {
        this.service=service;
    }
    @GetMapping public List<OrderResponse> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}") public OrderResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
    @PostMapping public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        var result=service.create(request);
        return ResponseEntity.created(URI.create("/api/orders/"+result.getId())).body(result);
    }
    @PatchMapping("/{id}/status") public OrderResponse updateStatus(@PathVariable Long id,@Valid @RequestBody OrderStatusRequest request) {
        return service.updateStatus(id,request);
    }
}
