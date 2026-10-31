package com.restaurant.order.repository;

import com.restaurant.order.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<RestaurantOrder,Long> {
}
