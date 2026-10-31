package com.restaurant.order.serviceimpl;

import com.restaurant.order.dto.*;
import com.restaurant.order.entity.*;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.client.MenuClient;
import com.restaurant.order.service.OrderService;
import com.restaurant.order.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;
import java.time.Instant;

@Service @Transactional(readOnly=true) public class OrderServiceImpl implements OrderService {
    private final OrderRepository orders;
    private final MenuClient menu;
    private final ModelMapper mapper;
    public OrderServiceImpl(OrderRepository orders,MenuClient menu,ModelMapper mapper) {
        this.orders=orders;
        this.menu=menu;
        this.mapper=mapper;
    }
    private RestaurantOrder find(Long id) {
        return orders.findById(id).orElseThrow(()->new ResourceNotFoundException("Order not found: "+id));
    }
    private OrderResponse response(RestaurantOrder order) {
        var dto=mapper.map(order,OrderResponse.class);
        dto.setItems(order.getItems().stream().map(i->mapper.map(i,OrderItemResponse.class)).toList());
        return dto;
    }
    public List<OrderResponse> getAll() {
        return orders.findAll().stream().sorted(Comparator.comparing(RestaurantOrder::getCreatedAt).reversed()).map(this::response).toList();
    }
    public OrderResponse getById(Long id) {
        return response(find(id));
    }
    @Transactional public OrderResponse create(OrderRequest request) {
        var order=new RestaurantOrder();
        order.setCustomerName(request.getCustomerName());
        order.setTableNumber(request.getTableNumber());
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(Instant.now());
        BigDecimal total=BigDecimal.ZERO;
        Map<Long,MenuDishResponse> snapshots=new HashMap<>();
        for(var line:request.getItems()) {
            var dish=snapshots.computeIfAbsent(line.getDishId(),menu::getDish);
            if(!dish.isAvailable())throw new IllegalArgumentException("Dish is unavailable: "+dish.getName());
            var item=new OrderItem();
            item.setOrder(order);
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setUnitPrice(dish.getPrice());
            item.setQuantity(line.getQuantity());
            item.setLineTotal(dish.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            order.getItems().add(item);
            total=total.add(item.getLineTotal());
        }
        order.setTotal(total);
        return response(orders.save(order));
    }
    @Transactional public OrderResponse updateStatus(Long id,OrderStatusRequest request) {
        var order=find(id);
        var next=request.getStatus();
        var current=order.getStatus();
        boolean allowed=(current==OrderStatus.PLACED&&(next==OrderStatus.PREPARING||next==OrderStatus.CANCELLED))||(current==OrderStatus.PREPARING&&(next==OrderStatus.SERVED||next==OrderStatus.CANCELLED));
        if(!allowed)throw new IllegalArgumentException("Cannot change status from "+current+" to "+next);
        order.setStatus(next);
        return response(orders.save(order));
    }
}
