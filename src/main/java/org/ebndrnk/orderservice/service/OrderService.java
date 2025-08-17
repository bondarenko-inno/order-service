package org.ebndrnk.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.orderservice.exception.OrderNotFoundException;
import org.ebndrnk.orderservice.mapper.OrderMapper;
import org.ebndrnk.orderservice.model.dto.OrderRequest;
import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.model.dto.UpdateOrderRequest;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.model.entity.Order;
import org.ebndrnk.orderservice.model.entity.OrderItem;
import org.ebndrnk.orderservice.model.entity.OrderStatus;
import org.ebndrnk.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final OrderMapper orderMapper;
    private final UserInfoService userInfoService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PROCESSING);

        addOrderItems(order, request.getItems());

        Order savedOrder = orderRepository.save(order);
        return userInfoService.addUserInfoToOrderResponse(orderMapper.entityToResponse(savedOrder), request.getUserId()); //TODO email из запроса
    }

    public OrderResponse getById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id: " + orderId + " not found"));
        return userInfoService.addUserInfoToOrderResponse(orderMapper.entityToResponse(order), order.getUserId());
    }

    public List<OrderResponse> getByIds(List<Long> orderIds) {
        List<Order> orders = orderRepository.findAllById(orderIds);
        return orders.stream()
                .map(orderMapper::entityToResponse)
                .map(orderResponse -> userInfoService.addUserInfoToOrderResponse(orderResponse, orderResponse.getUserId()))
                .toList();
    }

    public List<OrderResponse> getByStatuses(List<OrderStatus> statuses) {
        List<Order> orders = orderRepository.findAllByStatusIn(statuses);
        return orders.stream()
                .map(orderMapper::entityToResponse)
                .map(orderResponse -> userInfoService.addUserInfoToOrderResponse(orderResponse, orderResponse.getUserId()))
                .toList();
    }

    @Transactional
    public void deleteById(Long orderId) {
        System.out.println(orderId);

        Order order  = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id: " + orderId + " not found"));

        for (OrderItem orderItem : order.getItems()) {
            Item item = orderItem.getItem();
            item.setQuantity(item.getQuantity() + orderItem.getQuantity());
        }

        orderRepository.delete(order);
    }



    private void addOrderItems(Order order, List<OrderRequest.OrderItemDto> items) {
        for (OrderRequest.OrderItemDto itemDto : items) {
            Item item = itemService.reserveItem(itemDto.getItemId(), itemDto.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setOrder(order);

            order.getItems().add(orderItem);
        }
    }


    @Transactional
    public OrderResponse updateById(Long orderId, UpdateOrderRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order with id: " + orderId + " not found"));


            OrderItemsUpdater updater = new OrderItemsUpdater(order, itemService);
            updater.update(request.items());
            Order savedOrder = orderRepository.save(order);
            return userInfoService.addUserInfoToOrderResponse(orderMapper.entityToResponse(savedOrder), order.getUserId());
        } catch (Exception e) {
            log.error("Failed to update order with id {}", orderId, e);
            throw e;
        }
    }

    @RequiredArgsConstructor
    private static class OrderItemsUpdater {
        private final Order order;
        private final ItemService itemService;

        public void update(List<OrderRequest.OrderItemDto> newItems) {
            if (newItems == null) {
                return;
            }

            List<OrderItem> oldItems = new ArrayList<>(order.getItems());
            Map<Long, OrderItem> oldItemsMap = mapOrderItemsByItemId(oldItems);

            processExistingAndUpdatedItems(oldItemsMap, newItems);
            removeDeletedItems(oldItemsMap);
        }

        private Map<Long, OrderItem> mapOrderItemsByItemId(List<OrderItem> items) {
            return items.stream()
                    .collect(Collectors.toMap(oi -> oi.getItem().getId(), oi -> oi));
        }

        private void processExistingAndUpdatedItems(Map<Long, OrderItem> oldItemsMap, List<OrderRequest.OrderItemDto> newItems) {
            for (OrderRequest.OrderItemDto newItem : newItems) {
                validateQuantity(newItem.getQuantity());

                OrderItem oldItem = oldItemsMap.get(newItem.getItemId());

                if (oldItem != null) {
                    adjustQuantityForExistingItem(newItem, oldItem);
                    oldItemsMap.remove(newItem.getItemId());
                } else {
                    addNewOrderItem(newItem);
                }
            }
        }

        private void validateQuantity(Long quantity) {
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }

        private void adjustQuantityForExistingItem(OrderRequest.OrderItemDto newItem, OrderItem oldItem) {
            long oldQty = oldItem.getQuantity();
            long newQty = newItem.getQuantity();

            if (newQty > oldQty) {
                itemService.reserveItem(newItem.getItemId(), newQty - oldQty);
            } else if (newQty < oldQty) {
                itemService.returnItem(newItem.getItemId(), oldQty - newQty);
            }
            oldItem.setQuantity(newQty);
        }

        private void addNewOrderItem(OrderRequest.OrderItemDto newItem) {
            itemService.reserveItem(newItem.getItemId(), newItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(itemService.getItemById(newItem.getItemId()));
            orderItem.setQuantity(newItem.getQuantity());
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
        }

        private void removeDeletedItems(Map<Long, OrderItem> oldItemsMap) {
            List<OrderItem> toRemove = new ArrayList<>(oldItemsMap.values());

            for (OrderItem itemToRemove : toRemove) {
                itemService.returnItem(itemToRemove.getItem().getId(), itemToRemove.getQuantity());
                order.getItems().remove(itemToRemove);
            }
        }
    }
}
