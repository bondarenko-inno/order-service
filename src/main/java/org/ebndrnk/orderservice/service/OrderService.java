package org.ebndrnk.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ebndrnk.orderservice.client.dto.UserResponse;
import org.ebndrnk.orderservice.exception.CardNotFoundException;
import org.ebndrnk.orderservice.exception.ItemUpdateException;
import org.ebndrnk.orderservice.exception.OrderNotFoundException;
import org.ebndrnk.orderservice.kafka.OrderCreatedPublisher;
import org.ebndrnk.orderservice.kafka.dto.PaymentResponse;
import org.ebndrnk.orderservice.mapper.OrderMapper;
import org.ebndrnk.orderservice.mapper.StatusMapper;
import org.ebndrnk.orderservice.model.dto.OrderRequest;
import org.ebndrnk.orderservice.model.dto.OrderResponse;
import org.ebndrnk.orderservice.model.dto.UpdateOrderRequest;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.model.entity.Order;
import org.ebndrnk.orderservice.model.entity.OrderItem;
import org.ebndrnk.orderservice.model.entity.OrderStatus;
import org.ebndrnk.orderservice.repository.OrderRepository;
import org.ebndrnk.orderservice.util.JwtParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final JwtParser jwtParser;
    private final OrderCreatedPublisher orderCreatedPublisher;
    private final StatusMapper statusMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();

        String email = jwtParser.getEmailFromToken();

        order.setUserId(email);
        order.setStatus(OrderStatus.PENDING);

        addOrderItems(order, request.getItems());

        Order savedOrder = orderRepository.save(order);

        OrderResponse orderResponse = userInfoService.addUserInfoToOrderResponse(orderMapper.entityToResponse(savedOrder), email);

        if(orderResponse.getUserResponse().isCardAvailable()){
            orderCreatedPublisher.publishOrderCreated(savedOrder, getAmount(savedOrder));
            return orderResponse;
        } else {
            throw new CardNotFoundException("User should have a card for this action");
        }
    }

    @Transactional
    public void setOrderStatus(PaymentResponse paymentResponse) {
        Order order = orderRepository.findById(Long.valueOf(paymentResponse.orderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        OrderStatus orderStatus = statusMapper.toOrderStatus(paymentResponse.status());

        if(orderStatus == OrderStatus.FAILED) {
            order.getItems()
                    .forEach(orderItem -> {
                        itemService.returnItem(orderItem.getItem().getId(), orderItem.getItem().getQuantity());
                    });
        }

        order.setStatus(orderStatus);
        orderRepository.save(order);
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
        if(orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found for statuses: " + statuses);
        }
        return orders.stream()
                .map(orderMapper::entityToResponse)
                .map(orderResponse -> userInfoService.addUserInfoToOrderResponse(orderResponse, orderResponse.getUserId()))
                .toList();
    }

    @Transactional
    public void deleteById(Long orderId) {
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
            throw new ItemUpdateException("Error while updating order with id: " + orderId);
        }
    }

    private BigDecimal getAmount(Order savedOrder) {
        BigDecimal amount = BigDecimal.ZERO;
        for(OrderItem orderItem : savedOrder.getItems()) {
            amount = amount.add(BigDecimal.valueOf(orderItem.getQuantity()*orderItem.getItem().getPrice()));
        }
        return amount;
    }

    /**
     * Utility class responsible for synchronizing order items with a new list of items from request.
     * It updates quantities of existing items, adds new items, and removes items that are no longer present.
     * During the process it also calls ItemService to reserve or return stock quantities accordingly.
     */
    @RequiredArgsConstructor
    private static class OrderItemsUpdater {
        private final Order order;
        private final ItemService itemService;

        /**
         * Main entry point: updates the order items list according to the provided new items.
         */
        public void update(List<OrderRequest.OrderItemDto> newItems) {
            if (newItems == null) {
                return; // nothing to update
            }

            List<OrderItem> oldItems = new ArrayList<>(order.getItems());
            Map<Long, OrderItem> oldItemsMap = mapOrderItemsByItemId(oldItems);

            processExistingAndUpdatedItems(oldItemsMap, newItems);
            removeDeletedItems(oldItemsMap);
        }

        /**
         * Creates a map of existing order items keyed by itemId for quick lookup.
         */
        private Map<Long, OrderItem> mapOrderItemsByItemId(List<OrderItem> items) {
            return items.stream()
                    .collect(Collectors.toMap(oi -> oi.getItem().getId(), oi -> oi));
        }

        /**
         * Handles both updating existing items and adding new ones.
         * - If item already exists: adjust its quantity (reserve or return difference).
         * - If it's new: reserve the full quantity and add it to the order.
         */
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

        /**
         * Ensures that provided quantity is valid (> 0).
         */
        private void validateQuantity(Long quantity) {
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }

        /**
         * Adjusts quantity for an item that already exists in the order.
         * Reserves additional stock if increased, or returns stock if decreased.
         */
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

        /**
         * Adds a completely new item to the order with full reservation of stock.
         */
        private void addNewOrderItem(OrderRequest.OrderItemDto newItem) {
            itemService.reserveItem(newItem.getItemId(), newItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(itemService.getItemById(newItem.getItemId()));
            orderItem.setQuantity(newItem.getQuantity());
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
        }

        /**
         * Removes items that were not included in the new request anymore.
         * Returns their quantities back to the stock.
         */
        private void removeDeletedItems(Map<Long, OrderItem> oldItemsMap) {
            List<OrderItem> toRemove = new ArrayList<>(oldItemsMap.values());

            for (OrderItem itemToRemove : toRemove) {
                itemService.returnItem(itemToRemove.getItem().getId(), itemToRemove.getQuantity());
                order.getItems().remove(itemToRemove);
            }
        }

    }

}
