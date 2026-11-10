package com.restaurant.order.repository;

import com.restaurant.order.entity.RestaurantOrder;
import com.restaurant.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<RestaurantOrder, Long> {

    List<RestaurantOrder> findByStatus(OrderStatus status);
}
