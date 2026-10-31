package com.restaurant.order;

import com.restaurant.order.client.MenuClient;
import com.restaurant.order.dto.*;
import com.restaurant.order.entity.*;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.serviceimpl.OrderServiceImpl;
import com.restaurant.order.config.MapperConfig;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTests {
    private final OrderRepository repository=mock(OrderRepository.class);
    private final MenuClient menu=mock(MenuClient.class);
    private final OrderServiceImpl service=new OrderServiceImpl(repository,menu,new MapperConfig().modelMapper());
    private OrderRequest request() {
        var item=new OrderItemRequest();
        item.setDishId(7L);
        item.setQuantity(2);
        var request=new OrderRequest();
        request.setCustomerName("Alex");
        request.setTableNumber(2);
        request.setItems(List.of(item));
        return request;
    }
    private MenuDishResponse dish(boolean available) {
        var dish=new MenuDishResponse();
        dish.setId(7L);
        dish.setName("Pizza");
        dish.setPrice(new BigDecimal("12.50"));
        dish.setAvailable(available);
        return dish;
    }
    @Test void calculatesTotalsAndKeepsDishSnapshots() {
        var dish=dish(true);
        when(menu.getDish(7L)).thenReturn(dish);
        when(repository.save(any())).thenAnswer(call-> {
            RestaurantOrder order=call.getArgument(0); order.setId(1L); return order;
        });
        var response=service.create(request());
        assertEquals(new BigDecimal("25.00"),response.getTotal());
        assertEquals(OrderStatus.PLACED,response.getStatus());
        assertEquals("Pizza",response.getItems().get(0).getDishName());
        dish.setName("New name");
        dish.setPrice(new BigDecimal("99.00"));
        assertEquals("Pizza",response.getItems().get(0).getDishName());
        assertEquals(new BigDecimal("12.50"),response.getItems().get(0).getUnitPrice());
    }
    @Test void doesNotSaveAnOrderWhenADishIsUnavailable() {
        when(menu.getDish(7L)).thenReturn(dish(false));
        assertThrows(IllegalArgumentException.class,()->service.create(request()));
        verify(repository,never()).save(any());
    }
    @Test void doesNotSaveAnOrderWhenMenuCannotBeReached() {
        when(menu.getDish(7L)).thenThrow(new RuntimeException("Connection unavailable"));
        assertThrows(RuntimeException.class,()->service.create(request()));
        verify(repository,never()).save(any());
    }
    @Test void preventsSkippingPreparationAndChangingServedOrders() {
        var order=new RestaurantOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(Instant.now());
        order.setTotal(BigDecimal.ZERO);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(any())).thenAnswer(call->call.getArgument(0));
        var update=new OrderStatusRequest();
        update.setStatus(OrderStatus.SERVED);
        assertThrows(IllegalArgumentException.class,()->service.updateStatus(1L,update));
        update.setStatus(OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING,service.updateStatus(1L,update).getStatus());
        update.setStatus(OrderStatus.SERVED);
        assertEquals(OrderStatus.SERVED,service.updateStatus(1L,update).getStatus());
        update.setStatus(OrderStatus.CANCELLED);
        assertThrows(IllegalArgumentException.class,()->service.updateStatus(1L,update));
    }
}
