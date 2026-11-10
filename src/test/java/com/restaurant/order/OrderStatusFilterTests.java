package com.restaurant.order;

import com.restaurant.order.controller.OrderController;
import com.restaurant.order.entity.OrderStatus;
import com.restaurant.order.entity.RestaurantOrder;
import com.restaurant.order.exception.ApiExceptionHandler;
import com.restaurant.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderfiltertest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class OrderStatusFilterTests {

    @Autowired
    private OrderController controller;

    @Autowired
    private OrderRepository repository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private RestaurantOrder saveOrder(OrderStatus status, String createdAt) {
        var order = new RestaurantOrder();
        order.setCustomerName("Alex");
        order.setTableNumber(2);
        order.setStatus(status);
        order.setCreatedAt(Instant.parse(createdAt));
        order.setTotal(BigDecimal.ZERO);
        return repository.saveAndFlush(order);
    }

    @Test
    void returnsAllOrdersNewestFirstWhenStatusIsOmitted() throws Exception {
        var older = saveOrder(OrderStatus.PLACED, "2026-01-01T10:00:00Z");
        var newer = saveOrder(OrderStatus.SERVED, "2026-01-01T11:00:00Z");

        mvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newer.getId().intValue()))
                .andExpect(jsonPath("$[1].id").value(older.getId().intValue()));
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void returnsOnlyMatchingOrdersNewestFirst(OrderStatus selectedStatus) throws Exception {
        var older = saveOrder(selectedStatus, "2026-01-01T10:00:00Z");
        var newer = saveOrder(selectedStatus, "2026-01-01T11:00:00Z");
        for (var otherStatus : OrderStatus.values()) {
            if (otherStatus != selectedStatus) {
                saveOrder(otherStatus, "2026-01-01T12:00:00Z");
            }
        }

        mvc.perform(get("/api/orders").param("status", selectedStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newer.getId().intValue()))
                .andExpect(jsonPath("$[1].id").value(older.getId().intValue()))
                .andExpect(jsonPath("$[0].status").value(selectedStatus.name()))
                .andExpect(jsonPath("$[1].status").value(selectedStatus.name()));
    }

    @Test
    void returnsAnEmptyListWhenNoOrdersMatch() throws Exception {
        saveOrder(OrderStatus.SERVED, "2026-01-01T10:00:00Z");

        mvc.perform(get("/api/orders").param("status", "PLACED"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void rejectsAnUnknownStatus() throws Exception {
        mvc.perform(get("/api/orders").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
