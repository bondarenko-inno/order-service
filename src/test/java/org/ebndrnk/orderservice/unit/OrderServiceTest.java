package org.ebndrnk.orderservice.unit;

import org.ebndrnk.orderservice.exception.ItemUpdateException;
import org.ebndrnk.orderservice.exception.OrderNotFoundException;
import org.ebndrnk.orderservice.mapper.OrderMapper;
import org.ebndrnk.orderservice.model.dto.OrderRequest;
import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.model.dto.UpdateOrderRequest;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.model.entity.Order;
import org.ebndrnk.orderservice.model.entity.OrderItem;
import org.ebndrnk.orderservice.repository.OrderRepository;
import org.ebndrnk.orderservice.service.ItemService;
import org.ebndrnk.orderservice.service.OrderService;
import org.ebndrnk.orderservice.service.UserInfoService;
import org.ebndrnk.orderservice.util.JwtParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ItemService itemService;
    @Mock private OrderMapper orderMapper;
    @Mock private UserInfoService userInfoService;
    @Mock private JwtParser jwtParser;

    @InjectMocks private OrderService orderService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_shouldSaveAndReturnResponse() {
        OrderRequest.OrderItemDto orderItemDto = new OrderRequest.OrderItemDto();
        orderItemDto.setItemId(1L);
        orderItemDto.setQuantity(2L);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(orderItemDto));

        Order saved = new Order();
        saved.setUserId("test@example.com");

        OrderResponse mapped = new OrderResponse();
        OrderResponse finalResp = new OrderResponse();

        when(jwtParser.getEmailFromToken()).thenReturn("test@example.com");
        when(itemService.reserveItem(1L, 2L)).thenReturn(new Item());
        when(orderRepository.save(any(Order.class))).thenReturn(saved);
        when(orderMapper.entityToResponse(saved)).thenReturn(mapped);
        when(userInfoService.addUserInfoToOrderResponse(mapped, "test@example.com")).thenReturn(finalResp);

        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isEqualTo(finalResp);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getById_shouldReturnResponse_whenOrderExists() {
        Order order = new Order();
        order.setUserId("u1");
        OrderResponse mapped = new OrderResponse();
        OrderResponse withUser = new OrderResponse();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.entityToResponse(order)).thenReturn(mapped);
        when(userInfoService.addUserInfoToOrderResponse(mapped, "u1")).thenReturn(withUser);

        OrderResponse result = orderService.getById(1L);

        assertThat(result).isEqualTo(withUser);
    }

    @Test
    void getById_shouldThrow_whenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getById(1L));
    }

    @Test
    void deleteById_shouldReturnItemsToStockAndDeleteOrder() {
        Order order = new Order();
        OrderItem oi = new OrderItem();
        Item item = new Item();
        item.setQuantity(5L);
        oi.setItem(item);
        oi.setQuantity(2L);
        order.getItems().add(oi);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteById(1L);

        assertThat(item.getQuantity()).isEqualTo(7L);
        verify(orderRepository).delete(order);
    }

    @Test
    void updateById_shouldWrapExceptionsIntoItemUpdateException() {
        when(orderRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));
        UpdateOrderRequest req = new UpdateOrderRequest(List.of());

        assertThrows(ItemUpdateException.class, () -> orderService.updateById(1L, req));
    }

    // -----------------------
    // Tests for updateById logic
    // -----------------------

    @Test
    void updateById_shouldIncreaseQuantityAndReserve() {
        // given: existing order has item id=1 qty=2, request asks qty=5 -> reserve 3
        OrderRequest.OrderItemDto dto = new OrderRequest.OrderItemDto();
        dto.setItemId(1L);
        dto.setQuantity(5L);
        UpdateOrderRequest req = new UpdateOrderRequest(List.of(dto));

        Order order = new Order();
        order.setUserId("user1");
        Item item = new Item();
        item.setId(1L);
        item.setQuantity(100L);
        OrderItem existing = new OrderItem();
        existing.setItem(item);
        existing.setQuantity(2L);
        existing.setOrder(order);
        order.getItems().add(existing);

        OrderResponse mapped = new OrderResponse();
        OrderResponse withUser = new OrderResponse();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemService.reserveItem(1L, 3L)).thenReturn(item);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.entityToResponse(order)).thenReturn(mapped);
        when(userInfoService.addUserInfoToOrderResponse(mapped, "user1")).thenReturn(withUser);

        OrderResponse result = orderService.updateById(1L, req);

        assertThat(result).isEqualTo(withUser);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(5L);
        verify(itemService).reserveItem(1L, 3L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateById_shouldDecreaseQuantityAndReturnStock() {
        // given: existing qty=5 -> request qty=2 -> return 3
        OrderRequest.OrderItemDto dto = new OrderRequest.OrderItemDto();
        dto.setItemId(1L);
        dto.setQuantity(2L);
        UpdateOrderRequest req = new UpdateOrderRequest(List.of(dto));

        Order order = new Order();
        order.setUserId("user1");
        Item item = new Item();
        item.setId(1L);
        item.setQuantity(50L);
        OrderItem existing = new OrderItem();
        existing.setItem(item);
        existing.setQuantity(5L);
        existing.setOrder(order);
        order.getItems().add(existing);

        OrderResponse mapped = new OrderResponse();
        OrderResponse withUser = new OrderResponse();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(itemService).returnItem(1L, 3L);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.entityToResponse(order)).thenReturn(mapped);
        when(userInfoService.addUserInfoToOrderResponse(mapped, "user1")).thenReturn(withUser);

        OrderResponse result = orderService.updateById(1L, req);

        assertThat(result).isEqualTo(withUser);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(2L);
        verify(itemService).returnItem(1L, 3L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateById_shouldAddNewItemAndReserve() {
        // given: order has no items, request adds item id=2 qty=4
        OrderRequest.OrderItemDto dto = new OrderRequest.OrderItemDto();
        dto.setItemId(2L);
        dto.setQuantity(4L);
        UpdateOrderRequest req = new UpdateOrderRequest(List.of(dto));

        Order order = new Order();
        order.setUserId("user2");

        Item item = new Item();
        item.setId(2L);
        item.setQuantity(20L);

        OrderResponse mapped = new OrderResponse();
        OrderResponse withUser = new OrderResponse();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemService.reserveItem(2L, 4L)).thenReturn(item);
        when(itemService.getItemById(2L)).thenReturn(item);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.entityToResponse(order)).thenReturn(mapped);
        when(userInfoService.addUserInfoToOrderResponse(mapped, "user2")).thenReturn(withUser);

        OrderResponse result = orderService.updateById(1L, req);

        assertThat(result).isEqualTo(withUser);
        assertThat(order.getItems()).hasSize(1);
        OrderItem added = order.getItems().get(0);
        assertThat(added.getItem()).isEqualTo(item);
        assertThat(added.getQuantity()).isEqualTo(4L);
        verify(itemService).reserveItem(2L, 4L);
        verify(itemService).getItemById(2L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateById_shouldRemoveDeletedItemsAndReturnStock() {
        // given: order has one item but request is empty -> should remove and return stock
        UpdateOrderRequest req = new UpdateOrderRequest(List.of());

        Order order = new Order();
        order.setUserId("user3");
        Item item = new Item();
        item.setId(3L);
        item.setQuantity(10L);
        OrderItem oi = new OrderItem();
        oi.setItem(item);
        oi.setQuantity(2L);
        oi.setOrder(order);
        order.getItems().add(oi);

        OrderResponse mapped = new OrderResponse();
        OrderResponse withUser = new OrderResponse();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(itemService).returnItem(3L, 2L);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.entityToResponse(order)).thenReturn(mapped);
        when(userInfoService.addUserInfoToOrderResponse(mapped, "user3")).thenReturn(withUser);

        OrderResponse result = orderService.updateById(1L, req);

        assertThat(result).isEqualTo(withUser);
        assertThat(order.getItems()).isEmpty();
        verify(itemService).returnItem(3L, 2L);
        verify(orderRepository).save(order);
    }
}
