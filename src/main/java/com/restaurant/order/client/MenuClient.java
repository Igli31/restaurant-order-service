package com.restaurant.order.client;

import com.restaurant.order.dto.MenuDishResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "menu-service", url = "${clients.menu.url}")
public interface MenuClient {
    @GetMapping("/api/dishes/{id}")
    MenuDishResponse getDish(@PathVariable("id") Long id);
}
